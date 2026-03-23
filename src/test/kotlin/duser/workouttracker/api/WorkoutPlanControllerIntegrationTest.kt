package duser.workouttracker.api

import com.fasterxml.jackson.databind.ObjectMapper
import duser.workouttracker.api.dto.AddExerciseToPlanRequest
import duser.workouttracker.api.dto.CreateWorkoutPlanRequest
import duser.workouttracker.domain.Exercise
import duser.workouttracker.domain.User
import duser.workouttracker.repository.ExerciseRepository
import duser.workouttracker.repository.SetEntryRepository
import duser.workouttracker.repository.UserRepository
import duser.workouttracker.repository.WorkoutPlanExerciseRepository
import duser.workouttracker.repository.WorkoutPlanRepository
import duser.workouttracker.repository.WorkoutSessionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class WorkoutPlanControllerIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val exerciseRepository: ExerciseRepository,
    @Autowired private val workoutPlanRepository: WorkoutPlanRepository,
    @Autowired private val workoutPlanExerciseRepository: WorkoutPlanExerciseRepository,
    @Autowired private val workoutSessionRepository: WorkoutSessionRepository,
    @Autowired private val setEntryRepository: SetEntryRepository,
) {

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @BeforeEach
    fun setUp() {
        setEntryRepository.deleteAll()
        workoutSessionRepository.deleteAll()
        workoutPlanExerciseRepository.deleteAll()
        workoutPlanRepository.deleteAll()
        exerciseRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST workout plans creates plan for existing user`() {
        val user = userRepository.save(User(email = "plan@example.com", displayName = "Plan User"))

        mockMvc.perform(
            post(ApiPaths.WorkoutPlans.ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsBytes(
                        CreateWorkoutPlanRequest(
                            userId = user.id,
                            name = "Upper Body",
                            description = "Push day",
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Upper Body"))
            .andExpect(jsonPath("$.userId").value(user.id.toString()))
            .andExpect(jsonPath("$.exercises.length()").value(0))
    }

    @Test
    fun `POST workout plan exercises adds exercise to plan`() {
        val user = userRepository.save(User(email = "plan2@example.com", displayName = "Plan User"))
        val exercise = exerciseRepository.save(Exercise(name = "Row"))
        val plan = workoutPlanRepository.save(
            duser.workouttracker.domain.WorkoutPlan(
                user = user,
                name = "Pull Day",
                description = "Back work",
            ),
        )

        mockMvc.perform(
            post("${ApiPaths.WorkoutPlans.ROOT}/${plan.id}/exercises")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsBytes(
                        AddExerciseToPlanRequest(
                            exerciseId = exercise.id,
                            exerciseOrder = 1,
                        ),
                    ),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(plan.id.toString()))
            .andExpect(jsonPath("$.exercises.length()").value(1))
            .andExpect(jsonPath("$.exercises[0].exerciseId").value(exercise.id.toString()))
            .andExpect(jsonPath("$.exercises[0].exerciseName").value("Row"))
            .andExpect(jsonPath("$.exercises[0].exerciseOrder").value(1))
    }

    @Test
    fun `GET workout plan returns plan with exercises`() {
        val user = userRepository.save(User(email = "plan3@example.com", displayName = "Plan User"))
        val exercise = exerciseRepository.save(Exercise(name = "Deadlift"))
        val plan = workoutPlanRepository.save(
            duser.workouttracker.domain.WorkoutPlan(
                user = user,
                name = "Strength",
                description = "Heavy work",
            ),
        )
        workoutPlanExerciseRepository.save(
            duser.workouttracker.domain.WorkoutPlanExercise(
                workoutPlan = plan,
                exercise = exercise,
                exerciseOrder = 1,
            ),
        )

        mockMvc.perform(get("${ApiPaths.WorkoutPlans.ROOT}/${plan.id}"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Strength"))
            .andExpect(jsonPath("$.exercises[0].exerciseName").value("Deadlift"))
    }
}
