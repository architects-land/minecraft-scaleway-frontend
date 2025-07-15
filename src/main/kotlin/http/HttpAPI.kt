package world.anhgelus.world.architectsland.minecraftscalewayfrontend.http

import world.anhgelus.world.architectsland.minecraftscalewayfrontend.LOGGER
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class HttpAPI {
    class HttpErrorException(override val message: String?) : RuntimeException(message)

    protected var client: HttpClient = HttpClient.newHttpClient()

    protected open fun builder(uri: String): HttpRequest.Builder {
        return HttpRequest.newBuilder().uri(URI.create(uri))
    }

    protected fun send(request: HttpRequest): HttpResponse<String> {
        var resp : HttpResponse<String>
        try {
            resp = client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: IOException) {
            if (e.message == null || !e.message!!.contains("GOAWAY")) throw e
            resetClient()
            resp = client.send(request, HttpResponse.BodyHandlers.ofString())
        }
        resetClient()
        if (resp.statusCode() >= 400) {
            LOGGER.info(resp.body())
            throw HttpErrorException("Invalid server response status code: ${resp.statusCode()}")
        }
        return resp
    }

    private fun resetClient() {
        client.close()
        client = HttpClient.newHttpClient()
    }
}