package com.gradu.presentation.ui.auth

import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.mongmong.namo.R
import com.mongmong.namo.databinding.ActivityRegisterBinding
import com.mongmong.namo.presentation.config.BaseActivity
import com.gradu.presentation.MainActivity
import com.mongmong.namo.presentation.utils.hideKeyboardOnTouchOutside
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : BaseActivity<ActivityRegisterBinding>(R.layout.activity_register) {
    private val viewModel: RegisterViewModel by viewModels()

    override fun setup() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setUserName()
        initClickListener()
        initObservers()
    }

    private fun setUserName() {
        viewModel.setUserName(intent.getStringExtra("userName") ?: "사용자")
    }

    private fun initClickListener() {
        binding.registerProfileImgIv.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.registerColorSelectLl.setOnClickListener {
            showColorDialog()
        }

        binding.registerBirthContentTv.setOnClickListener {
            showRegisterDateDialog()
        }

        binding.registerSaveBtn.setOnClickListener {
            if (viewModel.isRegisterEnabled.value == true) {
                viewModel.requestRegister()
            } else {
                viewModel.enableHighlight()
                Toast.makeText(this, "색상과 필수 항목을 기재해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initObservers() {
        viewModel.isRegisterComplete.observe(this) { isComplete ->
            if(isComplete.isSuccess) {
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            } else Toast.makeText(this, isComplete.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showRegisterDateDialog() {
        val dialog = RegisterDateDialog()
        dialog.show(supportFragmentManager, "RegisterDateDialog")
    }


    private fun showColorDialog() {
        val dialog = RegisterColorDialog()
        dialog.show(supportFragmentManager, "RegisterColorDialog")
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
}
