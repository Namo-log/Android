package com.mongmong.namo.presentation.ui.bottom.home.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.domain.repositories.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {
    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    private val _isPostComplete = MutableLiveData<Boolean>()
    val isPostComplete: LiveData<Boolean> = _isPostComplete

    /** 카테고리 추가 */
    fun addCategory(category: Category) {
        viewModelScope.launch {
            Log.d("CategoryViewModel", "addCategory $category")
            repository.addCategory(
                category = category
            )
            _isPostComplete.postValue(true)
        }
    }

    /** 카테고리 수정 */
    fun editCategory(category: Category) {
        viewModelScope.launch {
            Log.d("CategoryViewModel", "editCategory $category")
            repository.editCategory(
                category = category
            )
            _isPostComplete.postValue(true)
        }
    }

    fun updateCategoryAfterUpload(localId: Long, isUpload: Boolean, serverId: Long, state: String) {
        viewModelScope.launch {
            repository.updateCategoryAfterUpload(localId, serverId, isUpload, state)
        }
    }
}