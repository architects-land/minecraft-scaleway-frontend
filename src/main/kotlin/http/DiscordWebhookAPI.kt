package world.anhgelus.world.architectsland.minecraftscalewayfrontend.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import world.anhgelus.world.architectsland.minecraftscalewayfrontend.LOGGER
import java.net.http.HttpRequest

class DiscordWebhookAPI(val url: String?, val username: String? = null) : HttpAPI() {
    @Serializable
    data class WebhookContent(val content: String, val username: String? = null)

    fun sendMessage(s: String) {
        if (url == null) {
            LOGGER.trace("No webhook set")
            return
        }
        val content = WebhookContent(s, username)
        val encoder = Json { encodeDefaults = false }
        send(
            builder(url)
                .POST(HttpRequest.BodyPublishers.ofString(encoder.encodeToString(content)))
                .build()
        )
    }
}