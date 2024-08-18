package com.mongmong.namo.presentation.ui.login

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.config.LoginPlatform
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment: BaseFragment<FragmentLoginBinding>(R.layout.fragment_login) {
    private val viewModel : AuthViewModel by viewModels()

    override fun setup() {
        initObserve()
        initClickListeners()
        initNotification()
        onBackPressed()
    }

    private fun initNotification() {
        requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun initClickListeners(){
        // 카카오 로그인
        binding.loginKakaoBt.setOnClickListener {
            startKakaoLogin()
        }
        // 네이버 로그인
        binding.loginNaverBt.setOnClickListener {
            startNaverLogin()
        }
    }

    private fun initObserve() {
        viewModel.loginResult.observe(viewLifecycleOwner) {
            if (it == null) return@observe

            if (it.newUser || viewModel.checkUpdatedTerms()) {
                // 약관 동의 화면으로 이동
                findNavController().navigate(R.id.action_loginFragment_to_termsFragment)
                return@observe
            }
            if (it.accessToken.isNotEmpty()) {
                Toast.makeText(requireContext(), "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show()
                setLoginFinished()
            } else {
                Toast.makeText(requireContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tryLogin(platform: LoginPlatform, accessToken: String, refreshToken: String) {
        viewModel.tryLogin(platform, accessToken, refreshToken)
    }

    private fun startKakaoLogin() {
        // 카카오계정 로그인 공통 callback 구성
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {  //토큰 에러
                Log.e(ContentValues.TAG, "카카오계정으로 로그인 실패", error)
                // 카카오톡 설치는 되어있지만, 로그인이 안 되어있는 경우 예외 처리
                if (error.toString().contains("statusCode=302")){
                    loginWithKakaoAccount()
                }
            } else if (token != null) {
                Log.i(ContentValues.TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")

                tryLogin(LoginPlatform.KAKAO, token.accessToken, token.refreshToken)
            }
        }
        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            UserApiClient.instance.loginWithKakaoTalk(requireContext(), callback = callback)
        } else {
            loginWithKakaoAccount()
        }
    }

    private fun loginWithKakaoAccount() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (token != null) {
                tryLogin(LoginPlatform.KAKAO, token.accessToken, token.refreshToken)
            }
        }
        UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
    }

    private fun startNaverLogin() {
        // OAuthLoginCallback을 authenticate() 메서드 호출 시 파라미터로 전달하거나 NidOAuthLoginButton 객체에 등록하면 인증이 종료됨
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                tryLogin(LoginPlatform.NAVER, NaverIdLoginSDK.getAccessToken().toString(), NaverIdLoginSDK.getRefreshToken().toString())
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d("naverLogin", "errorCode: $errorCode, errorDesc: $errorDescription")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }
        NaverIdLoginSDK.authenticate(requireContext(), oauthLoginCallback)
    }

    private fun setLoginFinished(){
        requireActivity().finish()
        startActivity(Intent(requireContext(), MainActivity::class.java))
    }

    private fun onBackPressed() {
        // 뒤로 가기 버튼 처리
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 여기서 뒤로 가기 동작을 정의합니다.
                requireActivity().finish()
            }
        })
    }
}