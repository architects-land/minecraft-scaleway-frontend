package world.anhgelus.world.architectsland.minecraftscalewayfrontend

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ScalewayAPI(val apiKey: String, val zone: String, val server: String) {
    class HttpErrorException(override val message: String?) : RuntimeException(message)

    private val client = HttpClient.newHttpClient()

    init {
        try {
            send(builder("https://api.scaleway.com/instance/v1/zones/$zone/servers/$server").GET().build())
        } catch (e: HttpErrorException) {
            throw IllegalArgumentException("Cannot connect to server $server in $zone with given apiKey", e)
        }
    }

    private fun builder(uri: String): HttpRequest.Builder {
        return HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .setHeader("X-Auth-Token", apiKey)
            .setHeader("ContentType", "application/json")
    }

    private fun send(request: HttpRequest): HttpResponse<String> {
        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (resp.statusCode() >= 400) {
            throw HttpErrorException("Invalid server response status code: ${resp.statusCode()}")
        }
        return resp
    }
}