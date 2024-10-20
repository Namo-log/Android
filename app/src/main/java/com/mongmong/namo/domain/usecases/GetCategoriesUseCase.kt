package com.mongmong.namo.domain.usecases

import android.util.Log
import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.domain.repositories.CategoryRepository
import javax.inject.Inject

open class GetCategoriesUseCase @Inject constructor(private var categoryRepository: CategoryRepository) {
    suspend operator fun invoke(): List<Category> {
        Log.d("GetCategoriesUseCase", "getCategories")
        return categoryRepository.getCategories()
    }
}