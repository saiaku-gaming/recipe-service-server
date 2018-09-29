package com.valhallagame.valhalla.recipeserviceserver.config

import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.wardrobeserviceclient.WardrobeServiceClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("default")
class DefaultConfig {
    @Bean
    fun characterServiceClient(): CharacterServiceClient {
        return CharacterServiceClient.get()
    }

    @Bean
    fun currencyServiceClient(): CurrencyServiceClient {
        return CurrencyServiceClient.get()
    }

    @Bean
    fun wardrobeServiceClient(): WardrobeServiceClient {
        return WardrobeServiceClient.get()
    }
}
