package com.mongmong.namo.presentation.ui.splash

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mongmong.namo.R
import com.mongmong.namo.databinding.FragmentOnboarding5Binding
import org.joda.time.DateTime


class FifthFragment : Fragment() {

    private var _binding : FragmentOnboarding5Binding? = null
    private val binding get() = _binding!!

    private lateinit var alarmManager : AlarmManager
    private lateinit var notificationManager : NotificationManager
    private var builder : NotificationCompat.Builder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding5Binding.inflate(inflater, container, false)

        notificationManager = requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.onboarding5StartBtn.setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingFragment_to_termsFragment)
            onBoardingFinished()
        }

    }

    private fun onBoardingFinished(){
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished",true).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FifthFragment()
    }
}