package com.gradu.presentation.ui.auth

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gradu.presentation.MainActivity
import com.gradu.presentation.auth.AuthViewModel
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.mongmong.namo.presentation.config.BaseFragment
import com.mongmong.namo.presentation.enums.LoginPlatform
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
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
        binding.loginKakaoBt.setOnClickListener {
            startKakaoLogin()
        }
        binding.loginNaverBt.setOnClickListener {
            startNaverLogin()
        }
    }

    private fun initObserve() {
        viewModel.loginResult.observe(viewLifecycleOwner) {
            when {
                // 로그인 실패
                it == null -> return@observe
                // 로그인 성공: 신규 유저 or 약관에 동의하지 않은 유저
                it.newUser || viewModel.checkUpdatedTerms() -> {
                    findNavController().navigate(
                        R.id.action_loginFragment_to_termsFragment,
                        Bundle().apply { putString("userName", it.userName) }
                    )
                    return@observe
                }
                // 로그인 성공: 소셜 가입만 된 유저 or 회원가입 완료(signUpComplete)된 유저
                it.accessToken.isNotEmpty() -> {
                    val intent = Intent(requireContext(),
                        if(!it.signUpComplete) RegisterActivity::class.java else MainActivity::class.java)
                    intent.putExtra("userName", it.userName) // 사용자 이름 전달
                    startActivity(intent)
                    requireActivity().finish()
                }
                else -> Toast.makeText(requireContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startKakaoLogin() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(ContentValues.TAG, "카카오 로그인 실패", error)
            } else if (token != null) {
                UserApiClient.instance.me { user, _ ->
                    val userName = user?.kakaoAccount?.profile?.nickname ?: "사용자"
                    viewModel.tryLogin(LoginPlatform.KAKAO, token.accessToken, token.refreshToken, userName)
                }
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
            UserApiClient.instance.loginWithKakaoTalk(requireContext(), callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
        }
    }

    private fun startNaverLogin() {
        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                NidOAuthLogin().callProfileApi(object : NidProfileCallback<NidProfileResponse> {
                    override fun onSuccess(response: NidProfileResponse) {
                        val userName = response.profile?.name ?: "사용자"
                        Log.d("naverSignUp", userName)
                        viewModel.tryLogin(
                            LoginPlatform.NAVER,
                            NaverIdLoginSDK.getAccessToken().toString(),
                            NaverIdLoginSDK.getRefreshToken().toString(),
                            userName
                        )
                    }

                    override fun onFailure(httpStatus: Int, message: String) {
                        Log.e("NaverLogin", "Profile API 호출 실패: $message")
                    }

                    override fun onError(errorCode: Int, message: String) {
                        Log.e("NaverLogin", "Profile API 에러: $message")
                    }
                })
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.d("NaverLogin", "로그인 실패: errorCode=$errorCode, errorDesc=$errorDescription")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }
        NaverIdLoginSDK.authenticate(requireContext(), oauthLoginCallback)
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
