package duser.workouttracker.config

import jakarta.servlet.http.HttpServletRequest

object HttpLoggingSupport {
    const val STARTED_AT_ATTRIBUTE = "httpLogging.startedAt"
    const val RESPONSE_LOGGED_ATTRIBUTE = "httpLogging.responseLogged"

    fun shouldSkip(request: HttpServletRequest): Boolean {
        return request.requestURI.startsWith("/actuator/prometheus")
    }

    fun requestPath(request: HttpServletRequest): String {
        return buildString {
            append(request.requestURI)
            request.queryString?.takeIf { it.isNotBlank() }?.let {
                append('?')
                append(it)
            }
        }
    }

    fun markResponseLogged(request: HttpServletRequest) {
        request.setAttribute(RESPONSE_LOGGED_ATTRIBUTE, true)
    }

    fun isResponseLogged(request: HttpServletRequest): Boolean {
        return request.getAttribute(RESPONSE_LOGGED_ATTRIBUTE) == true
    }

    fun startedAt(request: HttpServletRequest): Long {
        return request.getAttribute(STARTED_AT_ATTRIBUTE) as? Long ?: System.nanoTime()
    }
}
