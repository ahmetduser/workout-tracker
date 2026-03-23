package duser.workouttracker.api

object ApiPaths {
    const val API = "/api"
    const val ID_SEGMENT = "/{id}"

    object Exercises {
        const val ROOT = "$API/exercises"
    }

    object WorkoutPlans {
        const val ROOT = "$API/workout-plans"
        const val PLAN_ID = "planId"
        const val BY_ID = "/{$PLAN_ID}"
        const val EXERCISES = "$BY_ID/exercises"
    }

    object WorkoutSessions {
        const val ROOT = "$API/workout-sessions"
        const val SESSION_ID = "sessionId"
        const val BY_ID = "/{$SESSION_ID}"
        const val SET_ENTRIES = "$BY_ID/set-entries"
    }
}
