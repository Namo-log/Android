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
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.config.LoginPlatform
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
            if (platform == LoginPlatform.KAKAO) {
                _loginResult.value = repository.postKakaoLogin(LoginBody(accessToken, refreshToken)).result
            } else {
                _loginResult.value = repository.postNaverLogin(LoginBody(accessToken, refreshToken)).result
            }
            _loginResult.value?.let {
                saveLoginPlatform(platform)
                // 토큰 저장
                saveToken(it)
            }
        }
    }

    /** 토큰 재발급 */
    fun tryRefreshToken() {
        val tokenBody = getSavedToken()
        viewModelScope.launch {
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

    /** 토큰 */
    private fun getBearerToken(): String {
        return "Bearer ${getAccessToken()}"
    }

    private fun getAccessToken(): String? {
        return ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_ACCESS_TOKEN, null)
    }

    // 앱 내 저장된 토큰 정보 가져오기
    private fun getSavedToken(): TokenBody {
        val spf = ApplicationClass.sSharedPreferences
        return TokenBody(spf.getString(ApplicationClass.X_ACCESS_TOKEN, null).toString(), spf.getString(ApplicationClass.X_REFRESH_TOKEN, null).toString())
    }

    // 로그인 한 sdk 정보 가져오기
    private fun getLoginPlatform(): String {
        val spf = ApplicationClass.sSharedPreferences
        return spf.getString(ApplicationClass.SDK_PLATFORM, LoginPlatform.KAKAO.platformName)!!
    }

    // 로그인 플랫폼 정보 앱 내에 저장
    private fun saveLoginPlatform(platform: LoginPlatform) {
        ApplicationClass.sSharedPreferences.edit()
            .putString(ApplicationClass.SDK_PLATFORM, platform.platformName)
            .apply()
    }

    // 토큰 정보 앱 내에 저장
    private fun saveToken(tokenResult: LoginResult) {
        // 토큰 저장
        ApplicationClass.sSharedPreferences.edit()
            .putString(ApplicationClass.X_ACCESS_TOKEN, tokenResult.accessToken)
            .putString(ApplicationClass.X_REFRESH_TOKEN, tokenResult.refreshToken)
            .apply()
    }

    // 앱 내에 저장된 토큰 정보 삭제
    private fun deleteToken() {
        ApplicationClass.sSharedPreferences.edit().clear().apply()
    }
}