package com.gradu.domain.usecases.category

import com.mongmong.namo.domain.model.CategoryModel
import com.mongmong.namo.domain.repositories.CategoryRepository
import javax.inject.Inject

class FindCategoryUseCase @Inject constructor(private var categoryRepository: CategoryRepository) {
    suspend operator fun invoke(
        categoryId: Long,
    ): CategoryModel {
        return categoryRepository.findCategoryById(categoryId)
    }
}