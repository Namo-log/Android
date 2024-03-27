package com.mongmong.namo.presentation.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.mongmong.namo.R
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.config.BaseResponse
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.home.Category
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.domain.model.CategoryBody
import com.mongmong.namo.data.remote.category.CategoryDeleteService
import com.mongmong.namo.data.remote.category.CategoryDeleteView
import com.mongmong.namo.data.remote.category.CategoryDetailView
import com.mongmong.namo.data.remote.category.CategoryService
import com.mongmong.namo.data.remote.category.CategorySettingService
import com.mongmong.namo.data.remote.category.CategorySettingView
import com.mongmong.namo.domain.model.GetCategoryResponse
import com.mongmong.namo.domain.model.GetCategoryResult
import com.mongmong.namo.domain.model.PostCategoryResponse
import com.mongmong.namo.data.remote.diary.DiaryRepository
import com.mongmong.namo.data.remote.diary.DiaryService
import com.mongmong.namo.data.remote.diary.GetMonthDiaryView
import com.mongmong.namo.domain.model.DeleteScheduleResponse
import com.mongmong.namo.data.remote.schedule.DeleteScheduleView
import com.mongmong.namo.domain.model.EditScheduleResponse
import com.mongmong.namo.data.remote.schedule.ScheduleService
import com.mongmong.namo.data.remote.schedule.ScheduleView
import com.mongmong.namo.data.remote.schedule.GetAllScheduleView
import com.mongmong.namo.domain.model.GetMonthScheduleResponse
import com.mongmong.namo.domain.model.GetMonthScheduleResult
import com.mongmong.namo.domain.model.PostScheduleResponse
import com.mongmong.namo.databinding.ActivityMainBinding
import com.mongmong.namo.domain.model.DiaryGetAllResponse
import com.mongmong.namo.domain.model.DiaryGetAllResult
import com.mongmong.namo.presentation.config.RoomState
import com.mongmong.namo.presentation.config.UploadState
import com.mongmong.namo.presentation.utils.NetworkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime


private const val PERMISSION_REQUEST_CODE= 1001
private const val NOTIFICATION_PERMISSION_REQUEST_CODE= 777

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ScheduleView, DeleteScheduleView, GetAllScheduleView,
    CategorySettingView, GetMonthDiaryView, CategoryDetailView, CategoryDeleteView {

    private lateinit var binding: ActivityMainBinding
    lateinit var db: NamoDatabase
    lateinit var unUploaded: List<Schedule>
    lateinit var unUploadedCategory : List<Category>
    var paletteId : Int = 0

    private val serverSchedule = ArrayList<Schedule>()
    private val serverCategory = ArrayList<Category>()
    private val serverDiary = ArrayList<Diary>()

    private lateinit var categoryColorArray: IntArray

    private var isCategorySuccess = false
    private var isScheduleSuccess = false
    private var isDiarySuccess = false

    companion object {
        const val ORIGIN_ACTIVITY_INTENT_KEY: String = "original_activity"
        const val PLACE_NAME_INTENT_KEY: String = "place_name"
        const val PLACE_X_INTENT_KEY: String = "place_x"
        const val PLACE_Y_INTENT_KEY: String = "place_y"
        const val GROUP_MEMBER_INTENT_KEY : String = "group_member"
        var IS_MOIM_EVENT_SUCCESS : Boolean = false


        const val IS_UPLOAD = true
        const val IS_NOT_UPLOAD = false

        fun setCategoryList(db: NamoDatabase): List<Category> {
            var categoryList =listOf<Category>()
            val thread = Thread{
                categoryList = db.categoryDao.getCategoryList()
            }
            thread.start()
            try {
                thread.join()
            } catch (e: InterruptedException) {
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
        categoryColorArray =resources.getIntArray(R.array.categoryColorArr)
        Log.d("CATEGORY_ARR", categoryColorArray.contentToString())

//        logToken()
        checkPermissions()
        checkNetworkUpload()

        val sf= this.getSharedPreferences("sf", Context.MODE_PRIVATE)
        val editor = sf.edit()
        editor.remove("yearMonth")
        editor.remove("checked")
        editor.apply()

    }

    private fun checkNetworkUpload() {
        if (!NetworkManager.checkNetworkState(this)) {
            IS_MOIM_EVENT_SUCCESS = true
            Log.d("MAIN_SERVER_UPLOAD", "WIFI ERROR : Fail to upload")
            return
        }

        val size = getAllCategorySize()
        Log.d("MAIN_SERVER_UPLOAD", "RoomDB : $size category")
        when (size) {
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

    private fun getAllCategorySize(): Int {
        var size: Int = 0
        val thread = Thread{
            size = db.categoryDao.getAllCategorySize()
        }
        thread.start()
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return size
    }

    private fun getAllScheduleSize(): Int {
        var size: Int = 0
        val thread = Thread{
            size = db.scheduleDao.getAllSchedule()
        }
        thread.start()
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return size
    }

    private fun downloadServerToRoom() {
        val time = DateTime(System.currentTimeMillis()).toString("yyyy,MM")

        //카테고리
        CategorySettingService(this).tryGetAllCategory()

        //이벤트
        val eventService = ScheduleService()
        eventService.setGetAllScheduleView(this)
        eventService.getAllSchedule()

        // 다이어리
        val diaryService = DiaryService()
        diaryService.getAllDiary()
        diaryService.getMonthDiaryView(this)

    }

    private fun uploadRoomToServer() {

        val thread = Thread{
            unUploaded = db.scheduleDao.getNotUploadedSchedule()
            unUploadedCategory = db.categoryDao.getNotUploadedCategory()
        }
        thread.start()
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        Log.d("MAIN_SERVER_UPLOAD", unUploaded.toString())

        // serverId가 없는데 delete인 것들은 안 올림, ID가 0이 아닌데 delete인 건 delete로 올림
        // serverId = 0 인데 state가 delete가 아닌 애들은 POST로 올림, serverId !=0 인 건 PATCH로 올림

        val eventService = ScheduleService()
        eventService.setScheduleView(this)
        eventService.setDeleteScheduleView(this)

        for (i in unUploaded) {
            if (i.serverId == 0L) {
                if (i.state == RoomState.DELETED.state) {
                    return
                } else {
                    //POST
                    eventService.postSchedule(i.convertLocalScheduleToServer(), i.scheduleId)
                }
            } else {
                if (i.state == RoomState.DELETED.state) {
                    eventService.deleteSchedule(i.serverId, i.scheduleId, 0)
                } else {
                    eventService.editSchedule(i.serverId, i.convertLocalScheduleToServer(), i.scheduleId)
                }
            }
        }
        val paletteDatas = arrayListOf(
            categoryColorArray[4], categoryColorArray[5], categoryColorArray[6], categoryColorArray[7], categoryColorArray[8],
            categoryColorArray[9], categoryColorArray[10], categoryColorArray[11], categoryColorArray[12], categoryColorArray[13]
        )

        for (i in unUploadedCategory) {
            for (j: Int in paletteDatas.indices) {
                if (paletteDatas[j] == i.color) {
                    paletteId = j + 5
                }
            }
            if (i.serverId == 0L) {
                if (i.state == RoomState.DELETED.state) {
                    return
                } else {
                    //POST
                    CategoryService(this).tryPostCategory(CategoryBody(i.name, paletteId, i.share), i.categoryId)
                }
            } else {
                if (i.state == RoomState.DELETED.state) {
                    CategoryDeleteService(this).tryDeleteCategory(i.serverId, i.categoryId)
                } else {
                    CategoryService(this).tryPatchCategory(i.serverId, CategoryBody(i.name, paletteId, i.share), i.categoryId)
                }

            }
        }
        val repo= DiaryRepository(this)
        lifecycleScope.launch {
            repo.uploadDiaryToServer()  // 다이어리 서버에 올림
        }

    }

    private fun logToken() {
        val accessToken: String? =
            ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_ACCESS_TOKEN, null)
        Log.d("Token", "$accessToken")
    }

    private fun checkPermissions() {
        val permissionsToRequest =mutableListOf<String>()

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
            intent.data= uri
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
        NavigationUI.setupWithNavController(binding.navBar,findNavController(R.id.nav_host))
        //바텀내비 백스택 수정 필요
    }

    override fun onPostScheduleSuccess(response: PostScheduleResponse, scheduleId: Long) {
        Log.d("MainActivity", "onPostScheduleSuccess")
        Log.d("MAIN_SERVER_UPLOAD", "$scheduleId 번 일정 post 완료")

        val result = response.result

        //룸디비에 isUpload, serverId, state 업데이트하기
        lifecycleScope.launch {
            db.scheduleDao.updateScheduleAfterUpload(
                scheduleId,
                UploadState.IS_UPLOAD.state,
                result.scheduleId,
                RoomState.DEFAULT.state
            )
        }
        val repo=DiaryRepository(this)
        repo.postDiaryToServer(result.scheduleId , scheduleId)
    }

    override fun onPostScheduleFailure(message: String) {
        Log.d("MainActivity", "onPostScheduleFailure")
    }

    override fun onEditScheduleSuccess(response: EditScheduleResponse, scheduleId: Long) {
        Log.d("MainActivity", "onEditScheduleSuccess")
        Log.d("MAIN_SERVER_UPLOAD", "$scheduleId 번 일정 edit 완료")

        val result = response.result

        //룸디비에 isUpload, serverId, state 업데이트하기
        lifecycleScope.launch {
            db.scheduleDao.updateScheduleAfterUpload(
                scheduleId,
                UploadState.IS_UPLOAD.state,
                result.scheduleId,
                RoomState.DEFAULT.state
            )
        }
    }

    override fun onEditScheduleFailure(message: String) {
        Log.d("MainActivity", "onEditScheduleFailure")
    }

    override fun onDeleteScheduleSuccess(response: DeleteScheduleResponse, scheduleId: Long) {
        Log.d("MainActivity", "onDeleteScheduleSuccess")
        Log.d("MAIN_SERVER_UPLOAD", "$scheduleId 번 일정 삭제 완료")

        val result = response.result
        Toast.makeText(this, "$scheduleId 번 일정의 $result", Toast.LENGTH_SHORT).show()


        var deleteDB: Thread = Thread{
            db.scheduleDao.deleteScheduleById(scheduleId)
        }
        deleteDB.start()
        try {
            deleteDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onDeleteScheduleFailure(message: String) {
        Log.d("MainActivity", "onDeleteScheduleFailure")
    }

    override fun onGetAllScheduleSuccess (response: GetMonthScheduleResponse) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetAllScheduleSuccess")

        val result = response.result
        serverSchedule.clear()
        serverSchedule.addAll(result.map{ it.convertServerScheduleResponseToLocal() })
        Log.d("TEST_CHECK", "서버에 있던 이벤트 : " + serverSchedule.toString())
        Log.d("MAIN_SERVER_UPLOAD", "Get All Schedule Finish")
        isScheduleSuccess = true
        checkServerDownloadCompleted()

    }

    override fun onGetAllScheduleFailure (message: String) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetAllScheduleFailure")
        isScheduleSuccess = false
    }
    override fun onGetAllCategorySuccess(response: GetCategoryResponse) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetAllCategorySuccess")

        val result = response.result
        serverCategory.clear()
        serverCategory.addAll(result.map{serverToCategory(it)})
        Log.d("TEST_CHECK", "서버에 있던 카테고리 : " + serverCategory.toString())
        Log.d("MAIN_SERVER_UPLOAD", "Get Category Finish")
        isCategorySuccess = true
        checkServerDownloadCompleted()

    }

    override fun onGetAllCategoryFailure(message: String) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetAllCategoryFailure")
        isCategorySuccess = false
    }

    override fun onGetMonthDiarySuccess(response: DiaryGetAllResponse) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetAllDiarySuccess")

        val result = response.result

        serverDiary.clear()
        serverDiary.addAll(result.map{serverToDiary(it)})
        Log.d("TEST_CHECK", "서버에 있던 다이어리 : $serverDiary")
        Log.d("MAIN_SERVER_UPLOAD", "Get Diary Finish")

        isDiarySuccess = true
        checkServerDownloadCompleted()

    }

    override fun onGetMonthDiaryFailure(message: String) {
        Log.d("MAIN_SERVER_UPLOAD", "onGetAllDiaryFailure")
        isDiarySuccess = false
    }

    override fun onPostCategorySuccess(response: PostCategoryResponse, categoryId : Long) {
        Log.d("MainActivity", "onPostCategorySuccess")
        Log.d("MAIN_SERVER_UPLOAD", "${categoryId} 번 카테고리 post 완료")

        val result = response.result

        //룸디비에 isUpload, serverId, state 업데이트하기
        var thread = Thread{
            db.categoryDao.updateCategoryAfterUpload(
                categoryId,
                1,
                result.categoryId,
                RoomState.DEFAULT.state
            )
        }
        thread.start()
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onPostCategoryFailure(message: String) {
        Log.d("MainActivity", "onPostCategoryFailure")
    }

    override fun onPatchCategorySuccess(response: PostCategoryResponse, categoryId: Long) {
        Log.d("MainActivity", "onPatchCategorySuccess")
        Log.d("MAIN_SERVER_UPLOAD", "${categoryId} 번 카테고리 patch 완료")

        val result = response.result

        //룸디비에 isUpload, serverId, state 업데이트하기
        var thread = Thread{
            db.categoryDao.updateCategoryAfterUpload(
                categoryId,
                1,
                result.categoryId,
                RoomState.DEFAULT.state
            )
        }
        thread.start()
        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onPatchCategoryFailure(message: String) {
        Log.d("MainActivity", "onPatchCategoryFailure")
    }

    override fun onDeleteCategorySuccess(response: BaseResponse, categoryId: Long) {
        Log.d("MainActivity", "onDeleteCategorySuccess")
        Log.d("MAIN_SERVER_UPLOAD", "$categoryId 번 일정 삭제 완료")

        val result = response.message
        Toast.makeText(this, "$categoryId 번 일정의 $result", Toast.LENGTH_SHORT).show()


        var deleteDB: Thread = Thread{
            db.categoryDao.deleteCategoryById(categoryId)
        }
        deleteDB.start()
        try {
            deleteDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onDeleteCategoryFailure(message: String) {
        Log.d("MainActivity", "onDeleteCategoryFailure")
    }


    private fun checkServerDownloadCompleted() {
        if (isCategorySuccess && isScheduleSuccess && isDiarySuccess) {
            lifecycleScope.launch {
                // 모든 데이터베이스 작업을 한 withContext 블록 안에서 순차적으로 처리
                withContext(Dispatchers.IO) {
                    // 카테고리 RoomDB에 저장
                    serverCategory.forEach { category ->
                        db.categoryDao.insertCategory(category)
                    }

                    // 일정 RoomDB에 저장
                    serverSchedule.filter { !it.moimSchedule }.forEach { event ->
                        db.scheduleDao.insertSchedule(event)
                    }

                    // 일정과 비교하고, 기록 RoomDB에 저장
                    val allSchedule = db.diaryDao.getAllSchedule()
                    serverDiary.forEach { diary ->
                        allSchedule.filter { it.hasDiary == 1 && it.serverId == diary.scheduleServerId }.forEach { event ->
                            val diaryData = Diary(
                                diaryId = event.scheduleId,
                                scheduleServerId = diary.scheduleServerId,
                                content = diary.content,
                                images = diary.images,
                                state = RoomState.DEFAULT.state,
                                isUpload = IS_UPLOAD
                            )
                            db.diaryDao.insertDiary(diaryData)
                        }
                    }
                }
                Log.d("TEST_CHECK", "All data has been successfully uploaded to the Room database.")
            }
        }
    }

    private fun serverToCategory(category: GetCategoryResult): Category {
        Log.d(
            "CATEGORY_ARR",
            "Server to Category - color : ${categoryColorArray[category.paletteId - 1]}"
        )
        return Category(
            0,
            category.name,
            categoryColorArray[category.paletteId - 1],
            category.isShare,
            true,
            IS_UPLOAD,
            RoomState.DEFAULT.state,
            category.categoryId
        )
    }

    private fun serverToDiary(diary: DiaryGetAllResult): Diary {
        return Diary(
            0,
            diary.scheduleId,
            diary.contents,
            diary.urls,
            RoomState.DEFAULT.state,
            IS_UPLOAD
        )
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {  // editText 외 터치 시 키보드 내려감
        val focusView = currentFocus
        if (focusView != null && ev != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()

            if (!rect.contains(x, y)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun refreshFragment(fragment: Fragment, fragmentManager: FragmentManager) {
        var ft: FragmentTransaction = fragmentManager.beginTransaction()
        ft.detach(fragment).attach(fragment).commit()
    }


}