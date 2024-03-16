package com.mongmong.namo.domain.usecase.diary

import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.repositories.DiaryRepository
import java.io.File
import javax.inject.Inject

class AddDiaryUseCase @Inject constructor(private val diaryRepository: DiaryRepository) {
    suspend operator fun invoke(
        diary: Diary,
        images: List<File>?
    ) {
        Log.d("AddDiaryUseCase", "$diary")
        diaryRepository.addDiary(diary, images)
    }
}
