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
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "workout_session",
    indexes = [
        Index(name = "idx_workout_session_user_id", columnList = "user_id"),
        Index(name = "idx_workout_session_plan_id", columnList = "workout_plan_id"),
    ],
)
class WorkoutSession(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id")
    var workoutPlan: WorkoutPlan? = null,
    @Column(name = "started_at", nullable = false)
    var startedAt: Instant? = null,
    @Column(name = "ended_at")
    var endedAt: Instant? = null,
    @Column(length = 2000)
    var notes: String? = null,
) : AuditableEntity()
