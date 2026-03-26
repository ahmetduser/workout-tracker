package duser.workouttracker.service

import duser.workouttracker.domain.SetEntry
import duser.workouttracker.domain.WeeklyWorkoutSummary
import duser.workouttracker.repository.SetEntryRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WeeklyWorkoutSummaryRepository
import duser.workouttracker.repository.WorkoutSessionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters

@Service
class WeeklyWorkoutSummaryService(
    private val userRepository: UserRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val setEntryRepository: SetEntryRepository,
    private val weeklyWorkoutSummaryRepository: WeeklyWorkoutSummaryRepository,
    private val clock: Clock,
) {

    @Transactional
    fun generatePreviousWeekSummary(): Int {
        val today = LocalDate.now(clock)
        val previousWeekStart = today
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .minusWeeks(1)

        return generateWeekSummary(previousWeekStart)
    }

    @Transactional
    fun generateWeekSummary(weekStartDate: LocalDate): Int {
        val normalizedWeekStart = weekStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEndDate = normalizedWeekStart.plusDays(6)
        val rangeStart = normalizedWeekStart.atStartOfDay().toInstant(ZoneOffset.UTC)
        val rangeEndExclusive = normalizedWeekStart.plusWeeks(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        val generatedAt = Instant.now(clock)

        val users = userRepository.findAll()
        var generatedCount = 0

        users.forEach { user ->
            val userId = requireNotNull(user.id)
            val sessions = workoutSessionRepository.findAllByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                userId,
                rangeStart,
                rangeEndExclusive,
            )
            val sessionIds = sessions.mapNotNull { it.id }
            val setEntries = if (sessionIds.isEmpty()) {
                emptyList()
            } else {
                setEntryRepository.findAllByWorkoutSessionIdIn(sessionIds)
            }

            val existingSummary = weeklyWorkoutSummaryRepository.findByUserIdAndWeekStartDate(userId, normalizedWeekStart)
            if (sessions.isEmpty() && existingSummary.isEmpty) {
                return@forEach
            }

            val summary = existingSummary.orElseGet {
                WeeklyWorkoutSummary(
                    user = user,
                    weekStartDate = normalizedWeekStart,
                    weekEndDate = weekEndDate,
                )
            }

            summary.weekEndDate = weekEndDate
            summary.totalSessions = sessions.size
            summary.totalSets = setEntries.size
            summary.totalReps = setEntries.sumOf { it.reps ?: 0 }
            summary.totalWeightVolume = totalWeightVolume(setEntries)
            summary.generatedAt = generatedAt

            weeklyWorkoutSummaryRepository.save(summary)
            generatedCount++
        }

        logger.info(
            "Generated weekly workout summaries weekStartDate={} weekEndDate={} userCount={}",
            normalizedWeekStart,
            weekEndDate,
            generatedCount,
        )

        return generatedCount
    }

    private fun totalWeightVolume(setEntries: List<SetEntry>): BigDecimal {
        return setEntries.fold(BigDecimal.ZERO) { total, setEntry ->
            val weight = setEntry.weight ?: BigDecimal.ZERO
            val reps = BigDecimal.valueOf((setEntry.reps ?: 0).toLong())
            total + weight.multiply(reps)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WeeklyWorkoutSummaryService::class.java)
    }
}
