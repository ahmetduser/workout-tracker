package duser.workouttracker.api

import com.fasterxml.jackson.databind.ObjectMapper
import duser.workouttracker.api.dto.CreateWorkoutSessionRequest
import duser.workouttracker.api.dto.LogSetEntryRequest
import duser.workouttracker.domain.Exercise
import duser.workouttracker.domain.User
import duser.workouttracker.domain.WorkoutPlan
import duser.workouttracker.repository.ExerciseRepository
import duser.workouttracker.repository.SetEntryRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WeeklyWorkoutSummaryRepository
import duser.workouttracker.repository.WorkoutPlanExerciseRepository
import duser.workouttracker.repository.WorkoutPlanRepository
import duser.workouttracker.repository.WorkoutSessionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal

@SpringBootTest
@AutoConfigureMockMvc
class WorkoutSessionControllerIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val exerciseRepository: ExerciseRepository,
    @Autowired private val workoutPlanRepository: WorkoutPlanRepository,
    @Autowired private val workoutPlanExerciseRepository: WorkoutPlanExerciseRepository,
    @Autowired private val workoutSessionRepository: WorkoutSessionRepository,
    @Autowired private val setEntryRepository: SetEntryRepository,
    @Autowired private val weeklyWorkoutSummaryRepository: WeeklyWorkoutSummaryRepository,
) {
    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @BeforeEach
    fun setUp() {
        weeklyWorkoutSummaryRepository.deleteAll()
        setEntryRepository.deleteAll()
        workoutSessionRepository.deleteAll()
        workoutPlanExerciseRepository.deleteAll()
        workoutPlanRepository.deleteAll()
        exerciseRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST workout sessions creates session`() {
        val user = userRepository.save(User(email = "session@example.com", displayName = "Session User"))
        val plan = workoutPlanRepository.save(
            WorkoutPlan(
                user = user,
                name = "Push",
                description = "Push plan",
            ),
        )

        mockMvc.perform(
            post(ApiPaths.WorkoutSessions.ROOT)
                .with(oauth2Login())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsBytes(
                        CreateWorkoutSessionRequest(
                            userId = user.id,
                            workoutPlanId = plan.id,
                            notes = "Morning session",
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.userId").value(user.id.toString()))
            .andExpect(jsonPath("$.workoutPlanId").value(plan.id.toString()))
            .andExpect(jsonPath("$.notes").value("Morning session"))
    }

    @Test
    fun `POST set entries logs a set for session`() {
        val user = userRepository.save(User(email = "session2@example.com", displayName = "Session User"))
        val exercise = exerciseRepository.save(Exercise(name = "Bench Press"))
        val session = workoutSessionRepository.save(
            duser.workouttracker.domain.WorkoutSession(
                user = user,
                startedAt = java.time.Instant.parse("2026-03-23T10:00:00Z"),
                notes = "Session",
            ),
        )

        mockMvc.perform(
            post("${ApiPaths.WorkoutSessions.ROOT}/${session.id}/set-entries")
                .with(oauth2Login())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsBytes(
                        LogSetEntryRequest(
                            exerciseId = exercise.id,
                            setOrder = 1,
                            reps = 8,
                            weight = BigDecimal("100.50"),
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.sets.length()").value(1))
            .andExpect(jsonPath("$.sets[0].exerciseName").value("Bench Press"))
            .andExpect(jsonPath("$.sets[0].reps").value(8))
            .andExpect(jsonPath("$.sets[0].weight").value(100.50))
    }

    @Test
    fun `GET workout session returns session with sets`() {
        val user = userRepository.save(User(email = "session3@example.com", displayName = "Session User"))
        val exercise = exerciseRepository.save(Exercise(name = "Squat"))
        val session = workoutSessionRepository.save(
            duser.workouttracker.domain.WorkoutSession(
                user = user,
                startedAt = java.time.Instant.parse("2026-03-23T10:00:00Z"),
                notes = "Session",
            ),
        )
        setEntryRepository.save(
            duser.workouttracker.domain.SetEntry(
                workoutSession = session,
                exercise = exercise,
                setOrder = 1,
                reps = 5,
                weight = BigDecimal("140.00"),
            ),
        )

        mockMvc.perform(get("${ApiPaths.WorkoutSessions.ROOT}/${session.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(session.id.toString()))
            .andExpect(jsonPath("$.sets[0].exerciseName").value("Squat"))
    }

    @Test
    fun `GET workout history returns sessions for a user`() {
        val user = userRepository.save(User(email = "session4@example.com", displayName = "Session User"))
        val session = workoutSessionRepository.save(
            duser.workouttracker.domain.WorkoutSession(
                user = user,
                startedAt = java.time.Instant.parse("2026-03-23T10:00:00Z"),
                notes = "Session",
            ),
        )

        mockMvc.perform(get(ApiPaths.WorkoutSessions.ROOT).param("userId", user.id.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(session.id.toString()))
            .andExpect(jsonPath("$[0].userId").value(user.id.toString()))
    }
}
