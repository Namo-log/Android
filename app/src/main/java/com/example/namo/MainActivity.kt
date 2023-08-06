package com.example.namo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.event.DeleteEventResponse
import com.example.namo.data.remote.event.DeleteEventView
import com.example.namo.data.remote.event.EditEventResponse
import com.example.namo.data.remote.event.EventService
import com.example.namo.data.remote.event.EventView
import com.example.namo.data.remote.event.PostEventResponse
import com.example.namo.config.ApplicationClass
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.home.Category
import com.example.namo.data.remote.category.CategorySettingService
import com.example.namo.data.remote.category.CategorySettingView
import com.example.namo.data.remote.category.GetCategoryResult
import com.example.namo.data.remote.category.GetCategoryResponse
import com.example.namo.data.remote.diary.DiaryRepository
import com.example.namo.data.remote.diary.DiaryResponse
import com.example.namo.data.remote.diary.DiaryService
import com.example.namo.data.remote.diary.GetMonthDiaryView
import com.example.namo.data.remote.event.GetMonthEventResponse
import com.example.namo.data.remote.event.GetMonthEventResult
import com.example.namo.data.remote.event.GetMonthEventView
import com.example.namo.databinding.ActivityMainBinding
import com.example.namo.ui.bottom.home.schedule.ScheduleDialogBasicFragment.Companion.eventToEventForUpload
import com.example.namo.utils.NetworkManager
import org.joda.time.DateTime
import java.util.Arrays


private const val PERMISSION_REQUEST_CODE = 1001
private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 777
class MainActivity : AppCompatActivity(), EventView, DeleteEventView, GetMonthEventView, CategorySettingView, GetMonthDiaryView {

    private lateinit var binding : ActivityMainBinding
    lateinit var db : NamoDatabase
    lateinit var unUploaded : List<Event>

    private val serverEvent = ArrayList<Event>()
    private val serverCategory = ArrayList<Category>()
    private val serverDiary = ArrayList<Diary>()

    private lateinit var categoryColorArray : IntArray

    private var isCategorySuccess = false
    private var isEventSuccess = false
    private var isDiarySuccess = false


    companion object {
        const val PLACE_NAME_INTENT_KEY : String = "place_name"
        const val PLACE_X_INTENT_KEY : String = "place_x"
        const val PLACE_Y_INTENT_KEY : String = "place_y"

        fun setCategoryList(db : NamoDatabase) : List<Category> {
            var categoryList = listOf<Category>()
            val thread = Thread {
                categoryList = db.categoryDao.getCategoryList()
            }
            thread.start()
            try {
                thread.join()
            } catch (e : InterruptedException) {
                e.printStackTrace()
            }

            Log.d("SetCategory", categoryList.toString())

            return categoryList
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        db = NamoDatabase.getInstance(this)
        initNavigation()
        categoryColorArray = resources.getIntArray(R.array.categoryColorArr)
        Log.d("CATEGORY_ARR", categoryColorArray.contentToString())

        logToken()
        checkPermissions()
        checkNetworkUpload()
    }

    private fun checkNetworkUpload() {
        if (!NetworkManager.checkNetworkState(this)) {
            Log.d("MAIN_SERVER_UPLOAD", "WIFI ERROR : Fail to upload")
            return
        }

        val size = getAllCategorySize()
        Log.d("MAIN_SERVER_UPLOAD", "RoomDB : $size category")
        when(size) {
            0 -> {
                //서버에서 데이터 받아오기
                downloadServerToRoom()
            }
            else -> {
                //Room에서 서버에 안 올라간 것들 처리
                uploadRoomToServer()
            }
        }
    }

    private fun getAllCategorySize() : Int {
        var size : Int = 0
        val thread = Thread {
            size = db.categoryDao.getAllCategorySize()
        }
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }

        return size
    }

    private fun getAllEventSize() : Int {
        var size : Int = 0
        val thread = Thread {
            size = db.eventDao.getAllEvent()
        }
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }

        return size
    }

    private fun downloadServerToRoom() {
        val time = DateTime(System.currentTimeMillis()).toString("yyyy,MM")

        //카테고리
        CategorySettingService(this).tryGetAllCategory()

        //이벤트
        val eventService = EventService()
        eventService.setGetMonthEventView(this)
        eventService.getMonthEvent(time)

        //다이어리
        val diaryService = DiaryService()
        diaryService.getMonthDiaryView(this)
        diaryService.getMonthDiary(time)
    }

    private fun uploadRoomToServer() {
        val repo=DiaryRepository(this)
        val thread = Thread {
            unUploaded = db.eventDao.getNotUploadedEvent()
        }
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }
        Log.d("MAIN_SERVER_UPLOAD", unUploaded.toString())

        // serverId가 없는데 delete인 것들은 안 올림, ID가 0이 아닌데 delete인 건 delete로 올림
        // serverId = 0 인데 state가 delete가 아닌 애들은 POST로 올림, serverId !=0 인 건 PATCH로 올림

        val eventService = EventService()
        eventService.setEventView(this)
        eventService.setDeleteEventView(this)

        for (i in unUploaded) {
            if (i.serverIdx == 0L) {
                if (i.state == R.string.event_current_deleted.toString()) {
                    return
                }
                else {
                    //POST
                    eventService.postEvent(eventToEventForUpload(i), i.eventId)
                }
            }
            else {
                if (i.state == R.string.event_current_deleted.toString()) {
                    eventService.deleteEvent(i.serverIdx, i.eventId)
                }
                else {
                    eventService.editEvent(i.serverIdx, eventToEventForUpload(i), i.eventId)
                }
                repo.getUpload(i.serverIdx)
            }

        }
    }

    private fun logToken() {
        val accessToken: String? = ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_ACCESS_TOKEN, null)
        Log.d("Token", "$accessToken")
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            return
        }

        checkNotificationPermission(this)
    }

    private fun checkNotificationPermission(activity: Activity) {
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            Toast.makeText(this, "일정 알림을 위해 설정에서 알림 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        } else {
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "일정 알림을 위해 설정에서 알림 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return
            } else {
                Toast.makeText(this, "일정 관리를 위해 설정에서 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initNavigation() {
        NavigationUI.setupWithNavController(binding.navBar, findNavController(R.id.nav_host))
        //바텀내비 백스택 수정 필요
    }

    override fun onPostEventSuccess(response: PostEventResponse, eventId : Long) {
        Log.d("MainActivity", "onPostEventSuccess")
        Log.d("MAIN_SERVER_UPLOAD", "$eventId 번 일정 post 완료")

        val result = response.result

        //룸디비에 isUpload, serverId, state 업데이트하기
        var thread = Thread {
            db.eventDao.updateEventAfterUpload(eventId, 1, result.eventIdx, R.string.event_current_default.toString())
        }
        thread.start()
        try {
            thread.join()
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onPostEventFailure(message: String) {
        Log.d("MainActivity", "onPostEventFailure")
    }

    override fun onEditEventSuccess(response: EditEventResponse, eventId : Long) {
        Log.d("MainActivity", "onEditEventSuccess")
        Log.d("MAIN_SERVER_UPLOAD", "$eventId 번 일정 edit 완료")

        val result = response.result

        //룸디비에 isUpload, serverId, state 업데이트하기
        var thread = Thread {
            db.eventDao.updateEventAfterUpload(eventId, 1, result.eventIdx, R.string.event_current_default.toString())
        }
        thread.start()
        try {
            thread.join()
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onEditEventFailure(message: String) {
        Log.d("MainActivity", "onEditEventFailure")
    }

    override fun onDeleteEventSuccess(response: DeleteEventResponse, eventId : Long) {
        Log.d("MainActivity", "onDeleteEventSuccess")
        Log.d("MAIN_SERVER_UPLOAD", "$eventId 번 일정 삭제 완료")

        val result = response.result
        Toast.makeText(this, "$eventId 번 일정의 $result", Toast.LENGTH_SHORT).show()


        var deleteDB : Thread = Thread {
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
        Log.d("MainActivity", "onDeleteEventFailure")
    }

    override fun onGetMonthEventSuccess(response: GetMonthEventResponse) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetMonthEventSuccess")

        val result = response.result
        serverEvent.clear()
        serverEvent.addAll(result.map { serverToEvent(it) })
        Log.d("TEST_CHECK", "서버에 있던 이벤트 : " + serverEvent.toString())
        Log.d("MAIN_SERVER_UPLOAD", "Get Event Finish")
        isEventSuccess = true
        checkServerDownloadCompleted()
    }

    override fun onGetMonthEventFailure(message: String) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetMonthEventFailure")
        isEventSuccess = false
    }

    override fun onGetAllCategorySuccess(response: GetCategoryResponse) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetAllCategorySuccess")

        val result = response.result
        serverCategory.clear()
        serverCategory.addAll(result.map { serverToCategory(it) })
        Log.d("TEST_CHECK", "서버에 있던 카테고리 : " + serverCategory.toString())
        Log.d("MAIN_SERVER_UPLOAD", "Get Category Finish")
        isCategorySuccess = true
        checkServerDownloadCompleted()
    }

    override fun onGetAllCategoryFailure(message: String) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetAllCategoryFailure")
        isCategorySuccess = false
    }

    override fun onGetMonthDiarySuccess(result: List<DiaryResponse.MonthDiaryDto>) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetMonthDiarySuccess")
        //다이어리도 serverDiary에 저장해야됨
        isDiarySuccess = true
        checkServerDownloadCompleted()
    }

    override fun onGetMonthDiaryFailure() {
        Log.d("MAIN_SERVER_UPLOAD", "onGetMonthDiaryFailure")
        isDiarySuccess = true //원래 false여야 함
        checkServerDownloadCompleted()
    }

    private fun checkServerDownloadCompleted() {
        if (isCategorySuccess && isEventSuccess && isDiarySuccess) {
            //그럼 걔네 룸디비 올려야 됨
            val uploadRoom = Thread {
                for (category in serverCategory) {
                    db.categoryDao.insertCategory(category)
                }
                for (event in serverEvent) {
                    db.eventDao.insertEvent(event)
                }

                Log.d("TEST_CHECK", "Now category are ${db.categoryDao.getAllCategorySize()}")
                //다이어리도 해줘야됨
            }
            uploadRoom.start()
            try {
                uploadRoom.join()
            } catch (e : InterruptedException) {
                e.printStackTrace()
            }

//            // Room에 다 업로드 했으면 새로고침하기?
//            val intent = Intent(this, MainActivity::class.java)
//            finish()
//            startActivity(intent)
        }
        else {
            return
        }
    }

    fun serverToEvent(schedule : GetMonthEventResult) : Event {
        return Event(
            0,
            schedule.name,
            schedule.startDate,
            schedule.endDate,
            schedule.interval,
            schedule.categoryId,
            schedule.locationName,
            schedule.x,
            schedule.y,
            0,
            schedule.alarmDate ?: listOf(),
            1,
            (R.string.event_current_default).toString(),
            schedule.scheduleId,
            schedule.categoryId,
            if (schedule.hasDiary) 1 else 0
        )
    }

    fun serverToCategory(category : GetCategoryResult) : Category {
        Log.d("CATEGORY_ARR", "Server to Category - color : ${categoryColorArray[category.paletteId - 1]}")
        return Category(
            0,
            category.name,
            categoryColorArray[category.paletteId - 1],
            category.isShare,
            true,
            1,
            R.string.event_current_default.toString(),
            category.categoryId
        )
    }
}