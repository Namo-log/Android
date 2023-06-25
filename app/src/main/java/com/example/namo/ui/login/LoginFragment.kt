package com.example.namo.ui.login

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.namo.MainActivity
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
            val intent = Intent(requireContext(), MainActivity::class.java)
            requireActivity().finish()
            startActivity(intent)
            setLoginFinished()
        }
    }

    private fun setLoginFinished(){
        val prefs = requireActivity().getSharedPreferences("setLogin", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished",true).apply()
    }
}