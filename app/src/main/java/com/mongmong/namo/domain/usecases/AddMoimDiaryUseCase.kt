package com.mongmong.namo.domain.usecases

import com.mongmong.namo.domain.repositories.DiaryRepository
import javax.inject.Inject

class AddMoimDiaryUseCase @Inject constructor(diaryRepository: DiaryRepository) {
    suspend fun execute() {

    }
}