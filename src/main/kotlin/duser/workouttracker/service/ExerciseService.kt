package duser.workouttracker.service

import duser.workouttracker.api.dto.CreateExerciseRequest
import duser.workouttracker.api.dto.ExerciseResponse
import duser.workouttracker.domain.Exercise
import duser.workouttracker.exception.ConflictException
import duser.workouttracker.repository.ExerciseRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
) {

    @Transactional
    fun createExercise(request: CreateExerciseRequest): ExerciseResponse {
        val normalizedName = request.name.trim()
        if (exerciseRepository.existsByNameIgnoreCase(normalizedName)) {
            throw ConflictException("Exercise with name '$normalizedName' already exists")
        }

        val exercise = exerciseRepository.save(
            Exercise(
                name = normalizedName,
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )

        logger.info("Created exercise id={} name={}", exercise.id, exercise.name)

        return exercise.toResponse()
    }

    @Transactional(readOnly = true)
    fun listExercises(): List<ExerciseResponse> {
        val exercises = exerciseRepository.findAll()
            .sortedBy { it.name.lowercase() }
            .map { it.toResponse() }

        logger.debug("Listed exercises count={}", exercises.size)
        return exercises
    }

    private fun Exercise.toResponse(): ExerciseResponse {
        return ExerciseResponse(
            id = requireNotNull(id),
            name = name,
            description = description,
            createdAt = requireNotNull(createdAt),
            updatedAt = requireNotNull(updatedAt),
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseService::class.java)
    }
}
