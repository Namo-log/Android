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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mongmong.namo.R
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.data.local.entity.home.Event
import com.mongmong.namo.domain.model.DeleteEventResponse
import com.mongmong.namo.data.remote.event.DeleteEventView
import com.mongmong.namo.data.remote.event.EventService
import com.mongmong.namo.databinding.ActivityScheduleBinding
import com.mongmong.namo.presentation.ui.bottom.home.notify.PushNotificationReceiver
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialogInterface
import com.mongmong.namo.presentation.utils.NetworkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.joda.time.DateTime

@AndroidEntryPoint
class ScheduleActivity : AppCompatActivity(), DeleteEventView, ConfirmDialogInterface {

    private lateinit var binding : ActivityScheduleBinding
    lateinit var db : NamoDatabase

    private val navController: NavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.schedule_nav_host) as NavHostFragment
        navHostFragment.navController
    }

    private var alarmList : MutableList<Int> = mutableListOf()
    private val failList = ArrayList<Event>()

    private var event : Event? = null

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

    private fun uploadToServer(event: Event) {
        //룸디비에 isUpload, serverId, state 업데이트하기
        lifecycleScope.launch {
            db.eventDao.updateEventAfterUpload(event.eventId, 0, event.serverIdx, R.string.event_current_deleted.toString())
        }

        if (!NetworkManager.checkNetworkState(this)) {
            //인터넷 연결 안 됨
            printNotUploaded()
            return
        }

        val isMoim = if (event.moimSchedule) 1 else 0

        val eventService = EventService()
        eventService.setDeleteEventView(this)
        eventService.deleteEvent(event.serverIdx, event.eventId, isMoim)
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

    override fun onDeleteEventSuccess(response: DeleteEventResponse, eventId : Long) {
        Log.d("ScheduleActivity", "onDeleteEventSuccess")

//        val result = response.result
//        Toast.makeText(this, "$eventId 번 일정의 ${response.result}", Toast.LENGTH_SHORT).show()

        var deleteDB = Thread {
            db.eventDao.deleteEventById(eventId)
        }
        deleteDB.start()
        try {
            deleteDB.join()
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onDeleteEventFailure(message: String) {
        Log.d("ScheduleActivity", "onDeleteEventFailure")
        printNotUploaded()
    }

    override fun onClickYesButton(id: Int) {
        //일정 삭제하고 닫기
        alarmList = event!!.alarmList!!.toMutableList()
        for (i in alarmList) {
            deleteNotification(event!!.eventId.toInt() + DateTime(event!!.startLong).minusMinutes(i).millis.toInt(), event!!)
        }

        uploadToServer(event!!)

        Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

        finish()
    }
}