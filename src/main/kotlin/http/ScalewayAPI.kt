package world.anhgelus.architectsland.minecraftscalewayfrontend.http

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.http.HttpRequest

class ScalewayAPI(private val apiKey: String, val zone: String, val server: String) : HttpAPI() {
    @Serializable
    data class Server(val id: String, val name: String, val state: String)

    @Serializable
    data class ServerResponse(val server: Server)

    enum class ServerState {
        RUNNING, STOPPED, STOPPED_IN_PLACE, STARTING, STOPPING, LOCKED
    }

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

    fun powerOffServer() {
        send(
            builder("https://api.scaleway.com/instance/v1/zones/$zone/servers/$server/action")
                .POST(HttpRequest.BodyPublishers.ofString("{\"action\":\"poweroff\"}"))
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

    override fun builder(uri: String): HttpRequest.Builder {
        return super.builder(uri)
            .setHeader("X-Auth-Token", apiKey)
            .setHeader("Content-Type", "application/json")
    }
}