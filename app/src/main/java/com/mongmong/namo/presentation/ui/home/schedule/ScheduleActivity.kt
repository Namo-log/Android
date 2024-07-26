package com.mongmong.namo.presentation.ui.home.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.databinding.ActivityScheduleBinding
import com.mongmong.namo.presentation.ui.home.notify.PushNotificationReceiver
import com.mongmong.namo.presentation.utils.ConfirmDialog
import com.mongmong.namo.presentation.utils.ConfirmDialog.ConfirmDialogInterface
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.DateTime

@AndroidEntryPoint
class ScheduleActivity : AppCompatActivity(), ConfirmDialogInterface {

    private lateinit var binding : ActivityScheduleBinding

    private val navController: NavController by lazy {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.schedule_nav_host) as NavHostFragment
        navHostFragment.navController
    }

    private var alarmList : MutableList<Int> = mutableListOf()

    private var schedule : Schedule? = null

    private val viewModel : PersonalScheduleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val schedule = intent.getSerializableExtra("schedule") as? Schedule
        val nowDay = intent.getLongExtra("nowDay", 0)

        if (schedule != null) {
            binding.scheduleDeleteBtn.visibility = View.VISIBLE
            this.schedule = schedule
            deleteClick()
        } else {
            binding.scheduleDeleteBtn.visibility = View.GONE
            binding.scheduleDeleteBtn.setOnClickListener {
                return@setOnClickListener
            }
        }
        val action = ScheduleDialogBasicFragmentDirections.actionScheduleDialogBasicFragmentSelf(schedule = schedule, nowDay = nowDay)
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
        schedule?.let { schedule ->
            // 알림 리스트 삭제
            alarmList = schedule.alarmList!!.toMutableList()
            for (i in alarmList) {
                deleteNotification(schedule.scheduleId.toInt() + DateTime(schedule.startLong).minusMinutes(i).millis.toInt(), schedule!!)
            }

            // 일정 삭제
            viewModel.deleteSchedule(schedule.scheduleId, schedule.moimSchedule)

            Toast.makeText(this, "일정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun deleteNotification(id : Int, schedule : Schedule) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, PushNotificationReceiver::class.java)
        intent.putExtra("notification_id", id)
        intent.putExtra("notification_title", schedule.title)
        intent.putExtra("notification_content", DateTime(schedule.startLong).toString("MM-dd") + " ~ " + DateTime(schedule.endLong).toString("MM-dd"))

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        alarmManager.cancel(pendingIntent)
    }

    override fun onClickYesButton(id: Int) {
        // 일정 삭재
        deleteData()
    }
}