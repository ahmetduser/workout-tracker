package duser.workouttracker.repository

import duser.workouttracker.domain.Exercise
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ExerciseRepository : JpaRepository<Exercise, UUID> {
    fun existsByNameIgnoreCase(name: String): Boolean
}
