package com.gradu.domain.usecases.category

import com.gradu.domain.model.CategoryModel
import com.gradu.domain.repositories.CategoryRepository
import javax.inject.Inject

open class GetCategoriesUseCase @Inject constructor(private var categoryRepository: CategoryRepository) {
    suspend operator fun invoke(): List<CategoryModel> {
        return categoryRepository.getCategories()
    }
}