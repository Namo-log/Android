package com.gradu.presentation.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mongmong.namo.domain.model.ProfileModel
import com.mongmong.namo.domain.repositories.AuthRepository
import com.mongmong.namo.domain.repositories.ProfileRepository
import com.mongmong.namo.ApplicationClass
import com.mongmong.namo.ApplicationClass.Companion.dsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _profile = MutableLiveData<ProfileModel>()
    val profile: LiveData<ProfileModel> = _profile

    val version = ApplicationClass.VERSION

    private val _isLogoutComplete = MutableLiveData<Boolean>()
    val isLogoutComplete: LiveData<Boolean> = _isLogoutComplete

    private val _isQuitComplete = MutableLiveData<Boolean>()
    val isQuitComplete: LiveData<Boolean> = _isQuitComplete

    init {
        getProfile()
    }

    /** 내 정보 조회 */
    fun getProfile() {
        viewModelScope.launch {
            _profile.value = profileRepository.getProfile()
        }
    }

    /** 로그아웃 */
    fun tryLogout() {
        viewModelScope.launch {
            if (authRepository.postLogout()) {
                _isLogoutComplete.value = true
                deleteToken()
            }
        }
    }

    /** 회원탈퇴 */
    fun tryQuit() {
        viewModelScope.launch {
            val isSuccess = authRepository.postQuit(getLoginPlatform())
            if (isSuccess) {
                _isQuitComplete.value = true
                deleteToken()
            }
        }
    }

    // 로그인 한 sdk 정보 가져오기
    private fun getLoginPlatform(): String = runBlocking {
        dsManager.getPlatform().first().orEmpty()
    }

    // 앱 내에 저장된 토큰 정보 삭제
    private suspend fun deleteToken() {
        dsManager.clearTokens()
    }
}