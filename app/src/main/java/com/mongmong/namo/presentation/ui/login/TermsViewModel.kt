package com.mongmong.namo.presentation.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.TermBody
import com.mongmong.namo.domain.repositories.TermRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val repository: TermRepository
) : ViewModel() {
    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    /** 약관 동의 */
    fun tryCheckTerms(termBody: TermBody) {
        viewModelScope.launch {
            _isComplete.postValue(repository.postTerms(termBody))
        }
    }
}