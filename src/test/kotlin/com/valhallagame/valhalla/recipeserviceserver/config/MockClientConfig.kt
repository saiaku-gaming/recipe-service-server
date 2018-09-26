package com.valhallagame.valhalla.recipeserviceserver.config

import com.valhallagame.characterserviceclient.CharacterServiceClient
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("mock-client")
@Configuration
class MockClientConfig {

    companion object {
        val characterServiceClientMock = Mockito.mock(CharacterServiceClient::class.java)!!
    }

    @Bean
    @Primary
    fun characterServiceClient(): CharacterServiceClient {
        return characterServiceClientMock
    }
}