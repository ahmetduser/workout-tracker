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
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "weekly_workout_summary",
    indexes = [
        Index(name = "idx_weekly_workout_summary_user_id", columnList = "user_id"),
        Index(name = "idx_weekly_workout_summary_week_start_date", columnList = "week_start_date"),
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_weekly_workout_summary_user_week",
            columnNames = ["user_id", "week_start_date"],
        ),
    ],
)
class WeeklyWorkoutSummary(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null,
    @Column(name = "week_start_date", nullable = false)
    var weekStartDate: LocalDate? = null,
    @Column(name = "week_end_date", nullable = false)
    var weekEndDate: LocalDate? = null,
    @Column(name = "total_sessions", nullable = false)
    var totalSessions: Int = 0,
    @Column(name = "total_sets", nullable = false)
    var totalSets: Int = 0,
    @Column(name = "total_reps", nullable = false)
    var totalReps: Int = 0,
    @Column(name = "total_weight_volume", nullable = false, precision = 12, scale = 2)
    var totalWeightVolume: BigDecimal = BigDecimal.ZERO,
    @Column(name = "generated_at", nullable = false)
    var generatedAt: Instant? = null,
) : AuditableEntity()
