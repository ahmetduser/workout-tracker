package duser.workouttracker.service

import duser.workouttracker.domain.Exercise
import duser.workouttracker.domain.SetEntry
import duser.workouttracker.domain.User
import duser.workouttracker.domain.WorkoutSession
import duser.workouttracker.repository.ExerciseRepository
import duser.workouttracker.repository.SetEntryRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WeeklyWorkoutSummaryRepository
import duser.workouttracker.repository.WorkoutSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@SpringBootTest
class WeeklyWorkoutSummaryServiceIntegrationTest(
    @Autowired private val weeklyWorkoutSummaryService: WeeklyWorkoutSummaryService,
    @Autowired private val weeklyWorkoutSummaryRepository: WeeklyWorkoutSummaryRepository,
    @Autowired private val setEntryRepository: SetEntryRepository,
    @Autowired private val workoutSessionRepository: WorkoutSessionRepository,
    @Autowired private val exerciseRepository: ExerciseRepository,
    @Autowired private val userRepository: UserRepository,
) {

    @BeforeEach
    fun setUp() {
        weeklyWorkoutSummaryRepository.deleteAll()
        setEntryRepository.deleteAll()
        workoutSessionRepository.deleteAll()
        exerciseRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `generateWeekSummary persists aggregated metrics for active week`() {
        val user = userRepository.save(
            User(
                email = "summary@example.com",
                displayName = "Summary User",
            ),
        )
        val exercise = exerciseRepository.save(Exercise(name = "Bench Press"))
        val firstSession = workoutSessionRepository.save(
            WorkoutSession(
                user = user,
                startedAt = Instant.parse("2026-03-24T10:00:00Z"),
                notes = "Upper body",
            ),
        )
        val secondSession = workoutSessionRepository.save(
            WorkoutSession(
                user = user,
                startedAt = Instant.parse("2026-03-26T18:00:00Z"),
                notes = "Heavy day",
            ),
        )

        setEntryRepository.saveAll(
            listOf(
                SetEntry(
                    workoutSession = firstSession,
                    exercise = exercise,
                    setOrder = 1,
                    reps = 5,
                    weight = BigDecimal("100.00"),
                ),
                SetEntry(
                    workoutSession = secondSession,
                    exercise = exercise,
                    setOrder = 1,
                    reps = 8,
                    weight = BigDecimal("80.00"),
                ),
            ),
        )

        val generatedCount = weeklyWorkoutSummaryService.generateWeekSummary(LocalDate.parse("2026-03-23"))

        assertEquals(1, generatedCount)

        val summary = weeklyWorkoutSummaryRepository
            .findByUserIdAndWeekStartDate(requireNotNull(user.id), LocalDate.parse("2026-03-23"))
            .orElseThrow()

        assertEquals(LocalDate.parse("2026-03-23"), summary.weekStartDate)
        assertEquals(LocalDate.parse("2026-03-29"), summary.weekEndDate)
        assertEquals(2, summary.totalSessions)
        assertEquals(2, summary.totalSets)
        assertEquals(13, summary.totalReps)
        assertEquals(BigDecimal("1140.00"), summary.totalWeightVolume)
        assertEquals(Instant.parse("2026-03-30T02:00:00Z"), summary.generatedAt)
    }

    @TestConfiguration
    class FixedClockConfig {

        @Bean
        @Primary
        fun testClock(): Clock {
            return Clock.fixed(Instant.parse("2026-03-30T02:00:00Z"), ZoneOffset.UTC)
        }
    }
}
