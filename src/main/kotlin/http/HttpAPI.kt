package world.anhgelus.architectsland.minecraftscalewayfrontend.http

import world.anhgelus.architectsland.minecraftscalewayfrontend.LOGGER
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class HttpAPI {
    class HttpErrorException(override val message: String?) : RuntimeException(message)

    protected val client: HttpClient = HttpClient.newHttpClient()

    protected open fun builder(uri: String): HttpRequest.Builder {
        return HttpRequest.newBuilder()
            .uri(URI.create(uri))
    }

    protected fun send(request: HttpRequest): HttpResponse<String> {
        val resp = client.send(request, HttpResponse.BodyHandlers.ofString())
        if (resp.statusCode() >= 400) {
            LOGGER.info(resp.body())
            throw HttpErrorException("Invalid server response status code: ${resp.statusCode()}")
        }
        return resp
    }
}