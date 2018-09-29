package com.valhallagame.valhalla.recipeserviceserver.service

import com.valhallagame.common.RestResponse
import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.currencyserviceclient.message.LockCurrencyParameter.Currency
import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import com.valhallagame.valhalla.recipeserviceserver.repository.RecipeRepository
import com.valhallagame.wardrobeserviceclient.WardrobeServiceClient
import com.valhallagame.wardrobeserviceclient.message.AddWardrobeItemParameter
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class RecipeService(
        @Autowired val recipeRepository: RecipeRepository,
        @Autowired val currencyServiceClient: CurrencyServiceClient,
        @Autowired val wardrobeServiceClient: WardrobeServiceClient
) {

    fun getUnclaimedRecipes(characterName: String): List<Recipe> {
        return recipeRepository.findByCharacterNameAndClaimed(characterName, false)
    }

    fun addRecipe(characterName: String, recipe: String) {
        return recipeRepository.addRecipe(characterName, recipe)
    }

    @Transactional
    fun claimRecipe(characterName: String, recipeEnum: WardrobeItem, currencies: List<Currency>) {

        val recipe = recipeRepository.findByCharacterNameAndRecipeName(characterName, recipeEnum.name)
                ?: throw IllegalAccessError("character $characterName does not have $recipeEnum")

        val id = lockCurrencies(characterName, currencies)
        val wardrobeItemResp = wardrobeServiceClient.addWardrobeItem(AddWardrobeItemParameter(characterName, recipeEnum))

        if (!wardrobeItemResp.isOk) {
            val abortResp = abortLockedCurrencies(id)
            throw RuntimeException(if (abortResp.isOk) {
                "unlock failed with message: ${wardrobeItemResp.errorMessage}"
            } else {
                "unlock failed with wardrobe message: ${wardrobeItemResp.errorMessage} and abort currency ${abortResp.errorMessage}"
            })
        }

        val commitResp = commitLockedCurrencies(id)
        if (!commitResp.isOk) {
            throw RuntimeException("commit failed with message: ${commitResp.errorMessage}")
        }
        recipe.claimed = true
        recipeRepository.save(recipe)
    }

    fun deleteRecipes(characterName: String) {
        recipeRepository.deleteByCharacterName(characterName)
    }
}

private fun abortLockedCurrencies(id: Any): RestResponse<String> {
    TODO("not implemented $id")
}

private fun commitLockedCurrencies(id: Any): RestResponse<String> {
    TODO("not implemented $id")
}

private fun lockCurrencies(characterName: String, currencies: List<Currency>): RestResponse<Int> {
    TODO("not implemented $characterName $currencies")
}
