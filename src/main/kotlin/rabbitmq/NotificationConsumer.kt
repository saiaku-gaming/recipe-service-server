package rabbitmq

import com.valhallagame.common.rabbitmq.NotificationMessage
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
        val featName = message.data["feat"] as String
        val characterName = message.data["characterName"] as String
        recipeService.addRecipe(characterName, featName)
    }
}