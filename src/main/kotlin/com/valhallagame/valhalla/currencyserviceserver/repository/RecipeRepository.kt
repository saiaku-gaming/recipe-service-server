package com.valhallagame.valhalla.currencyserviceserver.repository

import com.valhallagame.currencyserviceclient.model.CurrencyType
import com.valhallagame.valhalla.currencyserviceserver.model.Recipe
import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<Recipe, Long> {
    fun findCurrencyByCharacterNameAndType(characterName: String, type: CurrencyType): Recipe?
    fun findCurrencyByCharacterName(characterName: String): List<Recipe>
}