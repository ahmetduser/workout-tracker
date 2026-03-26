package duser.workouttracker.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.AsyncHandlerInterceptor

@Component
class HttpLoggingInterceptor : AsyncHandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (HttpLoggingSupport.shouldSkip(request)) {
            return true
        }

        request.setAttribute(HttpLoggingSupport.STARTED_AT_ATTRIBUTE, System.nanoTime())
        log.info("Incoming HTTP {} {}", request.method, HttpLoggingSupport.requestPath(request))
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        if (HttpLoggingSupport.shouldSkip(request) || HttpLoggingSupport.isResponseLogged(request)) {
            return
        }

        logResponse(
            request = request,
            status = response.status,
        )
        HttpLoggingSupport.markResponseLogged(request)
    }

    fun logResponse(
        request: HttpServletRequest,
        status: Int,
    ) {
        val durationMs = (System.nanoTime() - HttpLoggingSupport.startedAt(request)) / 1_000_000
        log.info(
            "Outgoing HTTP {} {} status={} durationMs={}",
            request.method,
            HttpLoggingSupport.requestPath(request),
            status,
            durationMs,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(HttpLoggingInterceptor::class.java)
    }
}
