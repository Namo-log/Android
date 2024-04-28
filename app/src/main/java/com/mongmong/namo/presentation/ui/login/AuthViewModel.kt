package com.mongmong.namo.presentation.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.LoginResult
import com.mongmong.namo.domain.repositories.AuthRepository
import com.mongmong.namo.presentation.config.ApplicationClass
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _tokenResult = MutableLiveData<LoginResult?>()
    val tokenResult: LiveData<LoginResult?> = _tokenResult

    /** 카카오 로그인 */
    fun tryKakaoLogin(accessToken: String, refreshToken: String) {
        viewModelScope.launch {
            _tokenResult.value = repository.postKakaoLogin(accessToken, refreshToken).result
            _tokenResult.value?.let {
                // 토큰 저장
                saveToken(it)
            }
        }
    }

    /** 토큰 정보 앱 내에 저장 */
    private fun saveToken(tokenResult: LoginResult) {
        // 토큰 저장
        val editor = ApplicationClass.sSharedPreferences.edit()
        editor
            .putString(ApplicationClass.X_ACCESS_TOKEN, tokenResult.accessToken)
            .putString(ApplicationClass.X_REFRESH_TOKEN, tokenResult.refreshToken)
            .apply()
    }
}