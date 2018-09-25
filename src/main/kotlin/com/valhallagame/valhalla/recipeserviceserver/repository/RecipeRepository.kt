package com.valhallagame.valhalla.recipeserviceserver.repository

import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface RecipeRepository : JpaRepository<Recipe, Long> {

    fun findByCharacterNameAndClaimed(characterName: String, unclaimed: Boolean): Array<Recipe>

    @Query(value = "INSERT INTO recipe (character_name, recipe_name, claimed) VALUES" +
            " (:character_name, :recipe_name, true)", nativeQuery = true)
    fun addRecipe(@Param("character_name") characterName: String, @Param("recipe_name") recipe: String)

    fun findByCharacterNameAndRecipeName(characterName: String, name: String): Recipe?
    fun deleteByCharacterName(characterName: String)


}