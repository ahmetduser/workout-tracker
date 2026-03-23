package duser.workouttracker

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<WorkoutTrackerApplication>().with(TestcontainersConfiguration::class).run(*args)
}
