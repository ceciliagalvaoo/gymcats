package com.gymcats.util

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SymptomsReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        NotificationHelper.send(
            applicationContext,
            "GymCats",
            "Lembre-se de registrar como você se sentiu hoje.",
            id = 2
        )
        return Result.success()
    }
}
