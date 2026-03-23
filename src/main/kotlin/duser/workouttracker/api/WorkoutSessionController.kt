package duser.workouttracker.api

import duser.workouttracker.api.dto.CreateWorkoutSessionRequest
import duser.workouttracker.api.dto.LogSetEntryRequest
import duser.workouttracker.api.dto.WorkoutSessionResponse
import duser.workouttracker.service.WorkoutSessionService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@Validated
@RestController
@RequestMapping("/api/workout-sessions")
class WorkoutSessionController(
    private val workoutSessionService: WorkoutSessionService,
) {

    @PostMapping
    fun createWorkoutSession(@Valid @RequestBody request: CreateWorkoutSessionRequest): ResponseEntity<WorkoutSessionResponse> {
        val response = workoutSessionService.createWorkoutSession(request)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id)
            .toUri()

        return ResponseEntity.created(location).body(response)
    }

    @GetMapping
    fun getWorkoutHistory(@RequestParam @NotNull userId: UUID?): List<WorkoutSessionResponse> {
        return workoutSessionService.getWorkoutHistory(requireNotNull(userId))
    }

    @GetMapping("/{sessionId}")
    fun getWorkoutSession(@PathVariable sessionId: UUID): WorkoutSessionResponse {
        return workoutSessionService.getWorkoutSession(sessionId)
    }

    @PostMapping("/{sessionId}/set-entries")
    fun logSetEntry(
        @PathVariable sessionId: UUID,
        @Valid @RequestBody request: LogSetEntryRequest,
    ): ResponseEntity<WorkoutSessionResponse> {
        val response = workoutSessionService.logSetEntry(sessionId, request)
        return ResponseEntity.ok(response)
    }
}
