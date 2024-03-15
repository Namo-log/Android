package com.mongmong.namo.domain.usecase

import android.util.Log
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.repositories.DiaryRepository
import java.io.File
import javax.inject.Inject

class AddDiaryUseCase @Inject constructor(private val diaryRepository: DiaryRepository) {
    suspend operator fun invoke(
        diary: Diary,
        diaryLocalId: Long,
        content: String,
        images: List<File>?,
        serverId: Long
    ) {
        Log.d("AddDiaryUseCase", "$diary")
        diaryRepository.addDiary(diary, diaryLocalId, content, images, serverId)
    }
}
