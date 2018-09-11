package com.valhallagame.valhalla.recipeserviceserver

import com.valhallagame.common.DefaultServicePortMappings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory
import org.springframework.context.annotation.Bean
import java.io.FileInputStream
import java.util.*

@SpringBootApplication
class App {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(App::class.java)
    }

    @Bean
    fun customizer() = WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
        it.setPort(DefaultServicePortMappings.RECIPE_SERVICE_PORT)
    }
}

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        if (App.logger.isInfoEnabled) {
            App.logger.info("Args passed in: {}", Arrays.asList(args))
        }

        args.forEach {
            val split = it.split("=")

            if (split.size == 2) {
                System.getProperties().setProperty(split[0], split[1])
            } else {
                FileInputStream(args[0]).use {
                    System.getProperties().load(it)
                }
            }
        }
    } else {
        App.logger.info("No args passed to main")
    }

    runApplication<App>(*args)
}
