package duser.workouttracker.api

import duser.workouttracker.api.dto.AddExerciseToPlanRequest
import duser.workouttracker.api.dto.CreateWorkoutPlanRequest
import duser.workouttracker.api.dto.WorkoutPlanResponse
import duser.workouttracker.service.WorkoutPlanService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/api/workout-plans")
class WorkoutPlanController(
    private val workoutPlanService: WorkoutPlanService,
) {

    @PostMapping
    fun createWorkoutPlan(@Valid @RequestBody request: CreateWorkoutPlanRequest): ResponseEntity<WorkoutPlanResponse> {
        val response = workoutPlanService.createWorkoutPlan(request)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id)
            .toUri()

        return ResponseEntity.created(location).body(response)
    }

    @GetMapping("/{planId}")
    fun getWorkoutPlan(@PathVariable planId: UUID): WorkoutPlanResponse {
        return workoutPlanService.getWorkoutPlan(planId)
    }

    @PostMapping("/{planId}/exercises")
    fun addExerciseToPlan(
        @PathVariable planId: UUID,
        @Valid @RequestBody request: AddExerciseToPlanRequest,
    ): ResponseEntity<WorkoutPlanResponse> {
        val response = workoutPlanService.addExerciseToPlan(planId, request)
        return ResponseEntity.ok(response)
    }
}
