package duser.workouttracker.api

import com.fasterxml.jackson.databind.ObjectMapper
import duser.workouttracker.api.dto.CreateExerciseRequest
import duser.workouttracker.repository.ExerciseRepository
import org.junit.jupiter.api.Assertions.assertEquals
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
class ExerciseControllerIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val exerciseRepository: ExerciseRepository,
) {

    private val objectMapper = ObjectMapper().findAndRegisterModules()

    @BeforeEach
    fun setUp() {
        exerciseRepository.deleteAll()
    }

    @Test
    fun `POST exercises creates exercise`() {
        mockMvc.perform(
            post(ApiPaths.Exercises.ROOT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsBytes(
                        CreateExerciseRequest(
                            name = "Bench Press",
                            description = "Chest compound",
                        ),
                    ),
                ),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Bench Press"))
            .andExpect(jsonPath("$.description").value("Chest compound"))

        assertEquals(1, exerciseRepository.count())
    }

    @Test
    fun `GET exercises lists created exercises`() {
        exerciseRepository.saveAll(
            listOf(
                duser.workouttracker.domain.Exercise(name = "Squat"),
                duser.workouttracker.domain.Exercise(name = "Bench Press"),
            ),
        )

        mockMvc.perform(get(ApiPaths.Exercises.ROOT))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].name").value("Bench Press"))
            .andExpect(jsonPath("$[1].name").value("Squat"))
    }
}
