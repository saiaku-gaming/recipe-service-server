package com.valhallagame.valhalla.recipeserviceserver.service

import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.currencyserviceclient.message.LockCurrencyParameter.Currency
import com.valhallagame.currencyserviceclient.message.LockedCurrencyResult
import com.valhallagame.featserviceclient.message.FeatName
import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import com.valhallagame.valhalla.recipeserviceserver.repository.RecipeRepository
import com.valhallagame.wardrobeserviceclient.WardrobeServiceClient
import com.valhallagame.wardrobeserviceclient.message.AddWardrobeItemParameter
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class RecipeService(
        @Autowired val recipeRepository: RecipeRepository,
        @Autowired val currencyServiceClient: CurrencyServiceClient,
        @Autowired val wardrobeServiceClient: WardrobeServiceClient
) {

    fun getRecipes(characterName: String?): List<Recipe> {
        return recipeRepository.findByCharacterName(characterName)
    }

    fun getUnclaimedRecipes(characterName: String): List<Recipe> {
        return recipeRepository.findByCharacterNameAndClaimed(characterName, false)
    }

    fun addRecipe(characterName: String, recipeEnum: WardrobeItem) {
        recipeRepository.save(Recipe(null, characterName, recipeEnum.name, false))
    }

    @Transactional
    fun claimRecipe(characterName: String, recipeEnum: WardrobeItem, currencies: List<Currency>) {

        val recipe = recipeRepository.findByCharacterNameAndRecipeName(characterName, recipeEnum.name)
                ?: throw IllegalAccessException("character $characterName does not have $recipeEnum")

        val lockResp = currencyServiceClient.lockCurrencies(characterName, currencies)
        if (!lockResp.isOk) {
            throw RuntimeException("lock currencies failed with reason ${lockResp.errorMessage}")
        }
        val resultListOpt: Optional<MutableList<LockedCurrencyResult>> = lockResp.get()
        if (!resultListOpt.isPresent || resultListOpt.get().isEmpty()) {
            throw RuntimeException("lock currencies failed as there was a strange response from lockCurrencies: $lockResp")
        }

        // All locked currencies will get the same lock id
        // so we only take one from the first one
        val lockingId = resultListOpt.get()[0].lockingId

        val wardrobeItemResp = wardrobeServiceClient.addWardrobeItem(AddWardrobeItemParameter(characterName, recipeEnum))

        if (!wardrobeItemResp.isOk) {
            val abortResp = currencyServiceClient.abortLockedCurrencies(lockingId)
            throw RuntimeException(if (abortResp.isOk) {
                "unlock failed with message: ${wardrobeItemResp.errorMessage}"
            } else {
                "unlock failed with wardrobe message: ${wardrobeItemResp.errorMessage} and abort currency ${abortResp.errorMessage}"
            })
        }

        recipe.claimed = true
        recipeRepository.save(recipe)

        val commitResp = currencyServiceClient.commitLockedCurrencies(lockingId)
        if (!commitResp.isOk) {
            throw RuntimeException("commit failed with message: ${commitResp.errorMessage}")
        }
    }

    fun deleteRecipes(characterName: String) {
        recipeRepository.deleteByCharacterName(characterName)
    }

    fun addRecipeFromFeat(characterName: String, featNameString: String) {
        val featName = FeatName.valueOf(featNameString)
        when (featName) {
            FeatName.MISSVEDEN_SAXUMPHILE -> {
                addRecipe(characterName, SMALL_SHIELD)
                return
            }
            FeatName.MISSVEDEN_DENIED -> {
                addRecipe(characterName, SHAMANS_PELT)
                return
            }
            FeatName.MISSVEDEN_TREADING_WITH_GREAT_CARE -> {
                addRecipe(characterName, RANGERS_SAFEGUARD)
                return
            }
            FeatName.MISSVEDEN_NO_LESSER_FOES -> {
                addRecipe(characterName, LARGE_SHIELD)
                return
            }
            FeatName.MISSVEDEN_A_CRYSTAL_CLEAR_MYSTERY -> {
                addRecipe(characterName, DAGGER)
                return
            }
            FeatName.FREDSTORP_THIEF_OF_THIEVES -> {
                addRecipe(characterName, HUNTING_BOW)
                return
            }
            FeatName.FREDSTORP_SPEEDRUNNER -> {
                addRecipe(characterName, LONGSWORD)
                return
            }
            FeatName.FREDSTORP_GAMBLER -> {
                addRecipe(characterName, MAIL_ARMOR)
                return
            }
            FeatName.FREDSTORP_ANORECTIC -> {
                addRecipe(characterName, CLOTH_ARMOR)
                return
            }
            FeatName.FREDSTORP_NEVER_BEEN_BETTER -> {
                addRecipe(characterName, GREATAXE)
                return
            }
            FeatName.FREDSTORP_EXTRACTOR -> {
                addRecipe(characterName, HAND_AXE)
                return
            }
            FeatName.FREDSTORP_EXTERMINATOR -> {
                addRecipe(characterName, STEEL_SHIELD)
                return
            }
            FeatName.EINHARJER_SLAYER -> TODO("This should be removed!")
            FeatName.TRAINING_EFFICIENCY -> TODO("This should be removed")
            FeatName.MISSVEDEN_THE_CHIEFTAINS_DEMISE -> {
                // Adds traits instead
            }
        }
    }


}