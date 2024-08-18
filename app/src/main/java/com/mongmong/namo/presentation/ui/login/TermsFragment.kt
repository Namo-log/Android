package com.mongmong.namo.presentation.ui.login

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentTermsBinding
import com.mongmong.namo.domain.model.TermBody
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.config.Constants
import com.mongmong.namo.presentation.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsFragment: BaseFragment<FragmentTermsBinding>(R.layout.fragment_terms) {
    private val viewModel : TermsViewModel by viewModels()

    override fun setup() {
        initObserve()
        initClickListeners()
        checkboxListener()
    }

    private fun initObserve() {
        viewModel.isComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) {
                moveToMainActivity()
            }
        }
    }

    private fun initClickListeners(){
        binding.termsNextBtn.setOnClickListener {
            viewModel.tryCheckTerms(TermBody(true, true))
        }
        setTermsSee()
    }

    private fun checkboxListener() {
        // 처음에는 다음 버튼을 못누르게 막아둠
        binding.termsNextBtn.isClickable = false

        binding.termsAgreeAllBtn.setOnClickListener {
            // all을 밑에 안넣은 이유는... 클릭 여부에 따라서 전체 동의를 변경해야하기 때문입니다
            if(binding.termsAgreeAllBtn.isChecked) {  // 모두 true로 변경
                allTrue()
            } else {  // 모두 false로 변경
                allFalse()
            }
        }

        val checkboxListener = CompoundButton.OnCheckedChangeListener { checkbox, _ ->
            when(checkbox.id) {
                R.id.terms_agree_service_btn -> {
                    checkStatus()  // 선택 조건
                    essential()  // 필수 조건
                }
                R.id.terms_agree_personal_btn -> {
                    checkStatus()
                    essential()
                }
                R.id.terms_agree_gps_btn -> {
                    checkStatus()
                }
                R.id.terms_agree_alert_btn -> {
                    checkStatus()
                }
            }
        }

        // 클릭 리스너 연결
        binding.termsAgreeServiceBtn.setOnCheckedChangeListener(checkboxListener)
        binding.termsAgreePersonalBtn.setOnCheckedChangeListener(checkboxListener)
        binding.termsAgreeGpsBtn.setOnCheckedChangeListener(checkboxListener)
        binding.termsAgreeAlertBtn.setOnCheckedChangeListener(checkboxListener)
    }

    private fun allTrue() {
        // 전체 동의
        binding.termsAgreeServiceBtn.isChecked = true
        binding.termsAgreePersonalBtn.isChecked = true
        binding.termsAgreeGpsBtn.isChecked = true
        binding.termsAgreeAlertBtn.isChecked = true

        changeButton(true)
    }

    private fun allFalse() {
        // 전체 동의 해제
        binding.termsAgreeServiceBtn.isChecked = false
        binding.termsAgreePersonalBtn.isChecked = false
        binding.termsAgreeGpsBtn.isChecked = false
        binding.termsAgreeAlertBtn.isChecked = false

        changeButton(false)
    }

    private fun essential() {
        // 필수 동의 여부 체크, 얘네만 눌러도 넘어갈 수 있게
        if(binding.termsAgreeServiceBtn.isChecked && binding.termsAgreePersonalBtn.isChecked) {
            changeButton(true)
        } else {
            changeButton(false)
        }
    }

    private fun checkStatus() {
        // 전체 동의 여부 체크
        if(binding.termsAgreeServiceBtn.isChecked && binding.termsAgreePersonalBtn.isChecked &&
                binding.termsAgreeGpsBtn.isChecked && binding.termsAgreeAlertBtn.isChecked) {
            binding.termsAgreeAllBtn.isChecked = true
            changeButton(true)
        } else {
            binding.termsAgreeAllBtn.isChecked = false
        }
    }

    private fun changeButton(status: Boolean) {
        // 버튼 상태 변경
        if(status) {  // 다음으로 넘어갈 수 있는 상태
            binding.termsNextBtn.isClickable = true

            binding.termsNextBtn.setTextColor(Color.parseColor("#FFFFFFFF"))
            binding.termsNextBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DA6022"))
        } else {  // 다음으로 넘어갈 수 없는 상태
            binding.termsNextBtn.isClickable = false

            binding.termsNextBtn.setTextColor(Color.parseColor("#D68359"))
            binding.termsNextBtn.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F7F7F7"))
        }
    }

    private fun setTermsSee() {
        with(binding) {
            // 이용약관
            termsAgreeServiceSeeIv.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TERM_URL)))
            }

            // 개인정보 처리방침
            termsAgreePersonalSeeIv.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.POLICY_URL)))
            }
        }
    }

    private fun moveToMainActivity(){
        requireActivity().finish()
        startActivity(Intent(requireContext(), MainActivity::class.java))
    }
}