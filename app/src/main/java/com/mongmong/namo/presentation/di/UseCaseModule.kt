package com.mongmong.namo.presentation.di

import com.mongmong.namo.domain.repositories.CategoryRepository
import com.mongmong.namo.domain.usecase.GetCategoriesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {
    @Provides
    fun provideGetCategoriesUseCase(categoryRepository: CategoryRepository): GetCategoriesUseCase = GetCategoriesUseCase(categoryRepository)
}