package duser.workouttracker.service

import duser.workouttracker.api.dto.CreateWorkoutSessionRequest
import duser.workouttracker.api.dto.LogSetEntryRequest
import duser.workouttracker.api.dto.SetEntryResponse
import duser.workouttracker.api.dto.WorkoutSessionResponse
import duser.workouttracker.domain.SetEntry
import duser.workouttracker.domain.WorkoutSession
import duser.workouttracker.exception.ResourceNotFoundException
import duser.workouttracker.repository.ExerciseRepository
import duser.workouttracker.repository.SetEntryRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WorkoutPlanRepository
import duser.workouttracker.repository.WorkoutSessionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class WorkoutSessionService(
    private val userRepository: UserRepository,
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val setEntryRepository: SetEntryRepository,
) {

    @Transactional
    fun createWorkoutSession(request: CreateWorkoutSessionRequest): WorkoutSessionResponse {
        val user = userRepository.findById(requireNotNull(request.userId))
            .orElseThrow { ResourceNotFoundException("User '${request.userId}' not found") }
        val workoutPlan = request.workoutPlanId?.let { planId ->
            workoutPlanRepository.findById(planId)
                .orElseThrow { ResourceNotFoundException("Workout plan '$planId' not found") }
        }

        val workoutSession = workoutSessionRepository.save(
            WorkoutSession(
                user = user,
                workoutPlan = workoutPlan,
                startedAt = Instant.now(),
                notes = request.notes?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )

        logger.info(
            "Created workout session id={} userId={} workoutPlanId={}",
            workoutSession.id,
            user.id,
            workoutPlan?.id,
        )

        return workoutSession.toResponse(emptyList())
    }

    @Transactional
    fun logSetEntry(sessionId: UUID, request: LogSetEntryRequest): WorkoutSessionResponse {
        val workoutSession = workoutSessionRepository.findById(sessionId)
            .orElseThrow { ResourceNotFoundException("Workout session '$sessionId' not found") }
        val exercise = exerciseRepository.findById(requireNotNull(request.exerciseId))
            .orElseThrow { ResourceNotFoundException("Exercise '${request.exerciseId}' not found") }

        setEntryRepository.save(
            SetEntry(
                workoutSession = workoutSession,
                exercise = exercise,
                setOrder = requireNotNull(request.setOrder),
                reps = request.reps,
                weight = request.weight,
                durationSeconds = request.durationSeconds,
                distanceMeters = request.distanceMeters,
            ),
        )

        logger.info(
            "Logged set entry sessionId={} exerciseId={} setOrder={} reps={} weight={}",
            sessionId,
            exercise.id,
            request.setOrder,
            request.reps,
            request.weight,
        )

        return getWorkoutSession(sessionId)
    }

    @Transactional(readOnly = true)
    fun getWorkoutHistory(userId: UUID): List<WorkoutSessionResponse> {
        val sessions = workoutSessionRepository.findAllByUserIdOrderByStartedAtDesc(userId)
            .map { session ->
                val sets = setEntryRepository.findAllByWorkoutSessionIdOrderBySetOrderAsc(requireNotNull(session.id))
                session.toResponse(sets)
            }

        logger.debug("Fetched workout history userId={} sessionCount={}", userId, sessions.size)
        return sessions
    }

    @Transactional(readOnly = true)
    fun getWorkoutSession(sessionId: UUID): WorkoutSessionResponse {
        val workoutSession = workoutSessionRepository.findById(sessionId)
            .orElseThrow { ResourceNotFoundException("Workout session '$sessionId' not found") }
        val sets = setEntryRepository.findAllByWorkoutSessionIdOrderBySetOrderAsc(sessionId)

        return workoutSession.toResponse(sets)
    }

    private fun WorkoutSession.toResponse(sets: List<SetEntry>): WorkoutSessionResponse {
        return WorkoutSessionResponse(
            id = requireNotNull(id),
            userId = requireNotNull(user?.id),
            workoutPlanId = workoutPlan?.id,
            startedAt = requireNotNull(startedAt),
            endedAt = endedAt,
            notes = notes,
            createdAt = requireNotNull(createdAt),
            updatedAt = requireNotNull(updatedAt),
            sets = sets.map { it.toResponse() },
        )
    }

    private fun SetEntry.toResponse(): SetEntryResponse {
        return SetEntryResponse(
            id = requireNotNull(id),
            exerciseId = requireNotNull(exercise?.id),
            exerciseName = requireNotNull(exercise?.name),
            setOrder = setOrder,
            reps = reps,
            weight = weight,
            durationSeconds = durationSeconds,
            distanceMeters = distanceMeters,
            createdAt = requireNotNull(createdAt),
            updatedAt = requireNotNull(updatedAt),
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WorkoutSessionService::class.java)
    }
}
