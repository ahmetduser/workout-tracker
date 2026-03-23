package duser.workouttracker.service

import duser.workouttracker.api.dto.AddExerciseToPlanRequest
import duser.workouttracker.api.dto.CreateWorkoutPlanRequest
import duser.workouttracker.domain.Exercise
import duser.workouttracker.domain.User
import duser.workouttracker.domain.WorkoutPlan
import duser.workouttracker.domain.WorkoutPlanExercise
import duser.workouttracker.exception.ConflictException
import duser.workouttracker.exception.ResourceNotFoundException
import duser.workouttracker.repository.ExerciseRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WorkoutPlanExerciseRepository
import duser.workouttracker.repository.WorkoutPlanRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.Optional
import java.util.UUID

class WorkoutPlanServiceTest {

    private val userRepository = mock(UserRepository::class.java)
    private val exerciseRepository = mock(ExerciseRepository::class.java)
    private val workoutPlanRepository = mock(WorkoutPlanRepository::class.java)
    private val workoutPlanExerciseRepository = mock(WorkoutPlanExerciseRepository::class.java)
    private val workoutPlanService = WorkoutPlanService(
        userRepository,
        exerciseRepository,
        workoutPlanRepository,
        workoutPlanExerciseRepository,
    )

    @Test
    fun `createWorkoutPlan saves plan for existing user`() {
        val user = user(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val createdAt = Instant.parse("2026-03-23T12:00:00Z")
        `when`(userRepository.findById(requireNotNull(user.id))).thenReturn(Optional.of(user))
        `when`(workoutPlanRepository.save(any(WorkoutPlan::class.java))).thenAnswer { invocation ->
            invocation.getArgument<WorkoutPlan>(0).apply {
                id = UUID.fromString("22222222-2222-2222-2222-222222222222")
                this.createdAt = createdAt
                updatedAt = createdAt
            }
        }

        val response = workoutPlanService.createWorkoutPlan(
            CreateWorkoutPlanRequest(
                userId = requireNotNull(user.id),
                name = "  Upper Body  ",
                description = "  Push day  ",
            ),
        )

        assertEquals("Upper Body", response.name)
        assertEquals("Push day", response.description)
        assertEquals(user.id, response.userId)
        assertEquals(emptyList<Any>(), response.exercises)
    }

    @Test
    fun `createWorkoutPlan throws when user is missing`() {
        val userId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        `when`(userRepository.findById(userId)).thenReturn(Optional.empty())

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            workoutPlanService.createWorkoutPlan(
                CreateWorkoutPlanRequest(userId = userId, name = "Plan"),
            )
        }

        assertEquals("User '$userId' not found", exception.message)
    }

    @Test
    fun `addExerciseToPlan saves relation and returns hydrated plan`() {
        val planId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val exerciseId = UUID.fromString("33333333-3333-3333-3333-333333333333")
        val user = user(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val plan = workoutPlan(planId, user)
        val exercise = exercise(exerciseId, "Bench Press")
        val planExercise = workoutPlanExercise(
            UUID.fromString("44444444-4444-4444-4444-444444444444"),
            plan,
            exercise,
            1,
        )

        `when`(workoutPlanRepository.findById(planId)).thenReturn(Optional.of(plan))
        `when`(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise))
        `when`(workoutPlanExerciseRepository.existsByWorkoutPlanIdAndExerciseId(planId, exerciseId)).thenReturn(false)
        `when`(workoutPlanExerciseRepository.existsByWorkoutPlanIdAndExerciseOrder(planId, 1)).thenReturn(false)
        `when`(workoutPlanExerciseRepository.save(any(WorkoutPlanExercise::class.java))).thenAnswer { it.getArgument(0) }
        `when`(workoutPlanExerciseRepository.findAllByWorkoutPlanIdOrderByExerciseOrderAsc(planId)).thenReturn(listOf(planExercise))

        val response = workoutPlanService.addExerciseToPlan(
            planId,
            AddExerciseToPlanRequest(exerciseId = exerciseId, exerciseOrder = 1),
        )

        assertEquals(planId, response.id)
        assertEquals(1, response.exercises.size)
        assertEquals("Bench Press", response.exercises.single().exerciseName)
    }

    @Test
    fun `addExerciseToPlan throws when exercise already belongs to plan`() {
        val planId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val exerciseId = UUID.fromString("33333333-3333-3333-3333-333333333333")
        val plan = workoutPlan(planId, user(UUID.fromString("11111111-1111-1111-1111-111111111111")))
        val exercise = exercise(exerciseId, "Bench Press")

        `when`(workoutPlanRepository.findById(planId)).thenReturn(Optional.of(plan))
        `when`(exerciseRepository.findById(exerciseId)).thenReturn(Optional.of(exercise))
        `when`(workoutPlanExerciseRepository.existsByWorkoutPlanIdAndExerciseId(planId, exerciseId)).thenReturn(true)

        val exception = assertThrows(ConflictException::class.java) {
            workoutPlanService.addExerciseToPlan(
                planId,
                AddExerciseToPlanRequest(exerciseId = exerciseId, exerciseOrder = 1),
            )
        }

        assertEquals("Exercise '$exerciseId' is already part of plan '$planId'", exception.message)
    }

    @Test
    fun `getWorkoutPlan throws when plan is missing`() {
        val planId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        `when`(workoutPlanRepository.findById(planId)).thenReturn(Optional.empty())

        val exception = assertThrows(ResourceNotFoundException::class.java) {
            workoutPlanService.getWorkoutPlan(planId)
        }

        assertEquals("Workout plan '$planId' not found", exception.message)
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

    private fun workoutPlanExercise(id: UUID, plan: WorkoutPlan, exercise: Exercise, order: Int): WorkoutPlanExercise {
        return WorkoutPlanExercise(
            id = id,
            workoutPlan = plan,
            exercise = exercise,
            exerciseOrder = order,
        ).apply {
            createdAt = Instant.parse("2026-03-23T10:00:00Z")
            updatedAt = createdAt
        }
    }
}
