package com.example.namo.ui.splash

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.namo.R
import com.example.namo.databinding.FragmentOnboarding5Binding
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
//            startAlarm()
//            setAlarm()

            findNavController().navigate(R.id.action_onBoardingFragment_to_termsFragment)
            onBoardingFinished()
        }

    }

    private fun onBoardingFinished(){
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished",true).apply()
    }

//    private fun setAlarm() {
//        var receiverIntent : Intent = Intent(requireContext(), AlarmReceiver::class.java)
//        var pendingIntent : PendingIntent = PendingIntent.getBroadcast(requireContext(), 0, receiverIntent, PendingIntent.FLAG_IMMUTABLE)
//
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
//    }
//
//    private fun startAlarm() {
//        var alarmManager : AlarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        var intent : Intent = Intent(requireContext(), AlertReceiver::class.java)
//        var pendingIntent : PendingIntent = PendingIntent.getBroadcast(requireContext(), 1, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        var date = DateTime(System.currentTimeMillis()).plusMinutes(1).millis
//
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date, pendingIntent)
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FifthFragment()
    }
}