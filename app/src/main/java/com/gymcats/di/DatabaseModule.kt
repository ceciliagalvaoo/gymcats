package com.gymcats.di

import android.content.Context
import androidx.room.Room
import com.gymcats.data.local.database.GymCatsDatabase
import com.gymcats.data.local.database.MIGRATION_1_2
import com.gymcats.data.local.database.MIGRATION_2_3
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymCatsDatabase =
        Room.databaseBuilder(context, GymCatsDatabase::class.java, "gymcats.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides fun provideAccountDao(db: GymCatsDatabase) = db.accountDao()
    @Provides fun provideUserProfileDao(db: GymCatsDatabase) = db.userProfileDao()
    @Provides fun provideWorkoutDao(db: GymCatsDatabase) = db.workoutDao()
    @Provides fun provideExerciseLogDao(db: GymCatsDatabase) = db.exerciseLogDao()
    @Provides fun provideCycleLogDao(db: GymCatsDatabase) = db.cycleLogDao()
    @Provides fun provideProgressPhotoDao(db: GymCatsDatabase) = db.progressPhotoDao()
}
