package com.valhallagame.valhalla.recipeserviceserver.repository

import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<Recipe, Long> {
    fun findByCharacterNameAndClaimed(characterName: String, claimed: Boolean): List<Recipe>
    fun findByCharacterNameAndRecipeName(characterName: String, name: String): Recipe?
    fun deleteByCharacterName(characterName: String)
    fun findByCharacterName(characterName: String?): List<Recipe>
    fun deleteByCharacterNameAndRecipeName(characterName: String, name: String)
}