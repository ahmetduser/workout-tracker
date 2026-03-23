package duser.workouttracker.api

import duser.workouttracker.api.dto.CreateExerciseRequest
import duser.workouttracker.api.dto.ExerciseResponse
import duser.workouttracker.service.ExerciseService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping(ApiPaths.Exercises.ROOT)
class ExerciseController(
    private val exerciseService: ExerciseService,
) {

    @PostMapping
    fun createExercise(@Valid @RequestBody request: CreateExerciseRequest): ResponseEntity<ExerciseResponse> {
        val response = exerciseService.createExercise(request)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path(ApiPaths.ID_SEGMENT)
            .buildAndExpand(response.id)
            .toUri()

        return ResponseEntity.created(location).body(response)
    }

    @GetMapping
    fun listExercises(): List<ExerciseResponse> {
        return exerciseService.listExercises()
    }
}
