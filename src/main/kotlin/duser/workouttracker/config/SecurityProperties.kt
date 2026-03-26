package duser.workouttracker.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security")
data class SecurityProperties(
    var username: String = "workout-admin",
    var password: String = "change-me",
    var role: String = "WRITER",
)
