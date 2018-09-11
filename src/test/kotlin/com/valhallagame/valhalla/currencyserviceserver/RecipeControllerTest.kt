package com.valhallagame.valhalla.currencyserviceserver

import com.valhallagame.valhalla.currencyserviceserver.controller.RecipeController
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@WebMvcTest(RecipeController::class)
@ActiveProfiles("test")
class RecipeControllerTest {
}