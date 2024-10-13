package com.mongmong.namo.presentation.di

import com.mongmong.namo.domain.repositories.ActivityRepository
import com.mongmong.namo.domain.repositories.CategoryRepository
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.repositories.ImageRepository
import com.mongmong.namo.domain.usecases.AddMoimDiaryUseCase
import com.mongmong.namo.domain.usecases.FindCategoryUseCase
import com.mongmong.namo.domain.usecases.GetCategoriesUseCase
import com.mongmong.namo.domain.usecases.GetActivitiesUseCase
import com.mongmong.namo.domain.usecases.UploadImageToS3UseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun provideGetCategoriesUseCase(categoryRepository: CategoryRepository): GetCategoriesUseCase =
        GetCategoriesUseCase(categoryRepository)

    @Provides
    fun provideFindCategoryUseCase(categoryRepository: CategoryRepository): FindCategoryUseCase =
        FindCategoryUseCase(categoryRepository)

    @Provides
    fun provideUploadImageToS3UseCase(imageRepository: ImageRepository): UploadImageToS3UseCase =
        UploadImageToS3UseCase(imageRepository)

    @Provides
    fun provideAddMoimDiaryUseCase(
        diaryRepository: DiaryRepository,
        uploadImageToS3UseCase: UploadImageToS3UseCase
    ) = AddMoimDiaryUseCase(diaryRepository, uploadImageToS3UseCase)

    @Provides
    fun provideGetActivitiesUseCase(activityRepository: ActivityRepository): GetActivitiesUseCase =
        GetActivitiesUseCase(activityRepository)
}