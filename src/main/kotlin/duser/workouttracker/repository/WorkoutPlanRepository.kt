package duser.workouttracker.repository

import duser.workouttracker.domain.WorkoutPlan
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface WorkoutPlanRepository : JpaRepository<WorkoutPlan, UUID>
