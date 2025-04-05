package com.mongmong.namo.di

import com.mongmong.namo.domain.repositories.ActivityRepository
import com.mongmong.namo.domain.repositories.AuthRepository
import com.mongmong.namo.domain.repositories.CategoryRepository
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.repositories.FriendRepository
import com.mongmong.namo.domain.repositories.ImageRepository
import com.mongmong.namo.domain.repositories.ProfileRepository
import com.mongmong.namo.domain.usecases.friend.AcceptFriendRequestUseCase
import com.mongmong.namo.domain.usecases.diary.AddMoimDiaryUseCase
import com.mongmong.namo.domain.usecases.friend.DenyFriendRequestUseCase
import com.mongmong.namo.domain.usecases.category.FindCategoryUseCase
import com.mongmong.namo.domain.usecases.category.GetCategoriesUseCase
import com.mongmong.namo.domain.usecases.activity.GetActivitiesUseCase
import com.mongmong.namo.domain.usecases.auth.RequestRegisterUseCase
import com.mongmong.namo.domain.usecases.friend.GetFriendsUseCase
import com.mongmong.namo.domain.usecases.image.UploadImageToS3UseCase
import com.mongmong.namo.domain.usecases.profile.EditProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun provideRequestRegisterUseCase(
        authRepository: AuthRepository,
        uploadImageToS3UseCase: UploadImageToS3UseCase
    ): RequestRegisterUseCase =
        RequestRegisterUseCase(authRepository, uploadImageToS3UseCase)

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

    @Provides
    fun provideGetFriendsUseCase(friendRepository: FriendRepository): GetFriendsUseCase =
        GetFriendsUseCase(friendRepository)

    @Provides
    fun provideAcceptFriendRequestUseCase(friendRepository: FriendRepository): AcceptFriendRequestUseCase =
        AcceptFriendRequestUseCase(friendRepository)

    @Provides
    fun provideDenyFriendRequestUseCase(friendRepository: FriendRepository): DenyFriendRequestUseCase =
        DenyFriendRequestUseCase(friendRepository)

    @Provides
    fun provideEditProfileUseCase(profileRepository: ProfileRepository, uploadImageToS3UseCase: UploadImageToS3UseCase): EditProfileUseCase =
        EditProfileUseCase(profileRepository, uploadImageToS3UseCase)
}