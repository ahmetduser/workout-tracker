package duser.workouttracker.api.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CreateWorkoutSessionRequest(
    @field:NotNull
    val userId: UUID? = null,
    val workoutPlanId: UUID? = null,
    @field:Size(max = 2000)
    val notes: String? = null,
)

data class LogSetEntryRequest(
    @field:NotNull
    val exerciseId: UUID? = null,
    @field:Positive
    val setOrder: Int? = null,
    @field:Positive
    val reps: Int? = null,
    @field:Positive
    val weight: BigDecimal? = null,
    @field:Positive
    val durationSeconds: Int? = null,
    @field:Positive
    val distanceMeters: BigDecimal? = null,
)

data class SetEntryResponse(
    val id: UUID,
    val exerciseId: UUID,
    val exerciseName: String,
    val setOrder: Int,
    val reps: Int?,
    val weight: BigDecimal?,
    val durationSeconds: Int?,
    val distanceMeters: BigDecimal?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class WorkoutSessionResponse(
    val id: UUID,
    val userId: UUID,
    val workoutPlanId: UUID?,
    val startedAt: Instant,
    val endedAt: Instant?,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val sets: List<SetEntryResponse>,
)
