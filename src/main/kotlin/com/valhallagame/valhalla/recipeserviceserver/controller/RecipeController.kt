package com.valhallagame.valhalla.recipeserviceserver.controller

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping(path = ["/v1/recipe"])
class RecipeController {
    private val logger = LoggerFactory.getLogger(RecipeController::class.java)
}