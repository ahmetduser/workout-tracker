package duser.workouttracker.service

import duser.workouttracker.api.dto.AddExerciseToPlanRequest
import duser.workouttracker.api.dto.CreateWorkoutPlanRequest
import duser.workouttracker.api.dto.WorkoutPlanExerciseResponse
import duser.workouttracker.api.dto.WorkoutPlanResponse
import duser.workouttracker.domain.WorkoutPlan
import duser.workouttracker.domain.WorkoutPlanExercise
import duser.workouttracker.exception.ConflictException
import duser.workouttracker.exception.ResourceNotFoundException
import duser.workouttracker.repository.ExerciseRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WorkoutPlanExerciseRepository
import duser.workouttracker.repository.WorkoutPlanRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WorkoutPlanService(
    private val userRepository: UserRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutPlanExerciseRepository: WorkoutPlanExerciseRepository,
) {

    @Transactional
    fun createWorkoutPlan(request: CreateWorkoutPlanRequest): WorkoutPlanResponse {
        val user = userRepository.findById(requireNotNull(request.userId))
            .orElseThrow { ResourceNotFoundException("User '${request.userId}' not found") }

        val workoutPlan = workoutPlanRepository.save(
            WorkoutPlan(
                user = user,
                name = request.name.trim(),
                description = request.description?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )

        return workoutPlan.toResponse(emptyList())
    }

    @Transactional
    fun addExerciseToPlan(planId: UUID, request: AddExerciseToPlanRequest): WorkoutPlanResponse {
        val workoutPlan = workoutPlanRepository.findById(planId)
            .orElseThrow { ResourceNotFoundException("Workout plan '$planId' not found") }
        val exercise = exerciseRepository.findById(requireNotNull(request.exerciseId))
            .orElseThrow { ResourceNotFoundException("Exercise '${request.exerciseId}' not found") }
        val exerciseOrder = requireNotNull(request.exerciseOrder)

        if (workoutPlanExerciseRepository.existsByWorkoutPlanIdAndExerciseId(planId, requireNotNull(request.exerciseId))) {
            throw ConflictException("Exercise '${request.exerciseId}' is already part of plan '$planId'")
        }
        if (workoutPlanExerciseRepository.existsByWorkoutPlanIdAndExerciseOrder(planId, exerciseOrder)) {
            throw ConflictException("Workout plan '$planId' already uses exercise order '$exerciseOrder'")
        }

        workoutPlanExerciseRepository.save(
            WorkoutPlanExercise(
                workoutPlan = workoutPlan,
                exercise = exercise,
                exerciseOrder = exerciseOrder,
            ),
        )

        return getWorkoutPlan(planId)
    }

    @Transactional(readOnly = true)
    fun getWorkoutPlan(planId: UUID): WorkoutPlanResponse {
        val workoutPlan = workoutPlanRepository.findById(planId)
            .orElseThrow { ResourceNotFoundException("Workout plan '$planId' not found") }
        val exercises = workoutPlanExerciseRepository.findAllByWorkoutPlanIdOrderByExerciseOrderAsc(planId)

        return workoutPlan.toResponse(exercises)
    }

    private fun WorkoutPlan.toResponse(exercises: List<WorkoutPlanExercise>): WorkoutPlanResponse {
        return WorkoutPlanResponse(
            id = requireNotNull(id),
            userId = requireNotNull(user?.id),
            name = name,
            description = description,
            active = active,
            createdAt = requireNotNull(createdAt),
            updatedAt = requireNotNull(updatedAt),
            exercises = exercises.map { it.toResponse() },
        )
    }

    private fun WorkoutPlanExercise.toResponse(): WorkoutPlanExerciseResponse {
        return WorkoutPlanExerciseResponse(
            id = requireNotNull(id),
            exerciseId = requireNotNull(exercise?.id),
            exerciseName = requireNotNull(exercise?.name),
            exerciseOrder = exerciseOrder,
        )
    }
}
