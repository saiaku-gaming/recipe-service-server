package com.valhallagame.valhalla.recipeserviceserver.service

import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.characterserviceclient.model.CharacterData
import com.valhallagame.common.RestResponse
import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.currencyserviceclient.message.LockCurrencyParameter
import com.valhallagame.currencyserviceclient.message.LockedCurrencyResult
import com.valhallagame.currencyserviceclient.model.CurrencyType
import com.valhallagame.featserviceclient.message.FeatName
import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import com.valhallagame.valhalla.recipeserviceserver.repository.RecipeRepository
import com.valhallagame.wardrobeserviceclient.WardrobeServiceClient
import com.valhallagame.wardrobeserviceclient.message.AddWardrobeItemParameter
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.*


class RecipeServiceTest {

    private lateinit var recipeRepository: RecipeRepository
    private lateinit var currencyServiceClient: CurrencyServiceClient
    private lateinit var wardrobeServiceClient: WardrobeServiceClient
    private lateinit var rabbitTemplate: RabbitTemplate
    private lateinit var characterServiceClient: CharacterServiceClient
    private lateinit var recipeService: RecipeService

    private val characterName = "nissecharacter"
    private val characterRestResponse = RestResponse<CharacterData>(HttpStatus.OK, Optional.of(CharacterData(
            "username", characterName, characterName, "","",""
    )))

    @BeforeEach
    fun mock() {
        //TODO why the fuck do it need to do it like this and not just use @Mock
        recipeRepository = Mockito.mock(RecipeRepository::class.java)
        currencyServiceClient = Mockito.mock(CurrencyServiceClient::class.java)
        wardrobeServiceClient = Mockito.mock(WardrobeServiceClient::class.java)
        characterServiceClient = Mockito.mock(CharacterServiceClient::class.java)
        rabbitTemplate = Mockito.mock(RabbitTemplate::class.java)
        recipeService = RecipeService(recipeRepository, currencyServiceClient, wardrobeServiceClient, characterServiceClient, rabbitTemplate)

        `when`(characterServiceClient.getCharacter(characterName)).thenReturn(characterRestResponse)
    }

    @Test
    fun getUnclaimedRecipes() {
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
        recipeService.addRecipe(characterName, WardrobeItem.CLOTH_ARMOR)
        verify(recipeRepository).save(Recipe(null, characterName, WardrobeItem.CLOTH_ARMOR.name, false))
    }

    @Test
    fun addRecipeByNotification() {
        recipeService.addRecipeFromFeat(characterName, FeatName.FREDSTORP_SPEEDRUNNER)
        `when`(recipeRepository.findByCharacterNameAndRecipeName(characterName, WardrobeItem.LONGSWORD.name)).thenReturn(null)

        verify(recipeRepository).findByCharacterNameAndRecipeName(characterName, WardrobeItem.LONGSWORD.name)
        verify(recipeRepository).save(Recipe(null, characterName, WardrobeItem.LONGSWORD.name, false))
        verifyZeroInteractions(recipeRepository)
    }

    @Test
    fun claimRecipe() {
        val currencies = mapOf(
                CurrencyType.GOLD to 10,
                CurrencyType.IRON to 20
        )

        `when`(recipeRepository.findByCharacterNameAndRecipeName(characterName, WardrobeItem.CLOTH_ARMOR.name))
                .thenReturn(Recipe(0, characterName, WardrobeItem.CLOTH_ARMOR.name, false))
        val lockingId = "lockingId"
        val lockedResult = listOf(
                LockedCurrencyResult(0, characterName, CurrencyType.GOLD, 10, lockingId, Instant.EPOCH),
                LockedCurrencyResult(1, characterName, CurrencyType.IRON, 20, lockingId, Instant.EPOCH)
        )
        `when`(currencyServiceClient.lockCurrencies(characterName, currencies.map { ent ->
            LockCurrencyParameter.Currency(ent.key, ent.value)
        })).thenReturn(RestResponse<List<LockedCurrencyResult>>(HttpStatus.OK, Optional.of(lockedResult)))

        `when`(currencyServiceClient.commitLockedCurrencies(lockingId))
                .thenReturn(RestResponse<String>(HttpStatus.OK, Optional.of("Eh, some response?")))

        `when`(wardrobeServiceClient.addWardrobeItem(AddWardrobeItemParameter(characterName, WardrobeItem.CLOTH_ARMOR)))
                .thenReturn(RestResponse<String>(HttpStatus.OK, Optional.of("Eh, some response?")))

        recipeService.claimRecipe(characterName, WardrobeItem.CLOTH_ARMOR, currencies)

        verify(recipeRepository).save(any())
    }

    @Test
    fun deleteRecipes() {
        recipeService.deleteRecipes(characterName)
        verify(recipeRepository).deleteByCharacterName(characterName)
    }
}