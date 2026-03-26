package duser.workouttracker.job

import duser.workouttracker.service.WeeklyWorkoutSummaryService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class WeeklyWorkoutSummaryJob(
    private val weeklyWorkoutSummaryService: WeeklyWorkoutSummaryService,
) {

    @Scheduled(
        cron = $$"${app.jobs.weekly-summary.cron:0 0 2 * * MON}",
        zone = $$"${app.jobs.weekly-summary.zone:UTC}",
    )
    fun generateWeeklySummary() {
        logger.info("Starting weekly workout summary job")
        val generatedCount = weeklyWorkoutSummaryService.generatePreviousWeekSummary()
        logger.info("Completed weekly workout summary job generatedCount={}", generatedCount)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WeeklyWorkoutSummaryJob::class.java)
    }
}
