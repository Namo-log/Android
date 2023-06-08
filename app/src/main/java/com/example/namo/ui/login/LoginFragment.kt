package com.example.namo.ui.login

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.config.ApplicationClass
import com.example.namo.data.remote.login.KakaoSDKResponse
import com.example.namo.data.remote.login.LoginService
import com.example.namo.data.remote.login.LoginView
import com.example.namo.data.remote.login.TokenBody
import com.example.namo.databinding.FragmentLoginBinding
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import org.joda.time.DateTime
import java.util.*

class LoginFragment: Fragment(), LoginView {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var alarmManager : AlarmManager
    private lateinit var notificationManager : NotificationManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        kakaoLogin()
        clickHandler()

        return binding.root
    }

    private fun clickHandler(){
        // 임시 로그인
        binding.loginTempBt.setOnClickListener {
//            setAlarm(System.currentTimeMillis())
//            setContent("NAMO","나모 이용방법을 알려드려요.")
//            setAlarm(System.currentTimeMillis() + 10000)
//            setContent("나모","두 번째 알람입니다.")
//            setAlarm(System.currentTimeMillis() + 20000)
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            //setLoginFinished()

        }
    }

    private fun kakaoLogin() {
        // 카카오계정 로그인 공통 callback 구성
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {  //토큰 에러
                Log.e(ContentValues.TAG, "카카오계정으로 로그인 실패", error)
//                printKakaoErrorToast(error)
            } else if (token != null) {
                Log.i(ContentValues.TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
//                showCustomToast("로그인에 성공하였습니다.")
//                getUserInfo()

                //토큰 저장
                val spf = requireActivity().getSharedPreferences("kakaoToken", AppCompatActivity.MODE_PRIVATE)

                spf.edit().putString("access", token.accessToken).apply()
                val accessToken = spf.getString("access", "")

                spf.edit().putString("refresh", token.refreshToken).apply()
                val refreshToken = spf.getString("refresh", "")

                // 서버 통신
                LoginService(this).tryPostKakaoSDK(TokenBody("$accessToken", "$refreshToken"))

                Log.d("kakao_access_token", "$accessToken")
                Log.d("kakao_refresh_token", "$refreshToken")
            }

        }
        // 카카오 로그인 버튼 클릭시 로그인
        binding.loginKakaoBt.setOnClickListener {
            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(requireContext())) {
                UserApiClient.instance.loginWithKakaoTalk(requireContext(), callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(requireContext(), callback = callback)
            }
        }
    }

//    private fun setAlarm(millis : Long) {
//        val receiverIntent : Intent = Intent(requireContext(), AlarmReceiver::class.java)
////        val receiverIntent : Intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.naver.com/"))
//        val requestCode : Int = createID()
//        Log.d("CHECK_NOTIFY", "RequestCode : $requestCode")
//        val pendingIntent : PendingIntent = PendingIntent.getBroadcast(requireContext(), requestCode, receiverIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        alarmManager.set(AlarmManager.RTC, millis, pendingIntent)
//        Log.d("CHECK_NOTIFY", "Set Ararm do on ${DateTime(millis)}")
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                //API 19 이상 23 미만
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
//            } else {
//                //API 19 미만
//                alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
//            }
//        } else {
//            //API 23 이상
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
//        }
//    }

    private fun setLoginFinished(){
        val prefs = requireActivity().getSharedPreferences("setLogin", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished",true).apply()
    }

    override fun onPostKakaoSDKSuccess(response: KakaoSDKResponse) {
        Log.d("LoginActivity", "onPostKakaoSDKSuccess")
        Log.d("Login", "$response")

        val result = response.result
        // 토큰 저장
        val editor = ApplicationClass.sSharedPreferences.edit()
        editor.putString(ApplicationClass.X_ACCESS_TOKEN, result.accessToken).apply()
        editor.putString(ApplicationClass.X_REFRESH_TOKEN, result.refreshToken).apply()

        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        //setLoginFinished()
    }

    override fun onPostKakaoSDKFailure(message: String) {
        Log.d("LoginActivity", "onPostKakaoSDKFailure")
    }
}