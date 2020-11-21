package poker

import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import java.time.Clock
import java.time.LocalDateTime

@Component
class ExceptionsHandler(private val clock: Clock) : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, includeStackTrace: Boolean): MutableMap<String, Any> {
        val error = getError(request)
        val errorStatus = HttpStatus.BAD_REQUEST
        return mutableMapOf(
            Pair("timestamp", LocalDateTime.now(clock).toString()),
            Pair("path", request.path()),
            Pair("status", errorStatus.value()),
            Pair("message", error.message ?: ""),
            Pair("requestId", request.exchange().request.id),
            Pair("exception", error.javaClass.name),
        )
    }
}