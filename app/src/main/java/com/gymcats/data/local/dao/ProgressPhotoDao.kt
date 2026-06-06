package com.gymcats.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.gymcats.data.local.entities.ProgressPhoto
import kotlinx.coroutines.flow.Flow

data class ProgressPhotoWithWorkout(
    val id: Long,
    val imagePath: String,
    val date: String,
    val workoutId: Long? = null,
    val workoutName: String? = null,
    val cycleNotes: String? = null
)

@Dao
interface ProgressPhotoDao {
    @Query("""
        SELECT p.id, p.imagePath, p.date, p.workoutId, w.name AS workoutName, cl.notes AS cycleNotes
        FROM progress_photos p
        LEFT JOIN workouts w ON p.workoutId = w.id
        LEFT JOIN cycle_logs cl ON cl.workoutId = p.workoutId
        WHERE p.accountId = :accountId AND p.date >= :fromDate
        ORDER BY p.date DESC, p.id DESC
    """)
    fun getPhotosFromDate(accountId: Long, fromDate: String): Flow<List<ProgressPhotoWithWorkout>>

    @Query("UPDATE progress_photos SET accountId = :newAccountId WHERE accountId = 0")
    suspend fun claimOrphanedPhotos(newAccountId: Long): Int

    @Query("SELECT COUNT(*) FROM progress_photos WHERE accountId = :accountId")
    suspend fun countPhotos(accountId: Long): Int

    @Insert
    suspend fun insertPhoto(photo: ProgressPhoto)

    @Delete
    suspend fun deletePhoto(photo: ProgressPhoto)
}
