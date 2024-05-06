package com.mongmong.namo.presentation.ui.custom

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.domain.model.LogoutBody
import com.mongmong.namo.data.remote.auth.LogoutService
import com.mongmong.namo.data.remote.auth.LogoutView
import com.mongmong.namo.databinding.FragmentCustomSettingBinding

import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kakao.sdk.user.UserApiClient
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.presentation.ui.splash.OnBoardingActivity
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback

class CustomSettingFramgent: Fragment(), ConfirmDialogInterface, LogoutView {

    private var _binding: FragmentCustomSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCustomSettingBinding.inflate(inflater, container, false)

        hideBottomNavigation(true)
        setVersion()

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

    private fun setVersion() {
        binding.customSettingVerInfoTv.text = ApplicationClass.VERSION
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
            // 토큰 삭제
            val token = ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_ACCESS_TOKEN, null)
            token?.let { LogoutBody(it) }?.let { LogoutService(this).tryPostLogout(it) }
            // 룸에 있는 모든 데이터 삭제
            deleteAllRoomDatas()
        }
        else if (id == 1) { // 회원탈퇴
//            LogoutService(this).tryQuit()
            // 네이버 연동 해제
            NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
                override fun onSuccess() {
                    //서버에서 토큰 삭제에 성공한 상태입니다.
                }
                override fun onFailure(httpStatus: Int, message: String) {
                    // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                    // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                    Log.d(TAG, "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                    Log.d(TAG, "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
                }
                override fun onError(errorCode: Int, message: String) {
                    // 서버에서 토큰 삭제에 실패했어도 클라이언트에 있는 토큰은 삭제되어 로그아웃된 상태입니다.
                    // 클라이언트에 토큰 정보가 없기 때문에 추가로 처리할 수 있는 작업은 없습니다.
                    onFailure(errorCode, message)
                }
            })
            // 카카오 연동 해제
            UserApiClient.instance.unlink { error ->
                if (error != null) {
                    Log.e(TAG, "연결 끊기 실패", error)
                }
                else {
                    Log.i(TAG, "연결 끊기 성공. SDK에서 토큰 삭제 됨")
                }
            }
            // 일단 로그인 화면으로 이동
            moveToLoginFragment()
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

    override fun onPostLogoutSuccess(response: BaseResponse) {
        Log.d("CustomSettingFrag", "onPostLogoutSuccess")
        moveToLoginFragment()
    }

    override fun onPostLogoutFailure(message: String) {
        Log.d("CustomSettingFrag", "onPostLogoutFailure")
    }

}

private fun NidOAuthLogin.callDeleteTokenApi(context: OAuthLoginCallback) {

}

//private fun NidOAuthLogin.callDeleteTokenApi(context: OAuthLoginCallback) {
//
//}
