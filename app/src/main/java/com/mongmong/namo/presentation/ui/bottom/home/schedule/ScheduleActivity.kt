package com.mongmong.namo.presentation.ui.bottom.home.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mongmong.namo.R
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.databinding.ActivityScheduleBinding
import com.mongmong.namo.presentation.ui.bottom.home.notify.PushNotificationReceiver
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class ScheduleActivity : AppCompatActivity(), ConfirmDialogInterface {

    private lateinit var binding : ActivityScheduleBinding
    lateinit var db : NamoDatabase

    private val navController: NavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.schedule_nav_host) as NavHostFragment
        navHostFragment.navController
    }

    private var alarmList : MutableList<Int> = mutableListOf()
    private val failList = ArrayList<Event>()

    private var event : Event? = null

    private val viewModel : ScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleBinding.inflate(layoutInflater)
        db = NamoDatabase.getInstance(this)
        setContentView(binding.root)

        val event = intent.getSerializableExtra("event") as? Event
        val nowDay = intent.getLongExtra("nowDay", 0)

        if (event != null) {
            binding.scheduleDeleteBtn.visibility = View.VISIBLE
            this.event = event
            deleteClick()
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

    private fun deleteClick() {
        binding.scheduleDeleteBtn.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog() {
        // 삭제 확인 다이얼로그
        val title = "일정을 정말 삭제하시겠습니까?"
        val content = "일정 삭제 시, 해당 일정에 대한\n" + "기록 또한 삭제됩니다."

        val dialog = ConfirmDialog(this@ScheduleActivity, title, content, "삭제", 0)
        dialog.isCancelable = false
        dialog.show(this.supportFragmentManager, "ConfirmDialog")
    }

    /** 일정 삭제 **/
    private fun deleteData() {
        event?.let {event ->
            // 알림 리스트 삭제
            alarmList = event.alarmList!!.toMutableList()
            for (i in alarmList) {
                deleteNotification(event.scheduleId.toInt() + DateTime(event.startLong).minusMinutes(i).millis.toInt(), event!!)
            }

            // 일정 삭제
            viewModel.deleteSchedule(event.scheduleId, event.serverId)

            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

            printNotUploaded()
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

    private fun printNotUploaded() {
        val thread = Thread {
            failList.clear()
            failList.addAll(db.eventDao.getNotUploadedEvent() as ArrayList<Event>)
        }
        thread.start()
        try {
            thread.join()
        } catch ( e : InterruptedException) {
            e.printStackTrace()
        }

        Log.d("ScheduleActivity", "Not uploaded Schedule : ${failList}")
    }

    override fun onClickYesButton(id: Int) {
        // 일정 삭재
        deleteData()
    }
}