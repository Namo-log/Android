package com.example.namo.ui.bottom.custom

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.namo.R
import com.example.namo.config.ApplicationClass
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.databinding.FragmentCustomBinding
import com.example.namo.databinding.FragmentCustomSettingBinding

import com.example.namo.ui.bottom.diary.mainDiary.adapter.GalleryListAdapter
import com.example.namo.ui.splash.SplashActivity
import com.example.namo.utils.ConfirmDialog
import com.example.namo.utils.ConfirmDialogInterface
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class CustomSettingFramgent: Fragment(), ConfirmDialogInterface {

    private var _binding: FragmentCustomSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCustomSettingBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        onClickListener()
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        hideBottomNavigation(false)
    }


    private fun onClickListener() {
        binding.apply {
            // 뒤로가기
            customSettingBackIv.setOnClickListener {
                findNavController().popBackStack() //뒤로가기
            }

            // 이용약관
            customSettingTermTv.setOnClickListener {
                val url = "https://www.notion.so/30d9c6cf5b9f414cb624780360d2da8c?pvs=4"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            // 개인정보 처리방침
            customSettingIndividualPolicyTv.setOnClickListener {
                val url = "https://www.notion.so/ca8d93c7a4ef4ad98fd6169c444a5f32?pvs=4"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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

        val dialog = ConfirmDialog(this@CustomSettingFramgent, title, null, "확인", 0)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        activity?.let { dialog.show(it.supportFragmentManager, "ConfirmDialog") }
    }

    private fun quit() {
        // 다이얼로그
        val title = "정말 계정을 삭제하시겠어요?"
        val content = "지금까지의 정보가 모두 사라집니다."

        val dialog = ConfirmDialog(this@CustomSettingFramgent, title, content, "확인", 1)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        activity?.let { dialog.show(it.supportFragmentManager, "ConfirmDialog") }
    }


    override fun onClickYesButton(id: Int) { // 다이얼로그 확인 메시지 클릭
        if (id == 0) { // 로그아웃
//            LogoutService(this).tryLogout()
        }
        else if (id == 1) { // 회원탈퇴
//            LogoutService(this).tryQuit()
        }

        // 토큰 비우기
        ApplicationClass.sSharedPreferences.edit().clear().apply()
        // 화면 이동
        activity?.finishAffinity()
        startActivity(Intent(context, SplashActivity()::class.java))
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