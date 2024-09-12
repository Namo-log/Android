package com.mongmong.namo.domain.usecases

import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.domain.repositories.CategoryRepository
import javax.inject.Inject

class FindCategoryUseCase @Inject constructor(private var categoryRepository: CategoryRepository) {
    suspend operator fun invoke(
        localId: Long,
        serverId: Long
    ): Category {
        return categoryRepository.findCategoryById(localId, serverId)
    }
}