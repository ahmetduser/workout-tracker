package duser.workouttracker.repository

import duser.workouttracker.domain.SetEntry
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SetEntryRepository : JpaRepository<SetEntry, UUID> {
    fun findAllByWorkoutSessionIdOrderBySetOrderAsc(workoutSessionId: UUID): List<SetEntry>
    fun findAllByWorkoutSessionIdIn(workoutSessionIds: Collection<UUID>): List<SetEntry>
}
