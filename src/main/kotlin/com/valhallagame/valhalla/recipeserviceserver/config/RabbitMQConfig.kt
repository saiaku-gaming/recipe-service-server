package com.valhallagame.valhalla.recipeserviceserver.config

import com.valhallagame.common.rabbitmq.RabbitMQRouting
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitMQConfig {
    // Recipe configs
    @Bean
    fun characterExchange(): DirectExchange {
        return DirectExchange(RabbitMQRouting.Exchange.CHARACTER.name)
    }

    @Bean
    fun featExchange(): DirectExchange {
        return DirectExchange(RabbitMQRouting.Exchange.FEAT.name)
    }

    @Bean
    fun recipeFeatAddQueue(): Queue {
        return Queue("recipeFeatAddQueue")
    }

    @Bean
    fun bindingFeatAdd(featExchange: DirectExchange, recipeFeatAddQueue: Queue): Binding {
        return BindingBuilder.bind(recipeFeatAddQueue).to(featExchange).with(RabbitMQRouting.Feat.ADD)
    }

    @Bean
    fun recipeCharacterDeleteQueue(): Queue {
        return Queue("recipeCharacterDeleteQueue")
    }

    @Bean
    fun bindingCharacterDeleted(characterExchange: DirectExchange, recipeCharacterDeleteQueue: Queue): Binding {
        return BindingBuilder.bind(recipeCharacterDeleteQueue).to(characterExchange)
                .with(RabbitMQRouting.Character.DELETE)
    }

    @Bean
    fun jacksonConverter(): Jackson2JsonMessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun containerFactory(): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setMessageConverter(jacksonConverter())
        return factory
    }
}