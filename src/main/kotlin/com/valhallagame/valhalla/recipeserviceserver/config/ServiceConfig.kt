package com.valhallagame.valhalla.recipeserviceserver.config

import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.common.DefaultServicePortMappings
import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.wardrobeserviceclient.WardrobeServiceClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("production", "development")
class ServiceConfig {
    @Bean
    fun characterServiceClient(): CharacterServiceClient {
        CharacterServiceClient.init("http://character-service.character-service:" + DefaultServicePortMappings.CHARACTER_SERVICE_PORT)
        return CharacterServiceClient.get()
    }

    @Bean
    fun currencyServiceClient(): CurrencyServiceClient {
        CurrencyServiceClient.init("http://currency-service.currency-service:" + DefaultServicePortMappings.CURRENCY_SERVICE_PORT)
        return CurrencyServiceClient.get()
    }

    @Bean
    fun wardrobeServiceClient(): WardrobeServiceClient {
        WardrobeServiceClient.init("http://feat-service.feat-service:" + DefaultServicePortMappings.WARDROBE_SERVICE_PORT)
        return WardrobeServiceClient.get()
    }
}
