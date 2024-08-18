package com.mongmong.namo.presentation.ui.onBoarding

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.navigation.fragment.findNavController
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentOnboarding5Binding
import com.mongmong.namo.presentation.config.BaseFragment


class OnBoardingFifthFragment : BaseFragment<FragmentOnboarding5Binding>(R.layout.fragment_onboarding_5) {
    private lateinit var alarmManager : AlarmManager
    private lateinit var notificationManager : NotificationManager
    private var builder : NotificationCompat.Builder? = null

    override fun setup() {
        notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        binding.onboarding5StartBtn.setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingFragment_to_loginFragment)
            onBoardingFinished()
        }
    }


    private fun onBoardingFinished(){
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished",true).apply()
    }


    companion object {
        fun newInstance() = OnBoardingFifthFragment()
    }
}