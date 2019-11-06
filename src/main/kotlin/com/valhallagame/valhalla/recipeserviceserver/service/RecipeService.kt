package com.valhallagame.valhalla.recipeserviceserver.service

import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.common.exceptions.ApiException
import com.valhallagame.common.rabbitmq.NotificationMessage
import com.valhallagame.common.rabbitmq.RabbitMQRouting
import com.valhallagame.common.rabbitmq.RabbitSender
import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.currencyserviceclient.message.LockCurrencyParameter
import com.valhallagame.currencyserviceclient.message.LockedCurrencyResult
import com.valhallagame.currencyserviceclient.model.CurrencyType
import com.valhallagame.featserviceclient.message.FeatName
import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import com.valhallagame.valhalla.recipeserviceserver.repository.RecipeRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RecipeService(
        @Autowired val recipeRepository: RecipeRepository,
        @Autowired val currencyServiceClient: CurrencyServiceClient,
        @Autowired val characterServiceClient: CharacterServiceClient,
        @Autowired val rabbitSender: RabbitSender
) {
    companion object {
        private val logger = LoggerFactory.getLogger(RecipeService::class.java)
    }

    fun getRecipes(characterName: String?): List<Recipe> {
        logger.info("Getting recipes for {}", characterName)
        return recipeRepository.findByCharacterName(characterName)
    }

    fun getUnclaimedRecipes(characterName: String): List<Recipe> {
        logger.info("Getting unclaimed recipes for {}", characterName)
        return recipeRepository.findByCharacterNameAndClaimed(characterName, false)
    }

    fun addRecipe(characterName: String, recipeName: String) {
        logger.info("Adding recipe for {} recipe {}", characterName, recipeName)
        val characterResp = characterServiceClient.getCharacter(characterName)
        val characterOpt = characterResp.get()
        if (!characterOpt.isPresent) {
            throw MissingCharacterException("could not find character with $characterName")
        }
        val found = recipeRepository.findByCharacterNameAndRecipeName(characterName, recipeName)
        if (found != null) {
            throw IllegalArgumentException("already added recipe $recipeName for $characterName")
        }

        recipeRepository.save(Recipe(null, characterName, recipeName, false))
        rabbitSender.sendMessage(
                RabbitMQRouting.Exchange.RECIPE,
                RabbitMQRouting.Recipe.ADD.name,
                NotificationMessage(characterOpt.get().ownerUsername, "Gained $recipeName for $characterName")
                        .withData("recipe", recipeName)
        )
    }

    @Transactional
    @Throws(IllegalAccessException::class, LockCurrenciesException::class)
    fun claimRecipe(characterName: String, recipeName: String, currencies: Map<CurrencyType, Int>) {
        logger.info("Claiming recipe for {} recipe {} currencies {}", characterName, recipeName, currencies)
        val recipe = recipeRepository.findByCharacterNameAndRecipeName(characterName, recipeName)
                ?: throw IllegalArgumentException("character $characterName does not have $recipeName")

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

        recipe.claimed = true
        recipeRepository.save(recipe)

        val commitResp = currencyServiceClient.commitLockedCurrencies(lockingId)

        if (!commitResp.isOk) {
            throw RuntimeException("commit failed with message: ${commitResp.errorMessage}")
        }

        rabbitSender.sendMessage(
                RabbitMQRouting.Exchange.RECIPE,
                RabbitMQRouting.Recipe.REMOVE.name,
                NotificationMessage(
                        characterName,
                        "Claimed $recipeName for $characterName")
                        .withData("recipe", recipeName)
        )

    }

    @Transactional
    fun removeRecipe(characterName: String, recipeName: String) {
        logger.info("Removing recipe {} for {}", recipeName, characterName)
        recipeRepository.deleteByCharacterNameAndRecipeName(characterName, recipeName)

        rabbitSender.sendMessage(
                RabbitMQRouting.Exchange.RECIPE,
                RabbitMQRouting.Recipe.REMOVE.name,
                NotificationMessage(
                        characterName,
                        "Removed $recipeName for $characterName")
                        .withData("recipe", recipeName)
        )
    }

    @Transactional
    fun deleteRecipes(characterName: String) {
        logger.info("Deleting recipes for {}", characterName)
        recipeRepository.deleteByCharacterName(characterName)
    }

    fun addRecipeFromFeat(characterName: String, featName: FeatName) {
        logger.info("Adding recipe from feat {} for {}", featName, characterName)
        when (featName) {
            FeatName.MISSVEDEN_THE_CHIEFTAINS_DEMISE -> {
                addRecipe(characterName, "WARLOCKS_BOOTS")
                addRecipe(characterName, "LEATHER_STRIDERS")
                addRecipe(characterName, "HARDENED_GREAVES")
            }
            FeatName.MISSVEDEN_DENIED -> {
                addRecipe(characterName, "WARHORN")
            }
            FeatName.MISSVEDEN_TREADING_WITH_GREAT_CARE -> {
                addRecipe(characterName, "LANTERN")
            }
            FeatName.FREDSTORP_THIEF_OF_THIEVES -> {
                addRecipe(characterName, "SILKEN_WRAPS")
                addRecipe(characterName, "LEATHER_BRACERS")
                addRecipe(characterName, "METAL_BRACERS")
            }
            FeatName.FREDSTORP_GAMBLER -> {
                addRecipe(characterName, "PARRY_DAGGER")
            }
            FeatName.FREDSTORP_SPEEDRUNNER -> {
                addRecipe(characterName, "WOODEN_STAFF")
            }
            FeatName.FREDSTORP_NEVER_BEEN_BETTER -> {
                addRecipe(characterName, "SPELL_BOOK")
            }
            FeatName.HJUO_EXPLORER -> {
                addRecipe(characterName, "SUMMONERS_KILT")
                addRecipe(characterName, "TRAPPERS_PANTS")
                addRecipe(characterName, "STUDDED_LEATHER_PANTS")
            }
            FeatName.GRYNMAS_LAIR_JOTUNN_SLAYER -> {
                addRecipe(characterName, "SUMMONERS_TUNIC")
                addRecipe(characterName, "TRAPPERS_PELTS")
                addRecipe(characterName, "MAIL_HAUBERK")
            }
            else -> {}
        }
    }
}

class MissingCharacterException(message: String) : ApiException(HttpStatus.NOT_FOUND, message)
class LockCurrenciesException(httpStatus: HttpStatus, errorMessage: String) : ApiException(httpStatus, errorMessage)