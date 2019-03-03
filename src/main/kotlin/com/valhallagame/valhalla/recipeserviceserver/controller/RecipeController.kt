package com.valhallagame.valhalla.recipeserviceserver.controller

import com.fasterxml.jackson.databind.JsonNode
import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.common.JS
import com.valhallagame.recipeserviceclient.message.AddRecipeParameter
import com.valhallagame.recipeserviceclient.message.ClaimRecipeParameter
import com.valhallagame.recipeserviceclient.message.GetRecipesParameter
import com.valhallagame.recipeserviceclient.message.RemoveRecipeParameter
import com.valhallagame.recipeserviceclient.model.RecipeData
import com.valhallagame.valhalla.recipeserviceserver.service.RecipeService
import com.valhallagame.wardrobeserviceclient.message.WardrobeItem
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping(path = ["/v1/recipe"])
class RecipeController(private val recipeService: RecipeService, private val characterServiceClient: CharacterServiceClient) {
    companion object {
        private val logger = LoggerFactory.getLogger(RecipeController::class.java)
    }

    @PostMapping("/add")
    fun add(@Valid @RequestBody input: AddRecipeParameter): ResponseEntity<JsonNode> {
        logger.info("Add called with {}", input)
        recipeService.addRecipe(input.characterName, input.recipe)
        return JS.message(HttpStatus.OK, "Added recipe")
    }

    @PostMapping("/get")
    fun get(@Valid @RequestBody input: GetRecipesParameter): ResponseEntity<JsonNode> {
        logger.info("Get called with {}", input)
        val selectedCharacterResp = characterServiceClient.getSelectedCharacter(input.username)
        if (selectedCharacterResp.isOk && selectedCharacterResp.get().isPresent) {
            val recipes = recipeService.getRecipes(selectedCharacterResp.get().get().characterName)
            val recipeData = recipes.map {
                RecipeData(it.characterName, WardrobeItem.valueOf(it.recipeName), it.claimed)
            }
            return JS.message(HttpStatus.OK, recipeData)
        }
        return JS.message(HttpStatus.BAD_GATEWAY, "Could not get selected character for ${input.username}")
    }

    @PostMapping("/claim")
    fun claim(@Valid @RequestBody input: ClaimRecipeParameter): ResponseEntity<JsonNode> {
        logger.info("Claim called with {}", input)
        return JS.message(HttpStatus.OK, recipeService.claimRecipe(input.characterName, input.recipe, input.currencies))
    }

    @PostMapping("/remove")
    fun remove(@Valid @RequestBody input: RemoveRecipeParameter): ResponseEntity<JsonNode> {
        logger.info("Remove called with {}", input)
        recipeService.removeRecipe(input.characterName, input.recipe)
        return JS.message(HttpStatus.OK, "Recipe removed")
    }
}