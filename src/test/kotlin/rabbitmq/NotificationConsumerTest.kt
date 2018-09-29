package rabbitmq

import com.valhallagame.common.rabbitmq.NotificationMessage
import com.valhallagame.featserviceclient.message.FeatName
import com.valhallagame.valhalla.recipeserviceserver.service.RecipeService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class NotificationConsumerTest {

    @Mock
    lateinit var recipeService: RecipeService

    @Test
    fun receiveCharacterDelete() {
        val notificationConsumer = NotificationConsumer(recipeService)
        notificationConsumer.receiveCharacterDelete(NotificationMessage("nisse", mapOf("characterName" to "nisse")))
        verify(recipeService).deleteRecipes("nisse")
    }

    @Test
    fun receiveFeatAdd() {
        val notificationConsumer = NotificationConsumer(recipeService)
        val notificationMessage = NotificationMessage(
                "nisse",
                mapOf("characterName" to "nisse", "feat" to FeatName.FREDSTORP_ANORECTIC.name)
        )
        notificationConsumer.receiveFeatAdd(notificationMessage)
        verify(recipeService).addRecipeFromFeat("nisse", FeatName.FREDSTORP_ANORECTIC.name)
    }
}