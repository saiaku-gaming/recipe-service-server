package com.valhallagame.valhalla.recipeserviceserver.config

import com.valhallagame.characterserviceclient.CharacterServiceClient
import com.valhallagame.currencyserviceclient.CurrencyServiceClient
import com.valhallagame.wardrobeserviceclient.WardrobeServiceClient
import org.mockito.Mockito
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("mock-client")
@Configuration
class MockClientConfig {

    companion object {
        val characterServiceClientMock = Mockito.mock(CharacterServiceClient::class.java)!!
        val currencyServiceClientMock = Mockito.mock(CurrencyServiceClient::class.java)!!
        val wardrobeServiceClientMock = Mockito.mock(WardrobeServiceClient::class.java)!!
        val rabbitTemplate = Mockito.mock(RabbitTemplate::class.java)!!
    }

    @Bean
    @Primary
    fun characterServiceClient(): CharacterServiceClient {
        return characterServiceClientMock
    }

    @Bean
    @Primary
    fun currencyServiceClient(): CurrencyServiceClient {
        return currencyServiceClientMock
    }

    @Bean
    @Primary
    fun wardrobeServiceClient(): WardrobeServiceClient {
        return wardrobeServiceClientMock
    }

    @Bean
    @Primary
    fun rabbitTemplate(): RabbitTemplate {
        return rabbitTemplate;
    }
}