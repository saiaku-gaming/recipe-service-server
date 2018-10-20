package com.valhallagame.valhalla.recipeserviceserver

import com.valhallagame.common.DefaultServicePortMappings
import com.valhallagame.common.exceptions.ApiResponseExceptionHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Profile
import java.io.FileInputStream
import java.util.*

@SpringBootApplication
@ComponentScan(
        basePackages = ["com.valhallagame.valhalla.recipeserviceserver"],
        basePackageClasses = [ApiResponseExceptionHandler::class]
)
class RecipeApp {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(RecipeApp::class.java)
    }

    @Bean
    @Profile("!test")
    fun customizer() = WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        it.setPort(DefaultServicePortMappings.RECIPE_SERVICE_PORT)
    }
}

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        if (RecipeApp.logger.isInfoEnabled) {
            RecipeApp.logger.info("Args passed in: {}", Arrays.asList(args))
        }

        args.forEach {
            val split = it.split("=")

            if (split.size == 2) {
                System.getProperties().setProperty(split[0], split[1])
            } else {
                FileInputStream(args[0]).use { inputStream ->
                    System.getProperties().load(inputStream)
                }
            }
        }
    } else {
        RecipeApp.logger.info("No args passed to main")
    }

    runApplication<RecipeApp>(*args)
}
