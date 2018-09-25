package com.valhallagame.valhalla.recipeserviceserver.config

import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.common.DefaultServicePortMappings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("production", "development")
class ServiceConfig {
    @Bean
    fun characterServiceClient(): CharacterServiceClient {
        CharacterServiceClient.init("http://character-service:" + DefaultServicePortMappings.CHARACTER_SERVICE_PORT)
        return CharacterServiceClient.get()
    }
}
