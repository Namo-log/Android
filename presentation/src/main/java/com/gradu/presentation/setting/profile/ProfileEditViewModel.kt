package com.gradu.presentation.ui.setting.profile

import android.net.Uri
import androidx.lifecycle.*
import com.mongmong.namo.domain.model.BaseResponse
import com.mongmong.namo.domain.model.ProfileModel
import com.mongmong.namo.domain.usecases.profile.EditProfileUseCase
import com.mongmong.namo.presentation.enums.CategoryColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val editProfileUseCase: EditProfileUseCase
) : ViewModel() {
    private val _name = MutableLiveData<String>("")
    val name: LiveData<String> = _name

    private val _profileImage = MutableLiveData<String?>(null)
    val profileImage: LiveData<String?> = _profileImage

    val nickname = MutableLiveData<String>("")

    private val _birthday = MutableLiveData<String>("")
    val birthday: LiveData<String> = _birthday

    val intro = MutableLiveData<String>("")

    private val _color = MutableLiveData<CategoryColor?>(null)
    val color: LiveData<CategoryColor?> = _color

    // 닉네임, 이름 공개 여부
    private val _isPublicFields = MutableLiveData<Map<String,Boolean>>(
        mutableMapOf("name" to false, "birth" to false)
    )
    val isPublicFields: LiveData<Map<String, Boolean>> = _isPublicFields

    // 닉네임 유효성 검사
    val isNicknameValid: LiveData<Boolean> = Transformations.map(nickname) {
        validateNickname(it)
    }

    private val _isEditComplete = MutableLiveData<BaseResponse>()
    val isEditComplete: LiveData<BaseResponse> = _isEditComplete

    // 자기소개 글자 수 카운트
    val introLength: LiveData<Int> = Transformations.map(intro) { it.length }

    private var _initialProfileData: ProfileModel? = null

    // 수정 버튼 활성화 여부
    val isEditEnabled = MediatorLiveData<Boolean>().apply {
        addSource(isNicknameValid) { checkFormValidation() }
        addSource(profileImage) { checkFormValidation() }
        addSource(nickname) { checkFormValidation() }
        addSource(birthday) { checkFormValidation() }
        addSource(intro) { checkFormValidation() }
        addSource(color) { checkFormValidation() }
        addSource(isPublicFields) { checkFormValidation() }
    }

    fun setInitialProfileInfo(initialProfile: ProfileModel) {
        _initialProfileData = initialProfile

        _profileImage.value = initialProfile.profileUrl
        _name.value = initialProfile.name
        this.nickname.value = initialProfile.nickname
        _birthday.value = initialProfile.birth
        this.intro.value = initialProfile.introduction
        _color.value = initialProfile.favoriteColor
        _isPublicFields.value = mutableMapOf(
            "name" to initialProfile.isNamePublic,
            "birth" to initialProfile.isBirthPublic
        )
    }

    fun setColor(categoryColor: CategoryColor?) { _color.value = categoryColor }

    fun setProfileImage(uri: Uri) { _profileImage.value = uri.toString() }

    fun setBirthday(year: String, month: String, day: String) { _birthday.value = "$year-$month-$day" }

    fun setFieldPublic(field: String, isPublic: Boolean) {
        _isPublicFields.value = _isPublicFields.value?.toMutableMap()?.apply {
            this[field] = isPublic
        }
    }

    fun getFormattedBirthday(): String {
        return _birthday.value?.replace("-", "/") ?: ""
    }

    private fun validateNickname(nickname: String): Boolean {
        val regex = "^[a-zA-Z0-9가-힣]{1,12}$".toRegex()
        return nickname.matches(regex)
    }

    private fun isProfileChanged(): Boolean {
        val initial = _initialProfileData ?: return false
        return (profileImage.value != initial.profileUrl ||
                nickname.value != initial.nickname ||
                birthday.value != initial.birth ||
                intro.value != initial.introduction ||
                color.value != initial.favoriteColor ||
                isPublicFields.value?.get("name") != initial.isNamePublic ||
                isPublicFields.value?.get("birth") != initial.isBirthPublic)
    }

    private fun checkFormValidation() {
        isEditEnabled.value = isNicknameValid.value == true && isProfileChanged()
    }

    fun editProfile() {
        viewModelScope.launch {
            _isEditComplete.value = editProfileUseCase.invoke(
                nickname = nickname.value ?: "",
                birthday = birthday.value ?: "",
                colorId = color.value?.colorId ?: 0,
                intro = intro.value ?: "",
                profileImage = profileImage.value ?: "",
                isNamePublic = isPublicFields.value?.get("name") ?: false,
                isBirthdayPublic = isPublicFields.value?.get("birth") ?: false
            )
        }
    }
}