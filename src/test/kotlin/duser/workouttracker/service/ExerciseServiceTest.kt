package duser.workouttracker.service

import duser.workouttracker.api.dto.CreateExerciseRequest
import duser.workouttracker.domain.Exercise
import duser.workouttracker.exception.ConflictException
import duser.workouttracker.repository.ExerciseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Instant
import java.util.UUID

class ExerciseServiceTest {

    private val exerciseRepository = mock(ExerciseRepository::class.java)
    private val exerciseService = ExerciseService(exerciseRepository)

    @Test
    fun `createExercise trims fields and returns saved exercise`() {
        val now = Instant.parse("2026-03-23T12:00:00Z")
        `when`(exerciseRepository.existsByNameIgnoreCase("Bench Press")).thenReturn(false)
        `when`(exerciseRepository.save(any(Exercise::class.java))).thenAnswer { invocation ->
            invocation.getArgument<Exercise>(0).apply {
                id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                createdAt = now
                updatedAt = now
            }
        }

        val response = exerciseService.createExercise(
            CreateExerciseRequest(
                name = "  Bench Press  ",
                description = "  Chest compound movement  ",
            ),
        )

        assertEquals("Bench Press", response.name)
        assertEquals("Chest compound movement", response.description)
        assertEquals(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), response.id)
        assertEquals(now, response.createdAt)
        assertEquals(now, response.updatedAt)
    }

    @Test
    fun `createExercise throws when exercise name already exists`() {
        `when`(exerciseRepository.existsByNameIgnoreCase("Bench Press")).thenReturn(true)

        val exception = assertThrows(ConflictException::class.java) {
            exerciseService.createExercise(CreateExerciseRequest(name = " Bench Press "))
        }

        assertEquals("Exercise with name 'Bench Press' already exists", exception.message)
    }

    @Test
    fun `listExercises returns exercises sorted by name`() {
        val firstTime = Instant.parse("2026-03-23T12:00:00Z")
        val secondTime = Instant.parse("2026-03-23T12:10:00Z")
        `when`(exerciseRepository.findAll()).thenReturn(
            listOf(
                exercise(
                    id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                    name = "Squat",
                    createdAt = secondTime,
                ),
                exercise(
                    id = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                    name = "bench press",
                    createdAt = firstTime,
                ),
            ),
        )

        val response = exerciseService.listExercises()

        assertEquals(listOf("bench press", "Squat"), response.map { it.name })
    }

    private fun exercise(id: UUID, name: String, createdAt: Instant): Exercise {
        return Exercise(
            id = id,
            name = name,
            description = null,
        ).apply {
            this.createdAt = createdAt
            this.updatedAt = createdAt
        }
    }
}
