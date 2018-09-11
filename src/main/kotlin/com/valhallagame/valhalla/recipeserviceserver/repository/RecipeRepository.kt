package com.valhallagame.valhalla.recipeserviceserver.repository

import com.valhallagame.currencyserviceclient.model.CurrencyType
import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<Recipe, Long> {
    fun findCurrencyByCharacterNameAndType(characterName: String, type: CurrencyType): Recipe?
    fun findCurrencyByCharacterName(characterName: String): List<Recipe>
}