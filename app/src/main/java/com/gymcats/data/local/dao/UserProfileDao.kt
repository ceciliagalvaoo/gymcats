package com.gymcats.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymcats.data.local.entities.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE accountId = :accountId LIMIT 1")
    fun getProfile(accountId: Long): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE accountId = :accountId LIMIT 1")
    suspend fun getProfileNow(accountId: Long): UserProfile?

    @Query("UPDATE user_profile SET accountId = :newAccountId WHERE accountId = 0")
    suspend fun claimOrphanedProfile(newAccountId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)
}
