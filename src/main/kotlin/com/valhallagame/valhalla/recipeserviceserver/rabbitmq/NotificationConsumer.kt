package com.valhallagame.valhalla.recipeserviceserver.rabbitmq

import com.valhallagame.common.rabbitmq.NotificationMessage
import com.valhallagame.featserviceclient.message.FeatName
import com.valhallagame.valhalla.recipeserviceserver.service.RecipeService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationConsumer(val recipeService: RecipeService) {

    private val logger = LoggerFactory.getLogger(NotificationConsumer::class.java)

    @RabbitListener(queues = ["#{recipeCharacterDeleteQueue.name}"])
    fun receiveCharacterDelete(message: NotificationMessage) {
        logger.info("Received character delete with message: $message")
        val characterName = message.data["characterName"] as String
        recipeService.deleteRecipes(characterName)
    }

    @RabbitListener(queues = ["#{recipeFeatAddQueue.name}"])
    fun receiveFeatAdd(message: NotificationMessage) {
        logger.info("Received feat add notification with message: $message")
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
    }
}