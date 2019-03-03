package com.valhallagame.valhalla.recipeserviceserver.rabbitmq

import com.valhallagame.common.rabbitmq.NotificationMessage
import com.valhallagame.featserviceclient.message.FeatName
import com.valhallagame.valhalla.recipeserviceserver.service.RecipeService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*

@Component
class NotificationConsumer(val recipeService: RecipeService) {
    companion object {
        private val logger = LoggerFactory.getLogger(NotificationConsumer::class.java)
    }

    @Value("\${spring.application.name}")
    private val appName: String? = null

    @RabbitListener(queues = ["#{recipeCharacterDeleteQueue.name}"])
    fun receiveCharacterDelete(message: NotificationMessage) {
        MDC.put("service_name", appName)
        MDC.put("request_id", message.data["requestId"] as String? ?: UUID.randomUUID().toString())

        logger.info("Received character delete with message: $message")

        try {
            val characterName = message.data["characterName"] as String
            recipeService.deleteRecipes(characterName)
        } finally {
            MDC.clear()
        }
    }

    @RabbitListener(queues = ["#{recipeFeatAddQueue.name}"])
    fun receiveFeatAdd(message: NotificationMessage) {
        MDC.put("service_name", appName)
        MDC.put("request_id", message.data["requestId"] as String? ?: UUID.randomUUID().toString())

        logger.info("Received feat add notification with message: $message")

        try {
            val featNameString = message.data["feat"] as String
            val characterName = message.data["characterName"] as String
            val featName = FeatName.valueOf(featNameString)
            try {
                recipeService.addRecipeFromFeat(characterName, featName)
            } catch (e: IllegalArgumentException) {
                if (e.message!!.contains("already added recipe", false)) {
                    logger.info("Tried to add $featName to $characterName but it already had that recipe")
                    return
                }
                throw e
            }
        } finally {
            MDC.clear()
        }
    }
}