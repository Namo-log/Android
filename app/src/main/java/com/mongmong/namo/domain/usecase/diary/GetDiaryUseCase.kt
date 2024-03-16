package com.mongmong.namo.domain.usecase.diary

import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.repositories.DiaryRepository
import java.io.File
import javax.inject.Inject

class GetDiaryUseCase @Inject constructor(private val diaryRepository: DiaryRepository) {
    suspend operator fun invoke(diaryId: Long): Diary {
        Log.d("GetDiaryUseCase", "$diaryId")
        return diaryRepository.getDiary(diaryId)
    }
}
