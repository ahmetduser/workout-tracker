package duser.workouttracker.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreateWorkoutPlanRequest(
    @field:NotNull
    val userId: UUID? = null,
    @field:NotBlank
    @field:Size(max = 120)
    val name: String = "",
    @field:Size(max = 2000)
    val description: String? = null,
)

data class AddExerciseToPlanRequest(
    @field:NotNull
    val exerciseId: UUID? = null,
    @field:Positive
    val exerciseOrder: Int? = null,
)

data class WorkoutPlanExerciseResponse(
    val id: UUID,
    val exerciseId: UUID,
    val exerciseName: String,
    val exerciseOrder: Int,
)

data class WorkoutPlanResponse(
    val id: UUID,
    val userId: UUID,
    val name: String,
    val description: String?,
    val active: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val exercises: List<WorkoutPlanExerciseResponse>,
)
