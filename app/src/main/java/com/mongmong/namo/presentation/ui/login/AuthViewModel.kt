package com.mongmong.namo.presentation.ui.login

import android.util.Log
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

    private val _isComplete = MutableLiveData<Boolean>()
    val isComplete: LiveData<Boolean> = _isComplete

    /** 카카오 로그인 */
    fun tryKakaoLogin(accessToken: String, refreshToken: String) {
        Log.d("kakaoToken", accessToken)
        viewModelScope.launch {
            _tokenResult.value = repository.postKakaoLogin(accessToken).result
            _tokenResult.value?.let {
                // 토큰 저장
                saveToken(it)
            }
        }
    }

    /** 네이버 로그인 */
    fun tryNaverLogin(accessToken: String, refreshToken: String) {
        Log.d("naverToken", accessToken)
        viewModelScope.launch {
            _tokenResult.value = repository.postNaverLogin(accessToken).result
            _tokenResult.value?.let {
                // 토큰 저장
                saveToken(it)
            }
        }
    }

    /** 로그아웃 */
    fun tryLogout() {
        viewModelScope.launch {
            if (repository.postLogout(getAccessToken()!!)) {
                _isComplete.postValue(true)
                deleteToken()
                //TODO: 룸디비 데이터 삭제
            }
        }
    }

    /** 토큰 */
    private fun getAccessToken(): String? {
        return ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_ACCESS_TOKEN, null)
    }

    // 토큰 정보 앱 내에 저장
    private fun saveToken(tokenResult: LoginResult) {
        // 토큰 저장
        val editor = ApplicationClass.sSharedPreferences.edit()
        editor
            .putString(ApplicationClass.X_ACCESS_TOKEN, tokenResult.accessToken)
            .putString(ApplicationClass.X_REFRESH_TOKEN, tokenResult.refreshToken)
            .apply()
    }

    // 앱 내에 저장된 토큰 정보 삭제
    private fun deleteToken() {
        ApplicationClass.sSharedPreferences.edit().clear().apply()
    }
}