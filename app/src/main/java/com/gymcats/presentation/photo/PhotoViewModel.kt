package com.gymcats.presentation.photo

import androidx.lifecycle.ViewModel
import com.gymcats.data.local.entities.ProgressPhoto
import com.gymcats.data.repository.ProgressRepository
import com.gymcats.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val progressRepository: ProgressRepository
) : ViewModel() {

    suspend fun savePhoto(path: String, workoutId: Long?) {
        val photo = ProgressPhoto(
            imagePath = path,
            date = DateUtils.today(),
            workoutId = workoutId
        )
        progressRepository.insertPhoto(photo)
    }
}
