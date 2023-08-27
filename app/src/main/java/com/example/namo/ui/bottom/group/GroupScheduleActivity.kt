package com.example.namo.ui.bottom.group

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.databinding.ActivityGroupScheduleBinding
import net.daum.mf.map.api.MapView
import org.joda.time.DateTime

class GroupScheduleActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGroupScheduleBinding
    private lateinit var db : NamoDatabase

    lateinit var mapView: MapView
    var mapViewContainer: RelativeLayout? = null
    private var place_name : String = "없음"
    private var place_x : Double = 0.0
    private var place_y : Double = 0.0

    private var date = DateTime(System.currentTimeMillis())
    private var event : Event = Event()
    private var scheduleIdx : Long = 0L

    private var prevClicked : TextView? = null
    private var startDateTime = DateTime(System.currentTimeMillis())
    private var endDateTime = DateTime(System.currentTimeMillis())
    private var selectedDate = DateTime(System.currentTimeMillis())



    private var prevChecked : MutableList<Int> = mutableListOf()
    private var prevAlarmList : List<Int>? = null
    private var alarmList : MutableList<Int> = mutableListOf()
    private var alarmText : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupScheduleBinding.inflate(layoutInflater)
        db = NamoDatabase.getInstance(this)
        setContentView(binding.root)

        val nowDay = intent.getLongExtra("nowDay", 0L)
        val event = intent.getSerializableExtra("event") as? Event
        if (event != null) this.event = event

        binding.dialogGroupSchedulePlaceKakaoBtn.visibility = View.GONE
        binding.dialogGroupSchedulePlaceKakaoBtn.visibility = View.GONE
        mapViewContainer?.visibility = View.GONE

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
        binding.scheduleContainerLayout.startAnimation(slideAnimation)

        if (event != null) {
            prevAlarmList = event.alarmList
            setContent()
        } else {
            binding.dialogGroupScheduleHeaderTv.text = "새 일정"
            if (nowDay != 0L) {
                date = DateTime(nowDay)
            }
//            initPickerText()
        }

        if (this.event.eventId != 0L) {
            binding.dialogGroupScheduleHeaderTv.text = "일정 편집"
            scheduleIdx = this.event.eventId
        } else {
            binding.dialogGroupScheduleHeaderTv.text = "새 일정"
        }

//        clickListener()
    }

    private fun setContent() {
        binding.dialogGroupScheduleTitleEt.setText(event.title)
        //참여자 넣어야됨
        //시작일, 종료일
        startDateTime = DateTime(event.startLong * 1000L)
        endDateTime = DateTime(event.endLong * 1000L)
        binding.dialogGroupScheduleStartDateTv.text = startDateTime.toString(getString(R.string.dateFormat))
        binding.dialogGroupScheduleEndDateTv.text = endDateTime.toString(getString(R.string.dateFormat))
        //시작 시간, 종료 시간
        binding.dialogGroupScheduleStartTimeTv.text = startDateTime.toString(getString(R.string.timeFormat))
        binding.dialogGroupScheduleEndTimeTv.text = endDateTime.toString(getString(R.string.timeFormat))
        //알람
//        setAlarmClicked(event.alarmList)
        val checkedIds = binding.alarmGroup.checkedChipIds
        prevChecked = checkedIds
        for (i in checkedIds) {
//            alarmText += getChipText(i)
        }
        alarmText = alarmText.substring(0, alarmText.length - 2)
        binding.dialogGroupScheduleAlarmTv.text = alarmText
        //장소
        place_name = event.placeName
        place_x = event.placeX
        place_y = event.placeY
        if (event.placeX != 0.0 || event.placeY != 0.0) {
//            initMapView()
//            setMapContent()
        }
    }

}