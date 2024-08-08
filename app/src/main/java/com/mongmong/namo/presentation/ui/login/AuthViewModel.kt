package com.mongmong.namo.presentation.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.LoginBody
import com.mongmong.namo.domain.model.LoginResult
import com.mongmong.namo.domain.model.RefreshResponse
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.domain.repositories.AuthRepository
import com.mongmong.namo.presentation.config.LoginPlatform
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
        Log.d("${platform.platformName}Token", "accessToken: $accessToken, refreshToken: $refreshToken")
        viewModelScope.launch {
            val response = if (platform == LoginPlatform.KAKAO) {
                repository.postKakaoLogin(LoginBody(accessToken, refreshToken))
            } else {
                repository.postNaverLogin(LoginBody(accessToken, refreshToken))
            }
            if (response.code != SUCCESS_CODE) return@launch

            _loginResult.value = response.result
            // 로그인 정보 저장
            saveLoginPlatform(platform)
            // 토큰 저장
            saveToken(response.result)
        }
    }

    /** 토큰 재발급 */
    fun tryRefreshToken() {
        viewModelScope.launch {
            val tokenBody = getSavedToken()
            _refreshResponse.postValue(repository.postTokenRefresh(tokenBody.accessToken, tokenBody.refreshToken))
        }
    }

    /** 로그아웃 */
    fun tryLogout() {
        viewModelScope.launch {
            if (repository.postLogout(getAccessToken()!!)) {
                _isLogoutComplete.postValue(true)
                deleteToken()
                //TODO: 룸디비 데이터 삭제
            }
        }
    }

    /** 회원탈퇴 */
    fun tryQuit() {
        val platform = getLoginPlatform()
        Log.d("SdkInfo", "quit sdk: $platform")
        viewModelScope.launch {
            val isSuccess = if (platform == LoginPlatform.KAKAO.platformName) { // 카카오
                repository.postKakaoQuit()
            } else { // 네이버
                repository.postNaverQuit()
            }
            if (isSuccess) {
                _isQuitComplete.postValue(true)
                deleteToken()
                //TODO: 룸디비 데이터 삭제
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
    private fun getBearerToken(): String {
        return "Bearer ${getAccessToken()}"
    }

    private fun getAccessToken(): String? = runBlocking {
        dsManager.getAccessToken().first()
    }

    // 앱 내 저장된 토큰 정보 가져오기
    private fun getSavedToken(): TokenBody = runBlocking {
        val accessToken = dsManager.getAccessToken().first().orEmpty()
        val refreshToken = dsManager.getRefreshToken().first().orEmpty()
        TokenBody(accessToken, refreshToken)
    }

    // 로그인 한 sdk 정보 가져오기
    private fun getLoginPlatform(): String = runBlocking {
        dsManager.getPlatform().first().orEmpty()
    }

    // 로그인 플랫폼 정보 앱 내에 저장
    private fun saveLoginPlatform(platform: LoginPlatform) {
        viewModelScope.launch {
            dsManager.savePlatform(platform.platformName)
        }
    }

    // 토큰 정보 앱 내에 저장
    private fun saveToken(tokenResult: LoginResult) {
        viewModelScope.launch {
            dsManager.saveAccessToken(tokenResult.accessToken)
            dsManager.saveRefreshToken(tokenResult.refreshToken)
        }
    }

    // 앱 내에 저장된 토큰 정보 삭제
    private fun deleteToken() {
        viewModelScope.launch {
            dsManager.clearTokens()
        }
    }
}
