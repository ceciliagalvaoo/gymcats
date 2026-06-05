package com.gymcats.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gymcats.data.local.dao.AccountDao
import com.gymcats.data.local.dao.CycleLogDao
import com.gymcats.data.local.dao.ExerciseLogDao
import com.gymcats.data.local.dao.ProgressPhotoDao
import com.gymcats.data.local.dao.UserProfileDao
import com.gymcats.data.local.dao.WorkoutDao
import com.gymcats.data.local.entities.Account
import com.gymcats.data.local.entities.CycleLog
import com.gymcats.data.local.entities.ExerciseLog
import com.gymcats.data.local.entities.ProgressPhoto
import com.gymcats.data.local.entities.UserProfile
import com.gymcats.data.local.entities.Workout

@Database(
    entities = [
        Account::class,
        UserProfile::class,
        Workout::class,
        ExerciseLog::class,
        CycleLog::class,
        ProgressPhoto::class
    ],
    version = 3,
    exportSchema = false
)
abstract class GymCatsDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseLogDao(): ExerciseLogDao
    abstract fun cycleLogDao(): CycleLogDao
    abstract fun progressPhotoDao(): ProgressPhotoDao
}
