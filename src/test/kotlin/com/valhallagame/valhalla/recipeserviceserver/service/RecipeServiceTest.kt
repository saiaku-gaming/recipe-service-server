package com.valhallagame.valhalla.recipeserviceserver.service

import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.currencyserviceclient.message.LockCurrencyParameter
import com.valhallagame.currencyserviceclient.model.CurrencyType
import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import com.valhallagame.valhalla.recipeserviceserver.repository.RecipeRepository
import com.valhallagame.wardrobeserviceclient.WardrobeServiceClient
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify


class RecipeServiceTest {

    @BeforeEach
    fun mock() {
        //TODO why the fuck do it need to do it like this and not just use @Mock
        recipeRepository = Mockito.mock(RecipeRepository::class.java)
        currencyServiceClient = Mockito.mock(CurrencyServiceClient::class.java)
        wardrobeServiceClient = Mockito.mock(WardrobeServiceClient::class.java)
    }

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var currencyServiceClient: CurrencyServiceClient
    private lateinit var wardrobeServiceClient: WardrobeServiceClient

    private val characterName = "nissecharacter"

    @Test
    fun getUnclaimedRecipes() {
        val recipeService = RecipeService(recipeRepository, currencyServiceClient, wardrobeServiceClient)
        val recipeOne = Recipe(0, characterName, "do", false)
        val recipeTwo = Recipe(0, characterName, "re", false)
        val recipeThree = Recipe(0, characterName, "me", false)
        `when`(recipeRepository.findByCharacterNameAndClaimed(characterName, false))
                .thenReturn(listOf(recipeOne, recipeTwo, recipeThree))
        val unclaimedRecipes = recipeService.getUnclaimedRecipes(characterName)
        assertThat(unclaimedRecipes).hasSize(3)
    }

    @Test
    fun addRecipe() {
        val recipeService = RecipeService(recipeRepository, currencyServiceClient, wardrobeServiceClient)
        recipeService.addRecipe(characterName, "do")
        verify(recipeRepository).addRecipe(characterName, "do")
    }

    @Test
    fun claimRecipe() {
        val recipeService = RecipeService(recipeRepository, currencyServiceClient, wardrobeServiceClient)
        val currencies = listOf(
                LockCurrencyParameter.Currency(CurrencyType.GOLD, 10),
                LockCurrencyParameter.Currency(CurrencyType.IRON, 10)
        )
        `when`(recipeRepository.findByCharacterNameAndRecipeName(characterName, WardrobeItem.CLOTH_ARMOR.name))
                .thenReturn(Recipe(0, characterName, WardrobeItem.CLOTH_ARMOR.name, false))

        recipeService.claimRecipe(characterName, WardrobeItem.CLOTH_ARMOR, currencies)

        TODO("figure out what to assert")
    }

    @Test
    fun deleteRecipes() {
    }
}