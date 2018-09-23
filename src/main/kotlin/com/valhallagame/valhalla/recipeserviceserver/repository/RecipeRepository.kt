package com.valhallagame.valhalla.recipeserviceserver.repository

import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<Recipe, Long> {
    fun findRecipeByCharacterNameAndType(characterName: String): Recipe?
    fun findRecipeByCharacterName(characterName: String): List<Recipe>
}