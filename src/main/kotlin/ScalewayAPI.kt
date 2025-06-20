package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ScalewayAPI(val apiKey: String, val zone: String, val server: String) {
    class HttpErrorException(override val message: String?) : RuntimeException(message)

    @Serializable
    data class Server(val id: String, val name: String, val state: String)

    @Serializable
    data class ServerResponse(val server: Server)

    enum class ServerState {
        RUNNING, STOPPED, STOPPED_IN_PLACE, STARTING, STOPPING, LOCKED
    }

    private val client = HttpClient.newHttpClient()

    init {
        try {
            serverState()
        } catch (e: HttpErrorException) {
            throw IllegalArgumentException("Cannot connect to server $server in $zone with given apiKey", e)
        }
    }

    fun startServer() {
        send(
            builder("https://api.scaleway.com/instance/v1/zones/$zone/servers/$server/action")
            .POST(HttpRequest.BodyPublishers.ofString("{\"action\":\"poweron\"}"))
            .build()
        )
    }

    fun serverState(): ServerState {
        val resp = send(builder("https://api.scaleway.com/instance/v1/zones/$zone/servers/$server").GET().build())
        val decoder = Json { ignoreUnknownKeys = true }
        val serverResp = decoder.decodeFromString(ServerResponse.serializer(), resp.body())
        return when (serverResp.server.state) {
            "running" -> ServerState.RUNNING
            "stopped" -> ServerState.STOPPED
            "stopped in place" -> ServerState.STOPPED_IN_PLACE
            "starting" -> ServerState.STARTING
            "stopping" -> ServerState.STOPPING
            "locked" -> ServerState.LOCKED
            else -> throw HttpErrorException("Server state ${serverResp.server.state} is not supported")
        }
    }

    private fun builder(uri: String): HttpRequest.Builder {
        return HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .setHeader("X-Auth-Token", apiKey)
            .setHeader("Content-Type", "application/json")
    }

    private fun send(request: HttpRequest): HttpResponse<String> {
        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (resp.statusCode() >= 400) {
            LOGGER.info(resp.body())
            throw HttpErrorException("Invalid server response status code: ${resp.statusCode()}")
        }
        return resp
    }
}