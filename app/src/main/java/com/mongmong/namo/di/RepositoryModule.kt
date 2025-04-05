package com.mongmong.namo.di

import com.mongmong.namo.data.repositoriyImpl.*
import com.mongmong.namo.domain.repositories.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    /** 인증 */
    @Binds
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    /** 약관 */
    @Binds
    abstract fun bindTermRepository(
        impl: TermRepositoryImpl
    ): TermRepository

    /** 일정 */
    @Binds
    abstract fun bindScheduleRepository(
        impl: ScheduleRepositoryImpl
    ): ScheduleRepository

    /** 기록 */
    @Binds
    abstract fun bindDiaryRepository(
        impl: DiaryRepositoryImpl
    ): DiaryRepository

    /** 활동 */
    @Binds
    abstract fun bindActivityRepository(
        impl: ActivityRepositoryImpl
    ): com.gradu.domain.repositories.ActivityRepository

    /** 친구 */
    @Binds
    abstract fun bindFriendRepository(
        impl: FriendRepositoryImpl
    ): FriendRepository

    /** 카테고리 */
    @Binds
    abstract fun bindCategoryRepository(
        impl: com.gradu.data.repositoriyImpl.CategoryRepositoryImpl
    ): CategoryRepository

    /** 프로필 */
    @Binds
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    /** s3 관련 */
    @Binds
    abstract fun bindImageRepository(
        impl: ImageRepositoryImpl
    ): ImageRepository
}
