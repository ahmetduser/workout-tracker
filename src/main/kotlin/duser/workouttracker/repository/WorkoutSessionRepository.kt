package duser.workouttracker.repository

import duser.workouttracker.domain.WorkoutSession
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant
import java.util.UUID

interface WorkoutSessionRepository : JpaRepository<WorkoutSession, UUID> {
    fun findAllByUserIdOrderByStartedAtDesc(userId: UUID): List<WorkoutSession>
    fun findAllByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
        userId: UUID,
        startedAtInclusive: Instant,
        startedAtExclusive: Instant,
    ): List<WorkoutSession>
}
