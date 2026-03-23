package duser.workouttracker.service

import duser.workouttracker.api.dto.CreateWorkoutSessionRequest
import duser.workouttracker.api.dto.LogSetEntryRequest
import duser.workouttracker.domain.Exercise
import duser.workouttracker.domain.SetEntry
import duser.workouttracker.domain.User
import duser.workouttracker.domain.WorkoutPlan
import duser.workouttracker.domain.WorkoutSession
import duser.workouttracker.exception.ResourceNotFoundException
import duser.workouttracker.repository.ExerciseRepository
import duser.workouttracker.repository.SetEntryRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WorkoutPlanRepository
import duser.workouttracker.repository.WorkoutSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import java.util.UUID

class WorkoutSessionServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val workoutPlanRepository = mock(WorkoutPlanRepository::class.java)
    private val workoutSessionRepository = mock(WorkoutSessionRepository::class.java)
    private val exerciseRepository = mock(ExerciseRepository::class.java)
    private val setEntryRepository = mock(SetEntryRepository::class.java)
    private val workoutSessionService = WorkoutSessionService(
        userRepository,
        workoutPlanRepository,
        workoutSessionRepository,
        exerciseRepository,
        setEntryRepository,
    )

    @Test
    fun `createWorkoutSession creates session for user and optional plan`() {
        val user = user(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val plan = workoutPlan(UUID.fromString("22222222-2222-2222-2222-222222222222"), user)
        val createdAt = Instant.parse("2026-03-23T12:00:00Z")
        `when`(userRepository.findById(requireNotNull(user.id))).thenReturn(Optional.of(user))
        `when`(workoutPlanRepository.findById(requireNotNull(plan.id))).thenReturn(Optional.of(plan))
        `when`(workoutSessionRepository.save(any(WorkoutSession::class.java))).thenAnswer { invocation ->
            invocation.getArgument<WorkoutSession>(0).apply {
                id = UUID.fromString("33333333-3333-3333-3333-333333333333")
                this.createdAt = createdAt
                updatedAt = createdAt
            }
        }

        val response = workoutSessionService.createWorkoutSession(
            CreateWorkoutSessionRequest(
                userId = requireNotNull(user.id),
                workoutPlanId = requireNotNull(plan.id),
                notes = "  Strong session  ",
            ),
        )

        assertEquals(user.id, response.userId)
        assertEquals(plan.id, response.workoutPlanId)
        assertEquals("Strong session", response.notes)
        assertNotNull(response.startedAt)
        assertEquals(emptyList<Any>(), response.sets)
    }

    @Test
    fun `createWorkoutSession throws when user is missing`() {
        val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            workoutSessionService.createWorkoutSession(CreateWorkoutSessionRequest(userId = userId))
        }

        assertEquals("User '$userId' not found", exception.message)
    }

    @Test
    fun `logSetEntry saves set and returns workout session with sets`() {
        val user = user(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333")
        val exerciseId = UUID.fromString("44444444-4444-4444-4444-444444444444")
        val session = workoutSession(sessionId, user, null)
        val exercise = exercise(exerciseId, "Bench Press")
        val setEntry = setEntry(
            UUID.fromString("55555555-5555-5555-5555-555555555555"),
            session,
            exercise,
        )

        `when`(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.of(session))
        `when`(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise))
        `when`(setEntryRepository.save(any(SetEntry::class.java))).thenAnswer { it.getArgument(0) }
        `when`(setEntryRepository.findAllByWorkoutSessionIdOrderBySetOrderAsc(sessionId)).thenReturn(listOf(setEntry))

        val response = workoutSessionService.logSetEntry(
            sessionId,
            LogSetEntryRequest(
                exerciseId = exerciseId,
                setOrder = 1,
                reps = 8,
                weight = BigDecimal("100.50"),
            ),
        )

        assertEquals(sessionId, response.id)
        assertEquals(1, response.sets.size)
        assertEquals("Bench Press", response.sets.single().exerciseName)
        assertEquals(BigDecimal("100.50"), response.sets.single().weight)
    }

    @Test
    fun `getWorkoutHistory returns sessions ordered from repository with their sets`() {
        val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val user = user(userId)
        val firstSession = workoutSession(UUID.fromString("33333333-3333-3333-3333-333333333333"), user, null)
        val secondSession = workoutSession(UUID.fromString("66666666-6666-6666-6666-666666666666"), user, null)
        val exercise = exercise(UUID.fromString("44444444-4444-4444-4444-444444444444"), "Row")

        `when`(workoutSessionRepository.findAllByUserIdOrderByStartedAtDesc(userId)).thenReturn(listOf(firstSession, secondSession))
        `when`(setEntryRepository.findAllByWorkoutSessionIdOrderBySetOrderAsc(firstSession.id!!)).thenReturn(
            listOf(setEntry(UUID.fromString("55555555-5555-5555-5555-555555555555"), firstSession, exercise)),
        )
        `when`(setEntryRepository.findAllByWorkoutSessionIdOrderBySetOrderAsc(secondSession.id!!)).thenReturn(emptyList())

        val response = workoutSessionService.getWorkoutHistory(userId)

        assertEquals(2, response.size)
        assertEquals(1, response.first().sets.size)
        assertEquals(0, response.last().sets.size)
    }

    @Test
    fun `getWorkoutSession throws when session is missing`() {
        val sessionId = UUID.fromString("33333333-3333-3333-3333-333333333333")
        `when`(workoutSessionRepository.findById(sessionId)).thenReturn(Optional.empty())

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            workoutSessionService.getWorkoutSession(sessionId)
        }

        assertEquals("Workout session '$sessionId' not found", exception.message)
    }

    private fun user(id: UUID): User {
        return User(
            id = id,
            email = "test@example.com",
            displayName = "Test User",
        ).apply {
            createdAt = Instant.parse("2026-03-23T10:00:00Z")
            updatedAt = createdAt
        }
    }

    private fun workoutPlan(id: UUID, user: User): WorkoutPlan {
        return WorkoutPlan(
            id = id,
            user = user,
            name = "Upper Body",
            description = "Push day",
        ).apply {
            createdAt = Instant.parse("2026-03-23T10:00:00Z")
            updatedAt = createdAt
        }
    }

    private fun workoutSession(id: UUID, user: User, workoutPlan: WorkoutPlan?): WorkoutSession {
        return WorkoutSession(
            id = id,
            user = user,
            workoutPlan = workoutPlan,
            startedAt = Instant.parse("2026-03-23T11:00:00Z"),
            notes = "Session notes",
        ).apply {
            createdAt = Instant.parse("2026-03-23T11:00:00Z")
            updatedAt = createdAt
        }
    }

    private fun exercise(id: UUID, name: String): Exercise {
        return Exercise(
            id = id,
            name = name,
            description = null,
        ).apply {
            createdAt = Instant.parse("2026-03-23T10:00:00Z")
            updatedAt = createdAt
        }
    }

    private fun setEntry(id: UUID, session: WorkoutSession, exercise: Exercise): SetEntry {
        return SetEntry(
            id = id,
            workoutSession = session,
            exercise = exercise,
            setOrder = 1,
            reps = 8,
            weight = BigDecimal("100.50"),
        ).apply {
            createdAt = Instant.parse("2026-03-23T11:05:00Z")
            updatedAt = createdAt
        }
    }
}
