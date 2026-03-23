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
import java.util.UUID

@Entity
@Table(
    name = "workout_plan",
    indexes = [
        Index(name = "idx_workout_plan_user_id", columnList = "user_id"),
    ],
)
class WorkoutPlan(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,
    @Column(nullable = false, length = 120)
    var name: String = "",
    @Column(length = 2000)
    var description: String? = null,
    @Column(nullable = false)
    var active: Boolean = true,
) : AuditableEntity()
