package com.mongmong.namo.domain.usecases

import com.mongmong.namo.domain.model.Category
import com.mongmong.namo.domain.repositories.CategoryRepository
import javax.inject.Inject

class FindCategoryUseCase @Inject constructor(private var categoryRepository: CategoryRepository) {
    suspend operator fun invoke(
        categoryId: Long,
    ): Category {
        return categoryRepository.findCategoryById(categoryId)
    }
}