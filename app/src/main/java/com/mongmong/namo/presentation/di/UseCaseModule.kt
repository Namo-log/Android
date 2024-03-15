package com.mongmong.namo.presentation.di

import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.usecase.AddDiaryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun provideAddDiaryUseCase(diaryRepository: DiaryRepository): AddDiaryUseCase =
        AddDiaryUseCase(diaryRepository)
}