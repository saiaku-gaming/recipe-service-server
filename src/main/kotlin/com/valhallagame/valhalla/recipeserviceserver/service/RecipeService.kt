package com.valhallagame.valhalla.recipeserviceserver.service

import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import com.valhallagame.valhalla.recipeserviceserver.repository.RecipeRepository
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class RecipeService(val recipeRepository: RecipeRepository) {
    fun getUnclaimedRecipes(characterName: String): Array<Recipe> {
        return recipeRepository.findByCharacterNameAndClaimed(characterName, false)
    }

    fun addRecipe(characterName: String, recipe: String) {
        return recipeRepository.addRecipe(characterName, recipe)
    }

    @Transactional
    fun claimRecipe(characterName: String, recipeEnum: WardrobeItem) {
        val recipe = recipeRepository.findByCharacterNameAndRecipeName(characterName, recipeEnum.name)
        if (recipe == null) {
            throw IllegalAccessError("character $characterName does not have $recipe")
        }
        recipe.claimed = true
        recipeRepository.save(recipe)

        TODO("Call rabbitMQ so that feats knows about this")
    }

    fun deleteRecipes(characterName: String) {
        recipeRepository.deleteByCharacterName(characterName)
    }
}