package duser.workouttracker.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreateExerciseRequest(
    @field:NotBlank
    @field:Size(max = 120)
    val name: String = "",
    @field:Size(max = 2000)
    val description: String? = null,
)

data class ExerciseResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
