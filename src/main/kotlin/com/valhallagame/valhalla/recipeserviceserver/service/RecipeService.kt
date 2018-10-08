package com.valhallagame.valhalla.recipeserviceserver.service

import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.common.exceptions.ApiException
import com.valhallagame.common.rabbitmq.NotificationMessage
import com.valhallagame.common.rabbitmq.RabbitMQRouting
import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.currencyserviceclient.message.LockCurrencyParameter
import com.valhallagame.currencyserviceclient.message.LockedCurrencyResult
import com.valhallagame.currencyserviceclient.model.CurrencyType
import com.valhallagame.featserviceclient.message.FeatName
import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import com.valhallagame.valhalla.recipeserviceserver.repository.RecipeRepository
import com.valhallagame.wardrobeserviceclient.WardrobeServiceClient
import com.valhallagame.wardrobeserviceclient.message.AddWardrobeItemParameter
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RecipeService(
        @Autowired val recipeRepository: RecipeRepository,
        @Autowired val currencyServiceClient: CurrencyServiceClient,
        @Autowired val wardrobeServiceClient: WardrobeServiceClient,
        @Autowired val characterServiceClient: CharacterServiceClient,
        @Autowired val rabbitTemplate: RabbitTemplate
) {

    fun getRecipes(characterName: String?): List<Recipe> {
        return recipeRepository.findByCharacterName(characterName)
    }

    fun getUnclaimedRecipes(characterName: String): List<Recipe> {
        return recipeRepository.findByCharacterNameAndClaimed(characterName, false)
    }

    fun addRecipe(characterName: String, recipeEnum: WardrobeItem) {
        val characterResp = characterServiceClient.getCharacter(characterName)
        val characterOpt = characterResp.get()
        if (!characterOpt.isPresent) {
            throw MissingCharacterException("could not find character with $characterName")
        }
        val found = recipeRepository.findByCharacterNameAndRecipeName(characterName, recipeEnum.name)
        if (found != null) {
            throw IllegalArgumentException("already added recipe $recipeEnum for $characterName")
        }

        recipeRepository.save(Recipe(null, characterName, recipeEnum.name, false))
        rabbitTemplate.convertAndSend(
                RabbitMQRouting.Exchange.RECIPE.name,
                RabbitMQRouting.Recipe.ADD.name,
                NotificationMessage(characterOpt.get().ownerUsername, "Gained $recipeEnum for $characterName")
                        .withData("recipe", recipeEnum.name)
        )
    }

    @Transactional
    @Throws(IllegalAccessException::class, LockCurrenciesException::class)
    fun claimRecipe(characterName: String, recipeEnum: WardrobeItem, currencies: Map<CurrencyType, Int>) {

        val recipe = recipeRepository.findByCharacterNameAndRecipeName(characterName, recipeEnum.name)
                ?: throw IllegalArgumentException("character $characterName does not have ${recipeEnum.name}")

        val lockResp = currencyServiceClient.lockCurrencies(characterName, currencies.map { ent ->
            LockCurrencyParameter.Currency(ent.key, ent.value)
        })

        if (!lockResp.isOk) {
            throw LockCurrenciesException(lockResp.statusCode, "lock currencies failed with reason ${lockResp.errorMessage}")
        }

        val resultListOpt: Optional<MutableList<LockedCurrencyResult>> = lockResp.get()
        if (!resultListOpt.isPresent || resultListOpt.get().isEmpty()) {
            throw LockCurrenciesException(HttpStatus.INTERNAL_SERVER_ERROR, "lock currencies failed as there was a strange response from lockCurrencies: $lockResp")
        }

        // All locked currencies will get the same lock id
        // so we only take one from the first one
        val lockingId = resultListOpt.get()[0].lockingId

        val wardrobeItemResp = wardrobeServiceClient.addWardrobeItem(AddWardrobeItemParameter(characterName, recipeEnum))

        if (!wardrobeItemResp.isOk) {
            val abortResp = currencyServiceClient.abortLockedCurrencies(lockingId)
            if (abortResp.isOk) {
                throw LockCurrenciesException(wardrobeItemResp.statusCode, "unlock failed with message: ${wardrobeItemResp.errorMessage}")
            }
            throw LockCurrenciesException(wardrobeItemResp.statusCode,
                    "unlock failed with wardrobe message: ${wardrobeItemResp.errorMessage} and abort currency ${abortResp.errorMessage}")
        }

        recipe.claimed = true
        recipeRepository.save(recipe)

        val commitResp = currencyServiceClient.commitLockedCurrencies(lockingId)

        if (!commitResp.isOk) {
            throw RuntimeException("commit failed with message: ${commitResp.errorMessage}")
        }

        rabbitTemplate.convertAndSend(
                RabbitMQRouting.Exchange.RECIPE.name,
                RabbitMQRouting.Recipe.REMOVE.name,
                NotificationMessage(
                        characterName,
                        "Claimed $recipeEnum for $characterName")
                        .withData("recipe", recipeEnum.name)
        )

    }

    @Transactional
    fun removeRecipe(characterName: String, recipeEnum: WardrobeItem) {
        recipeRepository.deleteByCharacterNameAndRecipeName(characterName, recipeEnum.name)

        rabbitTemplate.convertAndSend(
                RabbitMQRouting.Exchange.RECIPE.name,
                RabbitMQRouting.Recipe.REMOVE.name,
                NotificationMessage(
                        characterName,
                        "Removed $recipeEnum for $characterName")
                        .withData("recipe", recipeEnum.name)
        )
    }

    @Transactional
    fun deleteRecipes(characterName: String) {
        recipeRepository.deleteByCharacterName(characterName)
    }

    fun addRecipeFromFeat(characterName: String, featName: FeatName) {
        when (featName) {
            FeatName.MISSVEDEN_SAXUMPHILE -> {
                addRecipe(characterName, SMALL_SHIELD)
            }
            FeatName.MISSVEDEN_DENIED -> {
                addRecipe(characterName, SHAMANS_PELT)
            }
            FeatName.MISSVEDEN_TREADING_WITH_GREAT_CARE -> {
                addRecipe(characterName, RANGERS_SAFEGUARD)
            }
            FeatName.MISSVEDEN_NO_LESSER_FOES -> {
                addRecipe(characterName, LARGE_SHIELD)
            }
            FeatName.MISSVEDEN_A_CRYSTAL_CLEAR_MYSTERY -> {
                addRecipe(characterName, DAGGER)
            }
            FeatName.FREDSTORP_THIEF_OF_THIEVES -> {
                addRecipe(characterName, HUNTING_BOW)
            }
            FeatName.FREDSTORP_SPEEDRUNNER -> {
                addRecipe(characterName, LONGSWORD)
            }
            FeatName.FREDSTORP_GAMBLER -> {
                addRecipe(characterName, MAIL_ARMOR)
            }
            FeatName.FREDSTORP_ANORECTIC -> {
                addRecipe(characterName, CLOTH_ARMOR)
            }
            FeatName.FREDSTORP_NEVER_BEEN_BETTER -> {
                addRecipe(characterName, GREATAXE)
            }
            FeatName.FREDSTORP_EXTRACTOR -> {
                addRecipe(characterName, HAND_AXE)
            }
            FeatName.FREDSTORP_EXTERMINATOR -> {
                addRecipe(characterName, STEEL_SHIELD)
            }
            FeatName.MISSVEDEN_THE_CHIEFTAINS_DEMISE -> {
                // Does not give recipe but traits!
            }
        }
    }
}

class MissingCharacterException(message: String) : ApiException(HttpStatus.NOT_FOUND, message)
class LockCurrenciesException(httpStatus: HttpStatus, errorMessage: String) : ApiException(httpStatus, errorMessage)