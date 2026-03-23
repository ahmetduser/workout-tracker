package duser.workouttracker.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "exercise",
    indexes = [
        Index(name = "idx_exercise_name", columnList = "name", unique = true),
    ],
)
class Exercise(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,
    @Column(nullable = false, length = 120, unique = true)
    var name: String = "",
    @Column(length = 2000)
    var description: String? = null,
) : AuditableEntity()
