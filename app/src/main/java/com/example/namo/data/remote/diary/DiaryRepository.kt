package com.example.namo.data.remote.diary


import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.entity.home.Category
import com.example.namo.utils.NetworkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat


class DiaryRepository(
    val context: Context,
) : DiaryView, DiaryDetailView, GetMonthDiaryView, GetDayDiaryView {

    private val diaryService = DiaryService()
    private val db = NamoDatabase.getInstance(context)
    private val diaryDao = db.diaryDao
    private val categoryDao = db.categoryDao

    private val scope = CoroutineScope(IO)

    private lateinit var notUploaded: List<Diary>

    private var callback: DiaryCallback? = null
    private var callback2: DiaryModifyCallback? = null

    fun setCallBack(callback: DiaryCallback) {
        this.callback = callback
    }

    fun setCallBack2(callback: DiaryModifyCallback) {
        this.callback2 = callback
    }

    interface DiaryCallback {
        fun onGetDiaryItems(diaryItem: List<DiaryItem>)
    }

    interface DiaryModifyCallback {
        fun onGetDiary(diary: Diary)
    }


    /** add diary **/
    fun addDiary(
        diaryLocalId: Long, // eventId
        content: String,
        images: List<String>?,
        serverId: Long // eventServerId
    ) {

        scope.launch {
            val diary = Diary(diaryLocalId, content, images)
            diaryDao.insertDiary(diary)
            updateHasDiary(diaryLocalId)
        } // 일단 roomdb에 다이어리 데이터 추가함

        if (!NetworkManager.checkNetworkState(context)) {
            // 인터넷 연결 안 됨
            // 룸디비에 isUpload, serverId, state 업데이트하기
            scope.launch {
                diaryDao.updateDiaryAfterUpload(
                    diaryLocalId,
                    0,
                    R.string.event_current_added.toString()
                )
            }

            Log.d("ADD_DIARY", "WIFI ERROR")

            return
        }

        addDiaryToServer(diaryLocalId, serverId, content, images)
        diaryService.addDiaryView(this)

    }

    private fun addDiaryToServer(
        localId: Long,
        scheduleId: Long,
        content: String,
        images: List<String>?
    ) {

        val imageMultiPart = images?.map { imagePath ->
            imagePath.let { path ->
                val file = File(absolutelyPath(path.toUri(), context))
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imgs", file.name, requestFile)
            }
        }

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryService.addDiary(
            localId,
            imageMultiPart,
            contentRequestBody,
            scheduleIdRequestBody
        )
    }

    override fun onAddDiarySuccess(
        response: DiaryResponse.DiaryAddResponse,
        localId: Long
    ) {
        scope.launch {
            diaryDao.updateDiaryAfterUpload(
                localId,
                1,
                R.string.event_current_default.toString()
            )
        }

        Log.d("addDiaryServer", response.result.toString())
    }

    override fun onAddDiaryFailure(localId: Long, message: String) {

        scope.launch {
            diaryDao.updateDiaryAfterUpload(
                localId,
                0,
                R.string.event_current_added.toString()
            )
        }

        Log.d("addDiaryServer", message)
    }


    /** edit diary **/
    fun editDiary(
        diaryLocalId: Long,
        content: String,
        images: List<String>?,
        serverId: Long
    ) {

        scope.launch {
            val diary = images?.let { Diary(diaryLocalId, content, it) }
            if (diary != null) {
                diaryDao.updateDiary(diary)
            }
        }

        if (!NetworkManager.checkNetworkState(context)) {
            //인터넷 연결 안 됨
            //룸디비에 isUpload, serverId, state 업데이트하기
            scope.launch {
                diaryDao.updateDiaryAfterUpload(
                    diaryLocalId,
                    0,
                    R.string.event_current_edited.toString()
                )
            }

            Log.d("EDIT_DIARY", "WIFI ERROR ")

            return
        }

        editDiaryToServer(diaryLocalId, serverId, content, images)
        diaryService.setDiaryView(this)
    }


    private fun editDiaryToServer(
        localId: Long,
        scheduleId: Long,
        content: String,
        images: List<String>?
    ) {

        val imageMultiPart = images?.map { imagePath ->
            imagePath.let { path ->
                val file = File(absolutelyPath(path.toUri(), context))
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imgs", file.name, requestFile)
            }
        }

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryService.editDiary(
            localId,
            imageMultiPart,
            contentRequestBody,
            scheduleIdRequestBody
        )
    }


    override fun onEditDiarySuccess(response: DiaryResponse.DiaryEditResponse, localId: Long) {

        scope.launch {
            diaryDao.updateDiaryAfterUpload(
                localId,
                1,
                R.string.event_current_default.toString()
            )
        }

        Log.d("editDiaryServer", response.result)
    }


    override fun onEditDiaryFailure(localId: Long, message: String) {

        scope.launch {
            diaryDao.updateDiaryAfterUpload(
                localId,
                0,
                R.string.event_current_edited.toString()
            )
        }

        Log.d("editDiaryServer", message)

    }


    /** delete diary **/
    fun deleteDiary(localId: Long, serverId: Long) {

        scope.launch {
            diaryDao.updateDiaryAfterUpload(
                localId,
                0,
                R.string.event_current_deleted.toString()
            )
        }  // 일단 delete 상태로 업로드

        if (!NetworkManager.checkNetworkState(context)) {
            //인터넷 연결 안 됨
            Toast.makeText(context, "WIFI ERROR", Toast.LENGTH_SHORT).show()
            return
        }

        diaryService.deleteDiary(localId, serverId)
        diaryService.setDiaryView(this)

    }

    override fun onDeleteDiarySuccess(response: DiaryResponse.DiaryDeleteResponse, localId: Long) {

        scope.launch {
            diaryDao.deleteDiary(localId) // roomDB에서 삭제
            diaryDao.updateDiaryAfterUpload(
                localId,
                1,
                R.string.event_current_default.toString()
            )
            deleteHasDiary(localId) // roomdb hasDiary 0으로 변경
        }

        Log.d("deleteDiary", response.result)
    }


    @SuppressLint("ResourceType")
    override fun onDeleteDiaryFailure(localId: Long, message: String) {

        val result = when (message) {
            "500" -> "서버 오류"
            else -> "error"
        }

        Toast.makeText(context, result, Toast.LENGTH_SHORT).show()

        Log.d("deleteDiary", message)
    }


    fun getUpload(eventServerId: Long) {

        scope.launch {
            notUploaded = diaryDao.getNotUploadedDiary()

            for (diary in notUploaded) {

                when (diary.state) {
                    R.string.event_current_added.toString() ->
                        diary.content?.let {
                            addDiaryToServer(
                                diary.diaryLocalId,
                                eventServerId,
                                it,
                                diary.images
                            )
                        }
                    R.string.event_current_edited.toString() ->
                        diary.content?.let {
                            editDiaryToServer(
                                diary.diaryLocalId,
                                eventServerId,
                                it,
                                diary.images
                            )
                        }
                    R.string.event_current_deleted.toString() ->
                        deleteDiary(diary.diaryLocalId, eventServerId)

                }
            }
        }

        diaryService.setDiaryView(this)
        diaryService.addDiaryView(this)

    }


    /** get diary **/
    fun setDiary(localId: Long, serverId: Long) {
        if (!NetworkManager.checkNetworkState(context)) {

            scope.launch {
                val diary = withContext(IO) {
                    diaryDao.getDiaryDaily(localId)
                }

                withContext(Dispatchers.Main) {
                    callback2?.onGetDiary(diary)

                }
            }

        } else {

            diaryService.getDayDiary(localId, serverId)
            diaryService.getDayDiaryView(this)

        }
    }

    override fun onGetDayDiarySuccess(response: DiaryResponse.DiaryGetDayResponse, serverId: Long) {

        val diary = Diary(serverId, response.result.content, response.result.imgUrl)
        callback2?.onGetDiary(diary)

        Log.d("getDayDiary", "success")

    }

    override fun onGetDayDiaryFailure(localId: Long, message: String) {

        scope.launch {
            val diary = withContext(IO) {
                diaryDao.getDiaryDaily(localId)
            }

            withContext(Dispatchers.Main) {
                callback2?.onGetDiary(diary)

            }
        }

        Log.d("getDayDiary", message)
    }

    fun getDiaryList(yearMonth: String) {

        if (!NetworkManager.checkNetworkState(context)) {

            scope.launch {

                val diaryItems = withContext(IO) {
                    getDiaryListLocal(yearMonth)
                }
                withContext(Dispatchers.Main) {
                    callback?.onGetDiaryItems(diaryItems)
                }
            }

        } else {
            getDiaryListFromServer(yearMonth)

        }
    }

    private fun getDiaryListLocal(yearMonth: String): List<DiaryItem> {

        val diaryEvent = diaryDao.getDiaryEventList(yearMonth)
        return diaryEvent.toListItems()
    }


    private fun getDiaryListFromServer(yearMonth: String) {

        val yearMonthSplit = yearMonth.split(".")
        val year = yearMonthSplit[0]
        val month = yearMonthSplit[1].removePrefix("0")
        val formatYearMonth = "$year,$month"

        diaryService.getMonthDiary(formatYearMonth)
        diaryService.getMonthDiaryView(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onGetMonthDiarySuccess(response: DiaryResponse.DiaryGetMonthResponse) {
        val diaryList = mutableListOf<DiaryEvent>()

        for (schedules in response.result.content) {

            val item =
                DiaryEvent(
                    eventId = 0L,
                    event_title = schedules.title,
                    event_start = dateTimeToMillSec(schedules.startDate),
                    event_category_idx = 0,
                    event_place_name = schedules.placeName,
                    content = schedules.content,
                    images = schedules.imgUrl,
                    event_server_idx = schedules.scheduleIdx,
                    event_category_server_idx = schedules.scheduleIdx
                )

            diaryList.add(item)

        }

        val diaryItems=diaryList.toListItems()
        callback?.onGetDiaryItems(diaryItems)

        Log.d("getMonthDiary", response.result.content.toString())
    }

    @SuppressLint("SimpleDateFormat")
    fun dateTimeToMillSec(dateTime: String): Long {
        var timeInMilliseconds: Long = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        try {
            val mDate = sdf.parse(dateTime)
            if (mDate != null) {
                timeInMilliseconds = mDate.time
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timeInMilliseconds
    }

    override fun onGetMonthDiaryFailure(yearMonth: String, message: String) {

        val yearMonthSplit = yearMonth.split(",")
        val year = yearMonthSplit[0]
        val month = yearMonthSplit[1].padStart(2, '0')
        val formatYearMonth = "$year.$month"

        scope.launch {

            val diaryItems = withContext(IO) {
                getDiaryListLocal(formatYearMonth)
            }
            withContext(Dispatchers.Main) {
                callback?.onGetDiaryItems(diaryItems)
            }
        }

        Log.d("getMonthDiary", message)
    }

    /** 같은 날짜끼리 묶어서 그룹 헤더로 추가 **/
    private fun List<DiaryEvent>.toListItems(): List<DiaryItem> {
        val result = arrayListOf<DiaryItem>() // 결과를 리턴할 리스트

        var groupHeaderDate: Long = 0 // 그룹날짜
        this.forEach { task ->
            // 날짜가 달라지면 그룹 헤더를 추가

            if (groupHeaderDate != task.event_start) {
                result.add(DiaryItem.Header(task.event_start))
            }
            //  task 추가

            result.add(
                DiaryItem.Content(

                    task.eventId,
                    task.event_title,
                    task.event_start,
                    task.event_category_idx,
                    task.event_place_name,
                    task.content,
                    task.images,
                    task.event_server_idx,
                    task.event_category_server_idx

                )
            )

            // 그룹 날짜를 바로 이전 날짜로 설정
            groupHeaderDate = task.event_start
        }

        return result
    }

    suspend fun getCategoryId(categoryId: Long): Category = withContext(IO) {
        categoryDao.getCategoryWithId(categoryId)
    }

    private fun updateHasDiary(localId: Long) {
        diaryDao.updateHasDiary(1, localId)
    }

    private fun deleteHasDiary(localId: Long) {
        diaryDao.deleteHasDiary(0, localId)
    }

    @SuppressLint("Recycle")
    fun absolutelyPath(path: Uri?, context: Context): String {
        if (path == null) {
            return ""
        }
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c: Cursor? = context.contentResolver.query(path, proj, null, null, null)
        val index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()

        val result = c?.getString(index ?: 0) ?: ""

        c?.close()

        return result
    }

}