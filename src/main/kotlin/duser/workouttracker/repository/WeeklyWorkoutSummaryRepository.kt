package duser.workouttracker.repository

import duser.workouttracker.domain.WeeklyWorkoutSummary
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

interface WeeklyWorkoutSummaryRepository : JpaRepository<WeeklyWorkoutSummary, UUID> {
    fun findByUserIdAndWeekStartDate(userId: UUID, weekStartDate: LocalDate): Optional<WeeklyWorkoutSummary>
}
