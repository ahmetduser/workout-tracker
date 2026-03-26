package duser.workouttracker.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableConfigurationProperties(SecurityProperties::class)
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.GET, "/api/**", "/actuator/**").permitAll()
                    .anyRequest().authenticated()
            }
            .httpBasic { }
            .formLogin { it.disable() }

        return http.build()
    }

    @Bean
    fun userDetailsService(
        securityProperties: SecurityProperties,
        passwordEncoder: PasswordEncoder,
    ): UserDetailsService {
        val user = User.builder()
            .username(securityProperties.username)
            .password(passwordEncoder.encode(securityProperties.password))
            .roles(securityProperties.role)
            .build()

        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }
}
