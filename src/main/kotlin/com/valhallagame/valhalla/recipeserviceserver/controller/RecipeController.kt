package com.valhallagame.valhalla.recipeserviceserver.controller

import com.fasterxml.jackson.databind.JsonNode
import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.common.JS
import com.valhallagame.recipeserviceclient.message.AddRecipeParameter
import com.valhallagame.recipeserviceclient.message.ClaimRecipeParameter
import com.valhallagame.recipeserviceclient.message.GetRecipesParameter
import com.valhallagame.valhalla.recipeserviceserver.service.RecipeService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import javax.validation.Valid

@Controller
@RequestMapping(path = ["/v1/recipe"])
class RecipeController(private val recipeService: RecipeService, private val characterServiceClient: CharacterServiceClient) {

    private val logger = LoggerFactory.getLogger(RecipeController::class.java)

    @ResponseBody
    @PostMapping("/add")
    fun add(@Valid @RequestBody input: AddRecipeParameter): ResponseEntity<JsonNode> {
        val addedCurrency = recipeService.addRecipe(input.characterName, input.recipe)
        return JS.message(HttpStatus.OK, addedCurrency)
    }

    @ResponseBody
    @PostMapping("/get")
    fun get(@Valid @RequestBody input: GetRecipesParameter): ResponseEntity<JsonNode> {
        val selectedCharacterResp = characterServiceClient.getSelectedCharacter(input.username)
        if (selectedCharacterResp.isOk && selectedCharacterResp.get().isPresent) {
            val recipes = recipeService.getUnclaimedRecipes(selectedCharacterResp.get().get().characterName)
            return JS.message(HttpStatus.OK, recipes)
        }
        return JS.message(HttpStatus.BAD_GATEWAY, "Could not get selected character for ${input.username}")
    }

    @ResponseBody
    @PostMapping("/claim")
    fun claim(@Valid @RequestBody input: ClaimRecipeParameter): ResponseEntity<JsonNode> {
        val addedCurrency = recipeService.claimRecipe(input.characterName, input.recipe)
        return JS.message(HttpStatus.OK, addedCurrency)
    }
}