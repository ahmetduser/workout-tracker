package duser.workouttracker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WorkoutTrackerApplication

fun main(args: Array<String>) {
    runApplication<WorkoutTrackerApplication>(*args)
}
