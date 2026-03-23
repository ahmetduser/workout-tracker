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
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "set_entry",
    indexes = [
        Index(name = "idx_set_entry_session_id", columnList = "workout_session_id"),
        Index(name = "idx_set_entry_exercise_id", columnList = "exercise_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_set_entry_session_exercise_order",
            columnNames = ["workout_session_id", "exercise_id", "set_order"],
        ),
    ],
)
class SetEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_session_id", nullable = false)
    var workoutSession: WorkoutSession? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    var exercise: Exercise? = null,
    @Column(name = "set_order", nullable = false)
    var setOrder: Int = 1,
    @Column
    var reps: Int? = null,
    @Column(precision = 7, scale = 2)
    var weight: BigDecimal? = null,
    @Column(name = "duration_seconds")
    var durationSeconds: Int? = null,
    @Column(name = "distance_meters", precision = 10, scale = 2)
    var distanceMeters: BigDecimal? = null,
) : AuditableEntity()
