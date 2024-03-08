package com.mongmong.namo.presentation.ui.bottom.home.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mongmong.namo.presentation.ui.MainActivity
import com.mongmong.namo.R

class PushNotificationReceiver : BroadcastReceiver() {

    private val channelId = "schedule_notify_id"

    override fun onReceive(context: Context?, intent: Intent?) {
        createNotificationChannel(context)
        Log.d("ALARM", "do alarm")

        val notificationId = intent!!.getIntExtra("notification_id",0)
        val notificationTitle = intent.getStringExtra("notification_title")
        val notificationContent = intent.getStringExtra("notification_content")

        val builder = NotificationCompat.Builder(context!!, channelId)
            .setSmallIcon(R.drawable.ic_app_logo_square)
            .setContentTitle(notificationTitle)
            .setContentText(notificationContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val clickIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId,builder.build())
    }

    private fun createNotificationChannel(context: Context?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Schedule Notify"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance)

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            channel.setSound(soundUri, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())

            val pattern = longArrayOf(0,500,200,500)
            channel.vibrationPattern = pattern
            channel.enableVibration(true)

            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}