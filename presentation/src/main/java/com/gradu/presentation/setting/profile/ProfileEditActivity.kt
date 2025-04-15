package com.gradu.presentation.ui.setting.profile

import android.net.Uri
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityProfileEditBinding
import com.mongmong.namo.domain.model.ProfileModel
import com.mongmong.namo.presentation.config.BaseActivity
import com.gradu.presentation.common.ConfirmDialog
import com.gradu.presentation.ui.community.moim.diary.MoimDiaryDetailActivity.Companion.BACK_BUTTON_ACTION
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileEditActivity : BaseActivity<ActivityProfileEditBinding>(R.layout.activity_profile_edit),
    ConfirmDialog.ConfirmDialogInterface {
    private val viewModel: ProfileEditViewModel by viewModels()

    override fun setup() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setProfileInfo()
        initClickListener()
        initObservers()
    }

    private fun setProfileInfo() {
        val profileData = intent.getSerializableExtra(PROFILE_KEY) as ProfileModel
        viewModel.setInitialProfileInfo(profileData)
    }

    private fun initClickListener() {
        binding.profileEditProfileImgIv.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.profileEditColorCv.setOnClickListener {
            showColorDialog()
        }

        binding.profileEditBackIv.setOnClickListener {
            showBackDialog()
        }

        binding.profileEditSaveBtn.setOnClickListener {
            viewModel.editProfile()
        }
    }

    private fun initObservers() {
        viewModel.isEditComplete.observe(this) { isComplete ->
            if (isComplete.isSuccess) {
                Toast.makeText(this, getString(R.string.profile_edit_success), Toast.LENGTH_SHORT).show()
                // viewModel.setInitialProfile() 성공하면 초기 프로필 데이터 갱신
                finish() // 화면 종료
            } else Toast.makeText(this, isComplete.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showColorDialog() {
        val dialog = ProfileEditColorDialog()
        dialog.show(supportFragmentManager, "ProfileEditColorDialog")
    }

    private fun showBackDialog() {
        val title = getString(R.string.moim_diary_confirm_back_title)
        val content = getString(R.string.moim_diary_confirm_back_content)

        val dialog = ConfirmDialog(
            this, title, content, "확인", BACK_BUTTON_ACTION
        )
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "")
    }

    override fun onClickYesButton(id: Int) {
        finish()
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                viewModel.setProfileImage(it)
            }
        }

    /** editText 외 터치 시 키보드 내리는 이벤트 **/
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        hideKeyboardOnTouchOutside(ev)
        return super.dispatchTouchEvent(ev)
    }

    companion object {
        const val PROFILE_KEY = "profile"
    }
}
