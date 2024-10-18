package com.mongmong.namo.presentation.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResult
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.domain.repositories.AuthRepository
import com.mongmong.namo.presentation.state.LoginPlatform
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.dsManager
import com.mongmong.namo.presentation.config.Constants.SUCCESS_CODE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _loginResult = MutableLiveData<LoginResult?>()
    val loginResult: LiveData<LoginResult?> = _loginResult

    private val _isLogoutComplete = MutableLiveData<Boolean>()
    val isLogoutComplete: LiveData<Boolean> = _isLogoutComplete

    private val _isQuitComplete = MutableLiveData<Boolean>()
    val isQuitComplete: LiveData<Boolean> = _isQuitComplete

    private val _refreshResponse = MutableLiveData<RefreshResponse>()
    val refreshResponse: LiveData<RefreshResponse> = _refreshResponse

    /** 로그인 */
    fun tryLogin(platform: LoginPlatform, accessToken: String, refreshToken: String) {
        viewModelScope.launch {
            // 로그인 진행
            val response = repository.postLogin(platform.platformName, LoginBody(accessToken, refreshToken))

            if (response.code != SUCCESS_CODE) return@launch

            // 로그인 정보 저장
            saveLoginPlatform(platform)
            // 토큰 저장
            saveToken(response.result)
            // userId 저장
            saveUserId(response.result.userId)

            _loginResult.value = response.result
        }
    }

    /** 토큰 재발급 */
    fun tryRefreshToken() {
        viewModelScope.launch {
            _refreshResponse.postValue(repository.postTokenRefresh())
        }
    }

    /** 로그아웃 */
    fun tryLogout() {
        viewModelScope.launch {
            if (repository.postLogout()) {
                _isLogoutComplete.value = true
                deleteToken()
            }
        }
    }

    /** 회원탈퇴 */
    fun tryQuit() {
        viewModelScope.launch {
            val isSuccess = repository.postQuit(getLoginPlatform())
            if (isSuccess) {
                _isQuitComplete.value = true
                deleteToken()
            }
        }
    }

    // 약관 동의 여부 확인
    fun checkUpdatedTerms(): Boolean {
        for (term in _loginResult.value!!.terms) {
            if (!term.check) return true // 하나라도 체크되어 있지 않을 경우 약관 동의 필요
        }
        return false
    }

    /** 토큰 */
    // 앱 내 저장된 토큰 정보 가져오기
    private fun getSavedToken(): TokenBody = runBlocking {
        val accessToken = dsManager.getAccessToken().first().orEmpty()
        val refreshToken = dsManager.getRefreshToken().first().orEmpty()
        return@runBlocking TokenBody(accessToken, refreshToken)
    }

    // 로그인 한 sdk 정보 가져오기
    private fun getLoginPlatform(): String = runBlocking {
        dsManager.getPlatform().first().orEmpty()
    }

    // 로그인 플랫폼 정보 앱 내에 저장
    private suspend fun saveLoginPlatform(platform: LoginPlatform) {
        dsManager.savePlatform(platform.platformName)
    }

    // 토큰 정보 앱 내에 저장
    private suspend fun saveToken(tokenResult: LoginResult) {
        dsManager.saveAccessToken(tokenResult.accessToken)
        dsManager.saveRefreshToken(tokenResult.refreshToken)
    }

    // userId 앱 내에 저장
    private suspend fun saveUserId(userId: Long) {
        dsManager.saveUserId(userId)
    }

    // 앱 내에 저장된 토큰 정보 삭제
    private suspend fun deleteToken() {
        dsManager.clearTokens()
    }
}
