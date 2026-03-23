package duser.workouttracker.repository

import duser.workouttracker.domain.WorkoutPlanExercise
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkoutPlanExerciseRepository : JpaRepository<WorkoutPlanExercise, UUID> {
    fun findAllByWorkoutPlanIdOrderByExerciseOrderAsc(workoutPlanId: UUID): List<WorkoutPlanExercise>
    fun existsByWorkoutPlanIdAndExerciseId(workoutPlanId: UUID, exerciseId: UUID): Boolean
    fun existsByWorkoutPlanIdAndExerciseOrder(workoutPlanId: UUID, exerciseOrder: Int): Boolean
}
