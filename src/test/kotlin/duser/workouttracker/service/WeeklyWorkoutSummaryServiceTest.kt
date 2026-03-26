package duser.workouttracker.service

import duser.workouttracker.domain.Exercise
import duser.workouttracker.domain.SetEntry
import duser.workouttracker.domain.User
import duser.workouttracker.domain.WeeklyWorkoutSummary
import duser.workouttracker.domain.WorkoutSession
import duser.workouttracker.repository.SetEntryRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WeeklyWorkoutSummaryRepository
import duser.workouttracker.repository.WorkoutSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Optional
import java.util.UUID

class WeeklyWorkoutSummaryServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val workoutSessionRepository = mock(WorkoutSessionRepository::class.java)
    private val setEntryRepository = mock(SetEntryRepository::class.java)
    private val weeklyWorkoutSummaryRepository = mock(WeeklyWorkoutSummaryRepository::class.java)
    private val clock = Clock.fixed(Instant.parse("2026-03-30T02:00:00Z"), ZoneOffset.UTC)
    private val weeklyWorkoutSummaryService = WeeklyWorkoutSummaryService(
        userRepository,
        workoutSessionRepository,
        setEntryRepository,
        weeklyWorkoutSummaryRepository,
        clock,
    )

    @Test
    fun `generateWeekSummary aggregates sessions and set metrics for active users`() {
        val weekStart = LocalDate.parse("2026-03-23")
        val rangeStart = Instant.parse("2026-03-23T00:00:00Z")
        val rangeEnd = Instant.parse("2026-03-30T00:00:00Z")
        val user = user(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val inactiveUser = user(UUID.fromString("99999999-9999-9999-9999-999999999999"), "inactive@example.com")
        val firstSession = workoutSession(UUID.fromString("22222222-2222-2222-2222-222222222222"), user, Instant.parse("2026-03-24T10:00:00Z"))
        val secondSession = workoutSession(UUID.fromString("33333333-3333-3333-3333-333333333333"), user, Instant.parse("2026-03-26T10:00:00Z"))
        val exercise = exercise(UUID.fromString("44444444-4444-4444-4444-444444444444"), "Bench Press")
        val setEntries = listOf(
            setEntry(UUID.fromString("55555555-5555-5555-5555-555555555555"), firstSession, exercise, 5, BigDecimal("100.00")),
            setEntry(UUID.fromString("66666666-6666-6666-6666-666666666666"), secondSession, exercise, 8, BigDecimal("80.00")),
        )
        var savedSummary: WeeklyWorkoutSummary? = null

        `when`(userRepository.findAll()).thenReturn(listOf(user, inactiveUser))
        `when`(
            workoutSessionRepository.findAllByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                requireNotNull(user.id),
                rangeStart,
                rangeEnd,
            ),
        ).thenReturn(listOf(firstSession, secondSession))
        `when`(
            workoutSessionRepository.findAllByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                requireNotNull(inactiveUser.id),
                rangeStart,
                rangeEnd,
            ),
        ).thenReturn(emptyList())
        `when`(
            setEntryRepository.findAllByWorkoutSessionIdIn(
                listOf(requireNotNull(firstSession.id), requireNotNull(secondSession.id)),
            ),
        ).thenReturn(setEntries)
        `when`(weeklyWorkoutSummaryRepository.findByUserIdAndWeekStartDate(requireNotNull(user.id), weekStart))
            .thenReturn(Optional.empty())
        `when`(weeklyWorkoutSummaryRepository.findByUserIdAndWeekStartDate(requireNotNull(inactiveUser.id), weekStart))
            .thenReturn(Optional.empty())
        `when`(weeklyWorkoutSummaryRepository.save(any(WeeklyWorkoutSummary::class.java))).thenAnswer {
            it.getArgument<WeeklyWorkoutSummary>(0).also { summary -> savedSummary = summary }
        }

        val generatedCount = weeklyWorkoutSummaryService.generateWeekSummary(weekStart)

        assertEquals(1, generatedCount)
        assertEquals(2, savedSummary?.totalSessions)
        assertEquals(2, savedSummary?.totalSets)
        assertEquals(13, savedSummary?.totalReps)
        assertEquals(BigDecimal("1140.00"), savedSummary?.totalWeightVolume)
    }

    @Test
    fun `generateWeekSummary updates existing summary for same user and week`() {
        val weekStart = LocalDate.parse("2026-03-23")
        val user = user(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val session = workoutSession(UUID.fromString("22222222-2222-2222-2222-222222222222"), user, Instant.parse("2026-03-24T10:00:00Z"))
        val exercise = exercise(UUID.fromString("44444444-4444-4444-4444-444444444444"), "Bench Press")
        val existingSummary = WeeklyWorkoutSummary(
            id = UUID.fromString("77777777-7777-7777-7777-777777777777"),
            user = user,
            weekStartDate = weekStart,
            weekEndDate = weekStart.plusDays(6),
            totalSessions = 1,
            totalSets = 1,
            totalReps = 1,
            totalWeightVolume = BigDecimal.ONE,
            generatedAt = Instant.parse("2026-03-30T01:00:00Z"),
        ).apply {
            createdAt = Instant.parse("2026-03-30T01:00:00Z")
            updatedAt = createdAt
        }

        `when`(userRepository.findAll()).thenReturn(listOf(user))
        `when`(
            workoutSessionRepository.findAllByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                requireNotNull(user.id),
                Instant.parse("2026-03-23T00:00:00Z"),
                Instant.parse("2026-03-30T00:00:00Z"),
            ),
        ).thenReturn(listOf(session))
        `when`(setEntryRepository.findAllByWorkoutSessionIdIn(listOf(requireNotNull(session.id))))
            .thenReturn(listOf(setEntry(UUID.fromString("55555555-5555-5555-5555-555555555555"), session, exercise, 10, BigDecimal("90.00"))))
        `when`(weeklyWorkoutSummaryRepository.findByUserIdAndWeekStartDate(requireNotNull(user.id), weekStart))
            .thenReturn(Optional.of(existingSummary))
        `when`(weeklyWorkoutSummaryRepository.save(any(WeeklyWorkoutSummary::class.java))).thenAnswer { it.getArgument(0) }

        val generatedCount = weeklyWorkoutSummaryService.generateWeekSummary(weekStart)

        assertEquals(1, generatedCount)
        assertEquals(1, existingSummary.totalSessions)
        assertEquals(1, existingSummary.totalSets)
        assertEquals(10, existingSummary.totalReps)
        assertEquals(BigDecimal("900.00"), existingSummary.totalWeightVolume)
        assertEquals(Instant.parse("2026-03-30T02:00:00Z"), existingSummary.generatedAt)
    }

    @Test
    fun `generatePreviousWeekSummary uses previous completed week`() {
        val user = user(UUID.fromString("11111111-1111-1111-1111-111111111111"))

        `when`(userRepository.findAll()).thenReturn(listOf(user))
        `when`(
            workoutSessionRepository.findAllByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
                requireNotNull(user.id),
                Instant.parse("2026-03-23T00:00:00Z"),
                Instant.parse("2026-03-30T00:00:00Z"),
            ),
        ).thenReturn(emptyList())
        `when`(weeklyWorkoutSummaryRepository.findByUserIdAndWeekStartDate(requireNotNull(user.id), LocalDate.parse("2026-03-23")))
            .thenReturn(Optional.empty())

        val generatedCount = weeklyWorkoutSummaryService.generatePreviousWeekSummary()

        assertEquals(0, generatedCount)
        verify(weeklyWorkoutSummaryRepository).findByUserIdAndWeekStartDate(requireNotNull(user.id), LocalDate.parse("2026-03-23"))
    }

    private fun user(id: UUID, email: String = "test@example.com"): User {
        return User(
            id = id,
            email = email,
            displayName = "Test User",
        ).apply {
            createdAt = Instant.parse("2026-03-23T10:00:00Z")
            updatedAt = createdAt
        }
    }

    private fun workoutSession(id: UUID, user: User, startedAt: Instant): WorkoutSession {
        return WorkoutSession(
            id = id,
            user = user,
            startedAt = startedAt,
            notes = "Session notes",
        ).apply {
            createdAt = startedAt
            updatedAt = createdAt
        }
    }

    private fun exercise(id: UUID, name: String): Exercise {
        return Exercise(
            id = id,
            name = name,
        ).apply {
            createdAt = Instant.parse("2026-03-23T10:00:00Z")
            updatedAt = createdAt
        }
    }

    private fun setEntry(
        id: UUID,
        session: WorkoutSession,
        exercise: Exercise,
        reps: Int,
        weight: BigDecimal,
    ): SetEntry {
        return SetEntry(
            id = id,
            workoutSession = session,
            exercise = exercise,
            setOrder = 1,
            reps = reps,
            weight = weight,
        ).apply {
            createdAt = Instant.parse("2026-03-23T11:05:00Z")
            updatedAt = createdAt
        }
    }
}
