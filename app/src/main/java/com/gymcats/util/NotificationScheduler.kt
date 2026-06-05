package com.gymcats.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.concurrent.TimeUnit

fun scheduleWorkoutReminder(context: Context, days: List<DayOfWeek>, hour: Int) {
    WorkManager.getInstance(context).cancelAllWorkByTag("workout_reminder")
    days.forEach { day ->
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            val targetDow = when (day) {
                DayOfWeek.SUNDAY -> Calendar.SUNDAY
                DayOfWeek.MONDAY -> Calendar.MONDAY
                DayOfWeek.TUESDAY -> Calendar.TUESDAY
                DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
                DayOfWeek.THURSDAY -> Calendar.THURSDAY
                DayOfWeek.FRIDAY -> Calendar.FRIDAY
                DayOfWeek.SATURDAY -> Calendar.SATURDAY
            }
            set(Calendar.DAY_OF_WEEK, targetDow)
            if (before(now)) add(Calendar.WEEK_OF_YEAR, 1)
        }
        val delay = target.timeInMillis - now.timeInMillis
        val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("workout_reminder")
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}

fun scheduleSymptomsReminder(context: Context) {
    val request = OneTimeWorkRequestBuilder<SymptomsReminderWorker>()
        .setInitialDelay(30, TimeUnit.MINUTES)
        .addTag("symptoms_reminder")
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "symptoms_reminder",
        ExistingWorkPolicy.REPLACE,
        request
    )
}

fun scheduleCycleReminder(context: Context, lastPeriodDate: LocalDate, cycleLength: Int) {
    val nextPeriod = lastPeriodDate.plusDays(cycleLength.toLong())
    val reminderDate = nextPeriod.minusDays(1)
    val now = LocalDate.now()
    if (reminderDate.isBefore(now)) return
    val delayDays = ChronoUnit.DAYS.between(now, reminderDate)
    val request = OneTimeWorkRequestBuilder<CycleReminderWorker>()
        .setInitialDelay(delayDays, TimeUnit.DAYS)
        .addTag("cycle_reminder")
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        "cycle_reminder",
        ExistingWorkPolicy.REPLACE,
        request
    )
}
