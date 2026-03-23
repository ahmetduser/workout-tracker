package duser.workouttracker.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "workout_plan_exercise",
    indexes = [
        Index(name = "idx_workout_plan_exercise_plan_id", columnList = "workout_plan_id"),
        Index(name = "idx_workout_plan_exercise_exercise_id", columnList = "exercise_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_workout_plan_exercise_plan_exercise",
            columnNames = ["workout_plan_id", "exercise_id"],
        ),
        UniqueConstraint(
            name = "uk_workout_plan_exercise_plan_order",
            columnNames = ["workout_plan_id", "exercise_order"],
        ),
    ],
)
class WorkoutPlanExercise(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    var workoutPlan: WorkoutPlan? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    var exercise: Exercise? = null,
    @Column(name = "exercise_order", nullable = false)
    var exerciseOrder: Int = 1,
) : AuditableEntity()
