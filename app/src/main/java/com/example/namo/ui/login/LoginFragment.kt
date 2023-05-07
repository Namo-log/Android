package com.example.namo.ui.login

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.namo.R
import com.example.namo.databinding.FragmentLoginBinding
import org.joda.time.DateTime
import java.util.*

class LoginFragment: Fragment() {
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

        clickHandler()

        return binding.root
    }

    private fun clickHandler(){
        binding.loginTempBt.setOnClickListener {
//            setAlarm(System.currentTimeMillis())
//            setContent("NAMO","나모 이용방법을 알려드려요.")
//            setAlarm(System.currentTimeMillis() + 10000)
//            setContent("나모","두 번째 알람입니다.")
//            setAlarm(System.currentTimeMillis() + 20000)
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            setLoginFinished()

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
}