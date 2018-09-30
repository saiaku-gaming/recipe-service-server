package com.valhallagame.valhalla.recipeserviceserver.controller

import com.valhallagame.characterserviceclient.model.CharacterData
import com.valhallagame.common.RestResponse
import com.valhallagame.recipeserviceclient.message.GetRecipesParameter
import com.valhallagame.valhalla.recipeserviceserver.config.MockClientConfig
import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import com.valhallagame.valhalla.recipeserviceserver.repository.RecipeRepository
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.test.FlywayTestExecutionListener
import org.flywaydb.test.annotation.FlywayTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.reset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("in-memory-db", "mock-client", "test")
@TestExecutionListeners(listeners = [DependencyInjectionTestExecutionListener::class, FlywayTestExecutionListener::class])
@FlywayTest
class RecipeControllerIntegrationTest {

    val username = "existinguser"
    val characterName = "existingcharacter"
    val recipeName = WardrobeItem.CLOTH_ARMOR.name

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var recipeRepository: RecipeRepository

    @BeforeEach
    fun setup() {
        //As this is a static client, lets make a fresh mock for every test
        reset(MockClientConfig.characterServiceClientMock)
        val characterData = CharacterData(
                username,
                characterName.toUpperCase(),
                characterName,
                "",
                "",
                ""
        )
        val restResponse = RestResponse<CharacterData>(HttpStatus.OK, Optional.of(characterData))

        `when`(MockClientConfig.characterServiceClientMock.getSelectedCharacter(username)).thenReturn(restResponse)
    }

    @Test
    fun add() {
        recipeRepository.save(Recipe(null, characterName, recipeName, false))
        val param = GetRecipesParameter(username)
        val postForEntity = restTemplate.postForEntity<Array<Recipe>>("/v1/recipe/get", param)
        assertThat(postForEntity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(postForEntity.body).hasSize(1)
    }

}