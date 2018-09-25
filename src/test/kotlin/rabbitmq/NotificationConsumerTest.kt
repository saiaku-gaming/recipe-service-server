package rabbitmq

import com.valhallagame.common.rabbitmq.NotificationMessage
import com.valhallagame.valhalla.recipeserviceserver.service.RecipeService
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
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
        notificationConsumer.receiveFeatAdd(NotificationMessage("nisse", mapOf("characterName" to "nisse", "feat" to "getting out of bed")))
        verify(recipeService).addRecipe("nisse", "getting out of bed")
    }
}