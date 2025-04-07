package com.gradu.domain.usecases.category

import com.gradu.domain.model.CategoryModel
import com.gradu.domain.repositories.CategoryRepository
import javax.inject.Inject

class FindCategoryUseCase @Inject constructor(private var categoryRepository: CategoryRepository) {
    suspend operator fun invoke(
        categoryId: Long,
    ): CategoryModel {
        return categoryRepository.findCategoryById(categoryId)
    }
}