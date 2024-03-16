package com.mongmong.namo.domain.usecase.diary

import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.repositories.DiaryRepository
import java.io.File
import javax.inject.Inject

class EditDiaryUseCase @Inject constructor(private val diaryRepository: DiaryRepository) {
    suspend operator fun invoke(
        diary: Diary,
        images: List<File>?
    ) {
        Log.d("EditDiaryUseCase", "$diary")
        diaryRepository.editDiary(diary, images)
    }
}
