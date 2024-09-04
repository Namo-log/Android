package com.mongmong.namo.presentation.ui.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.domain.repositories.CategoryRepository
import com.mongmong.namo.domain.usecase.GetCategoriesUseCase
import com.mongmong.namo.presentation.config.CategoryColor
import com.mongmong.namo.presentation.state.RoomState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : ViewModel() {
    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    private val _categoryList = MutableLiveData<List<Category>>(emptyList())
    val categoryList: LiveData<List<Category>> = _categoryList

    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    private val _completeState = MutableLiveData<RoomState>()
    val completeState: LiveData<RoomState> = _completeState

    private val _color = MutableLiveData<CategoryColor?>(null)
    val color: LiveData<CategoryColor?> = _color

    private val _selectedPalettePosition = MutableLiveData<Int?>() // 팔레트 -> 기본 색상 선택 시 사용될 변수
    val selectedPalettePosition: LiveData<Int?> = _selectedPalettePosition

    private val _canDeleteCategory = MutableLiveData<Boolean>(true)
    val canDeleteCategory: LiveData<Boolean> = _canDeleteCategory

    /** 카테고리 조회 */
    fun getCategories() {
        viewModelScope.launch {
            Log.d("CategoryViewModel", "getCategories")
            _categoryList.value = getCategoriesUseCase.invoke()
        }
    }

    /** 카테고리 추가 */
    fun addCategory() {
        viewModelScope.launch {
            Log.d("CategoryViewModel", "addCategory ${_category.value}")
            _isComplete.postValue(repository.addCategory(
                category = _category.value!!
            ))
            _completeState.value = RoomState.ADDED
        }
    }

    /** 카테고리 수정 */
    fun editCategory() {
        viewModelScope.launch {
            Log.d("CategoryViewModel", "editCategory ${_category.value}")
            _isComplete.postValue(repository.editCategory(
                category = _category.value!!
            ))
            _completeState.value = RoomState.EDITED
        }
    }

    /** 카테고리 삭제 */
    fun deleteCategory() {
        viewModelScope.launch {
            Log.d("CategoryViewModel", "deleteCategory ${_category.value}")
            _isComplete.postValue(repository.deleteCategory(
                category = _category.value!!
            ))
            _completeState.value = RoomState.DELETED
        }
    }

    fun setCategory(category: Category) {
        _category.value = category
        if (category.paletteId != 0) {
            _color.value = CategoryColor.findCategoryColorByPaletteId(category.paletteId)
        }
    }

    fun setDeliable(canDelete: Boolean) {
        _canDeleteCategory.value = canDelete
    }

    fun updateTitle(title: String) {
        _category.value = _category.value?.copy(
            name = title
        )
    }

    fun updateCategoryColor(color: CategoryColor) {
        _color.value = color
        _category.value = _category.value?.copy(paletteId = color.paletteId)
    }

    fun updateIsShare(isShare: Boolean) {
        _category.value!!.isShare = isShare
    }

    fun updateSelectedPalettePosition(pos: Int?) {
        _selectedPalettePosition.value = pos
    }

    fun isValidInput(): Boolean {
        return (!_category.value?.name.isNullOrEmpty()) && (_color.value != null)
    }

    fun updateCategoryAfterUpload(localId: Long, isUpload: Boolean, serverId: Long, state: String) {
        viewModelScope.launch {
            repository.updateCategoryAfterUpload(localId, serverId, isUpload, state)
        }
    }
}