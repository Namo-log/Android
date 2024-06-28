package com.mongmong.namo.presentation.ui.custom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.databinding.FragmentCustomSettingBinding

import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.presentation.ui.login.AuthViewModel
import com.mongmong.namo.presentation.ui.onBoarding.OnBoardingActivity
import com.mongmong.namo.presentation.config.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomSettingFragment: Fragment(), ConfirmDialogInterface {
    private lateinit var binding: FragmentCustomSettingBinding

    private val viewModel : AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCustomSettingBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)
        setVersion()
        initObserve()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        onClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()

        hideBottomNavigation(false)
    }

    private fun setVersion() {
        binding.customSettingVerInfoTv.text = ApplicationClass.VERSION
    }

    private fun initObserve() {
        viewModel.isLogoutComplete.observe(viewLifecycleOwner) {
//            deleteAllRoomDatas()
            Toast.makeText(requireContext(), "로그아웃에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
            moveToLoginFragment() // 화면 이동
        }
        viewModel.isQuitComplete.observe(viewLifecycleOwner) {
//            deleteAllRoomDatas()
            Toast.makeText(requireContext(), "회원탈퇴에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
            moveToLoginFragment() // 화면 이동
        }
    }


    private fun onClickListener() {
        binding.apply {
            // 뒤로가기
            customSettingBackIv.setOnClickListener {
                findNavController().popBackStack() //뒤로가기
            }

            // 이용 약관
            customSettingTermTv.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.TERM_URL))
                startActivity(intent)
            }

            // 개인정보 처리방침
            customSettingIndividualPolicyTv.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.POLICY_URL))
                startActivity(intent)
            }

            // 로그아웃
            customSettingLogoutTv.setOnClickListener {
                logout()
            }

            // 회원탈퇴
            customSettingQuitTv.setOnClickListener {
                quit()
            }
        }
    }

    private fun logout() {
        // 다이얼로그
        val title = "로그아웃 하시겠어요?"

        val dialog = ConfirmDialog(this@CustomSettingFragment, title, null, "확인", 0)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        activity?.let { dialog.show(it.supportFragmentManager, "ConfirmDialog") }
    }

    private fun quit() {
        // 다이얼로그
        val title = "정말 계정을 삭제하시겠어요?"
        val content = "지금까지의 정보는\n" +
                "3일 뒤 모두 사라집니다."

        val dialog = ConfirmDialog(this@CustomSettingFragment, title, content, "확인", 1)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        activity?.let { dialog.show(it.supportFragmentManager, "ConfirmDialog") }
    }

    private fun deleteAllRoomDatas() {
        val db = NamoDatabase.getInstance(requireContext())

        // 모든 데이터 삭제
        val thread = Thread {
            db.scheduleDao.deleteAllSchedules()
            db.diaryDao.deleteAllDiaries()
            db.categoryDao.deleteAllCategories()
        }
        thread.start()
        try {
            thread.join()
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun moveToLoginFragment() {
        // 토큰 비우기
        ApplicationClass.sSharedPreferences.edit().clear().apply()
        // 화면 이동
        activity?.finishAffinity()
        startActivity(Intent(context, OnBoardingActivity()::class.java))
    }


    override fun onClickYesButton(id: Int) { // 다이얼로그 확인 메시지 클릭
        if (id == 0) { // 로그아웃
            viewModel.tryLogout()
        }
        else if (id == 1) { // 회원탈퇴
            viewModel.tryQuit()
        }
    }

    private fun hideBottomNavigation(bool: Boolean) {
        val bottomNavigationView: BottomNavigationView =
            requireActivity().findViewById(R.id.nav_bar)
        if (bool) {
            bottomNavigationView.visibility = View.GONE
        } else {
            bottomNavigationView.visibility = View.VISIBLE
        }
    }
}
