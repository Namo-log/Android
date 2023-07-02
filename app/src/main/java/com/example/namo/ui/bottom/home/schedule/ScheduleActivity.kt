package com.example.namo.ui.bottom.home.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.ActivityScheduleBinding
import com.example.namo.ui.bottom.home.notify.PushNotificationReceiver
import org.joda.time.DateTime
import java.util.Date

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding : ActivityScheduleBinding
    lateinit var db : NamoDatabase

    private val navController: NavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.schedule_nav_host) as NavHostFragment
        navHostFragment.navController
    }

    private var alarmList : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleBinding.inflate(layoutInflater)
        db = NamoDatabase.getInstance(this)
        setContentView(binding.root)

        val event = intent.getSerializableExtra("event") as? Event
        val nowDay = intent.getLongExtra("nowDay", 0)

        if (event != null) {
            binding.scheduleDeleteBtn.visibility = View.VISIBLE

            deleteClick(event)
        } else {
            binding.scheduleDeleteBtn.visibility = View.GONE
            binding.scheduleDeleteBtn.setOnClickListener {
                return@setOnClickListener
            }
        }
        val action = ScheduleDialogBasicFragmentDirections.actionScheduleDialogBasicFragmentSelf(event = event, nowDay = nowDay)
        navController.navigate(action)

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
        binding.scheduleContainerLayout.startAnimation(slideAnimation)

        clickListener()
    }

    private fun clickListener() {
        binding.scheduleBackgroundLayout.setOnClickListener {
            finish()
        }

        binding.scheduleContainerLayout.setOnClickListener {
            return@setOnClickListener
        }
    }

    private fun deleteClick(event : Event) {
        binding.scheduleDeleteBtn.setOnClickListener {
            //일정 삭제하고 닫기
            alarmList = event.alarmList!!.toMutableList()
            for (i in alarmList) {
                deleteNotification(event.eventId.toInt() + DateTime(event.startLong).minusMinutes(i).millis.toInt(), event)
            }

            var deleteDB : Thread = Thread {
                db.eventDao.deleteEvent(event)
            }
            deleteDB.start()
            try {
                deleteDB.join()
            } catch ( e: InterruptedException) {
                e.printStackTrace()
            }

            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

            finish()
        }
    }

    private fun deleteNotification(id : Int, event : Event) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, PushNotificationReceiver::class.java)
        intent.putExtra("notification_id", id)
        intent.putExtra("notification_title", event.title)
        intent.putExtra("notification_content", DateTime(event.startLong).toString("MM-dd") + " ~ " + DateTime(event.endLong).toString("MM-dd"))

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        alarmManager.cancel(pendingIntent)
    }
}