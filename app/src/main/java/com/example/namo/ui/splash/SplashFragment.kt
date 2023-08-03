package com.example.namo.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.namo.MainActivity
import com.example.namo.R
import com.example.namo.config.ApplicationClass
import com.example.namo.data.remote.login.LoginResponse
import com.example.namo.data.remote.login.LoginService
import com.example.namo.data.remote.login.RefreshService
import com.example.namo.data.remote.login.SplashView
import com.example.namo.data.remote.login.TokenBody
import com.example.namo.databinding.FragmentSplashBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SplashFragment : Fragment(), SplashView {

    private var _binding : FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 서버 통신
        val splashService = RefreshService()
        splashService.setSplashView(this)

        val accessToken = ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_ACCESS_TOKEN, null)
        val refreshToken = ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_REFRESH_TOKEN, null)
        splashService.splashTokenRefresh(TokenBody(accessToken.toString(), refreshToken.toString()))

//        Handler(Looper.getMainLooper()).postDelayed({
//            //앱 처음인지, 로그인 되어있는지 유무 판단해야 됨
//            if(isSetLoginFinished()){
//                val intent = Intent(requireContext(), MainActivity::class.java)
//                requireActivity().finish()
//                startActivity(intent)
//            } else if (isOnBoardingFinished()) {
//                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
//            } else {
//                findNavController().navigate(R.id.action_splashFragment_to_onBoardingFragment)
//            }
//        }, 1000L)
    }

    private fun isOnBoardingFinished() : Boolean {
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return prefs.getBoolean("finished", false)
    }

    private fun isSetLoginFinished() : Boolean {
        val prefs = requireActivity().getSharedPreferences("setLogin", Context.MODE_PRIVATE)
        return prefs.getBoolean("finished", false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onVerifyTokenSuccess(response: LoginResponse) {
        Log.d("SplashFragment", "onVerifyTokenSuccess")
        // 로그인 성공
        // 토큰 업데이트
        ApplicationClass.sSharedPreferences.edit()
            .putString(ApplicationClass.X_REFRESH_TOKEN, response.result.refreshToken)
            .putString(ApplicationClass.X_ACCESS_TOKEN, response.result.accessToken)
            .apply()
        val intent = Intent(requireContext(), MainActivity::class.java)
        requireActivity().finish()
        startActivity(intent)
    }

    override fun onVerifyTokenFailure(message: String) {
        Log.d("SplashFragment", "onVerifyTokenFailure")
        if (isOnBoardingFinished()) {
            findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_onBoardingFragment)
        }
    }
}