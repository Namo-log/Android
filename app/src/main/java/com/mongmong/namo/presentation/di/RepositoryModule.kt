package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.datasource.category.LocalCategoryDataSource
import com.mongmong.namo.data.datasource.category.RemoteCategoryDataSource
import com.mongmong.namo.data.datasource.schedule.LocalScheduleDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.datasource.diary.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.datasource.group.GroupDataSource
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.repositoriyImpl.CategoryRepositoryImpl
import com.mongmong.namo.data.repositoriyImpl.DiaryRepositoryImpl
import com.mongmong.namo.data.repositoriyImpl.GroupRepositoryImpl
import com.mongmong.namo.data.repositoriyImpl.ScheduleRepositoryImpl
import com.mongmong.namo.domain.repositories.CategoryRepository
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.repositories.GroupRepository
import com.mongmong.namo.domain.repositories.ScheduleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    /** 일정 */
    @Provides
    fun provideScheduleRepository(
        localScheduleDataSource: LocalScheduleDataSource,
        remoteScheduleDataSource: RemoteScheduleDataSource,
        networkChecker: NetworkChecker
    ): ScheduleRepository = ScheduleRepositoryImpl(localScheduleDataSource, remoteScheduleDataSource, networkChecker)

    /** 기록 */
    @Provides
    fun provideDiaryRepository(
        localDiaryDataSource: LocalDiaryDataSource,
        remoteDiaryDataSource: RemoteDiaryDataSource,
        diaryDao: DiaryDao,
        diaryService: DiaryApiService,
        networkChecker: NetworkChecker
    ): DiaryRepository = DiaryRepositoryImpl(
        localDiaryDataSource,
        remoteDiaryDataSource,
        diaryDao,
        diaryService,
        networkChecker
    )

    /** 카테고리 */
    @Provides
    fun provideCategoryRepository(
        localCategoryDataSource: LocalCategoryDataSource,
        remoteCategoryDataSource: RemoteCategoryDataSource,
        networkChecker: NetworkChecker
    ): CategoryRepository = CategoryRepositoryImpl(localCategoryDataSource, remoteCategoryDataSource, networkChecker)

    /** 그룹 */
    @Provides
    fun provideGroupRepository(
        groupDataSource: GroupDataSource
    ): GroupRepository = GroupRepositoryImpl(
        groupDataSource
    )
}