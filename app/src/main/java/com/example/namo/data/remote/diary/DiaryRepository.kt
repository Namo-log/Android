package com.example.namo.data.remote.diary


import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.entity.home.Category
import com.example.namo.ui.bottom.diary.mainDiary.DiaryFragment
import com.example.namo.ui.bottom.diary.mainDiary.DiaryModifyFragment
import com.example.namo.utils.NetworkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import kotlin.collections.ArrayList


class DiaryRepository(
    val context: Context,
) : DiaryView, DiaryDetailView, GetMonthDiaryView, GetDayDiaryView {

    private val diaryService = DiaryService()
    private val db = NamoDatabase.getInstance(context)
    private val diaryDao = db.diaryDao
    private val categoryDao = db.categoryDao

    private var fragment: DiaryModifyFragment? = null
    private var fragment2: DiaryFragment? = null

    private val failList: ArrayList<Diary> = arrayListOf()
    private lateinit var notUploaded: List<Diary>
    fun setFragment(fragment: DiaryModifyFragment) {
        this.fragment = fragment
    }

    fun setFragment2(fragment: DiaryFragment) {
        this.fragment2 = fragment
    }


    /** add diary **/
    fun addDiary(
        diaryLocalId: Int, // eventId
        content: String,
        images: List<String?>?,
        serverId: Int // eventServerId
    ) {

        Thread {
            val diary = Diary(diaryLocalId, content, images)
            diaryDao.insertDiary(diary)
            updateHasDiary(diaryLocalId)
        }.start()  // 일단 roomdb에 다이어리 데이터 추가함


        if (!NetworkManager.checkNetworkState(context)) {
            //인터넷 연결 안 됨
            //룸디비에 isUpload, serverId, state 업데이트하기
            val thread = Thread {

                diaryDao.updateDiaryAfterUpload(
                    diaryLocalId,
                    0,
                    serverId,
                    R.string.event_current_added.toString()
                )
                failList.clear()
                failList.addAll(diaryDao.getNotUploadedDiary() as ArrayList<Diary>)

            }
            thread.start()
            try {
                thread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            Log.d("ADD_DIARY", "WIFI ERROR : $failList")

            return
        }

        if (images != null) {
            addDiaryToServer(diaryLocalId, serverId, content, images)
        }
        diaryService.addDiaryView(this)

    }

    private fun addDiaryToServer(
        localId: Int,
        scheduleId: Int,
        content: String,
        images: List<String?>?
    ) {

        val imageMultiPart = images?.map { imagePath ->
            imagePath?.let { path ->
                val file = File(path)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imgs", file.name, requestFile)
            }
        }

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryService.addDiary(
            localId,
            scheduleId,
            imageMultiPart,
            contentRequestBody,
            scheduleIdRequestBody
        )
    }

    override fun onAddDiarySuccess(
        result: DiaryResponse.GetScheduleIdx,
        localId: Int
    ) {
        Thread {
            diaryDao.updateDiaryAfterUpload(
                localId,
                1,
                result.scheduleIdx,
                R.string.event_current_default.toString()
            )
        }.start()


        Log.d("addDiaryServer", "success")
    }

    override fun onAddDiaryFailure(localId: Int, serverId: Int) {

        Thread {
            diaryDao.updateDiaryAfterUpload(
                localId,
                0,
                serverId,
                R.string.event_current_added.toString()
            )
            failList.clear()
            failList.addAll(diaryDao.getNotUploadedDiary() as ArrayList<Diary>)

        }.start()

        Log.d("addDiaryServer", "failure")
    }

    /** edit diary **/
    fun editDiary(
        diaryLocalId: Int,
        content: String,
        images: List<String?>?,
        serverId: Int
    ) {

        Thread {
            val diary = images?.let { Diary(diaryLocalId, content, it) }
            if (diary != null) {
                diaryDao.updateDiary(diary)
            }
        }.start()  // 일단 roomdb에 다이어리 데이터 추가함


        if (!NetworkManager.checkNetworkState(context)) {
            //인터넷 연결 안 됨
            //룸디비에 isUpload, serverId, state 업데이트하기
            val thread = Thread {

                diaryDao.updateDiaryAfterUpload(
                    diaryLocalId,
                    0,
                    serverId,
                    R.string.event_current_edited.toString()
                )
                failList.clear()
                failList.addAll(diaryDao.getNotUploadedDiary() as ArrayList<Diary>)

            }
            thread.start()
            try {
                thread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            Log.d("EDIT_DIARY", "WIFI ERROR : $failList")

            return
        }

        editDiaryToServer(diaryLocalId, serverId, content, images)
        diaryService.setDiaryView(this)
    }


    private fun editDiaryToServer(
        localId: Int,
        scheduleId: Int,
        content: String,
        images: List<String?>?
    ) {

        val imageMultiPart = images?.map { imagePath ->
            imagePath?.let { path ->
                val file = File(path)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("imgs", file.name, requestFile)
            }
        }

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryService.editDiary(
            localId,
            scheduleId,
            imageMultiPart,
            contentRequestBody,
            scheduleIdRequestBody
        )
    }


    override fun onEditDiarySuccess(result: String, localId: Int, serverId: Int) {

        Thread {
            diaryDao.updateDiaryAfterUpload(
                localId,
                1,
                serverId,
                R.string.event_current_default.toString()
            )
        }.start()

        Log.d("editDiaryServer", "success")
    }


    override fun onEditDiaryFailure(localId: Int, serverId: Int) {

        Thread {
            diaryDao.updateDiaryAfterUpload(
                localId,
                0,
                serverId,
                R.string.event_current_edited.toString()
            )
            failList.clear()
            failList.addAll(diaryDao.getNotUploadedDiary() as ArrayList<Diary>)

        }.start()

        Log.d("editDiaryServer", "failure")

    }


    fun deleteDiary(localId: Int, serverId: Int) {

        Thread {
            diaryDao.updateDiaryAfterUpload(
                localId,
                0,
                serverId,
                R.string.event_current_deleted.toString()
            )
        }.start()  // 일단 delete 상태로 업로드


        if (!NetworkManager.checkNetworkState(context)) {
            //인터넷 연결 안 됨
            Log.d("deleteDiary", "WIFI ERROR")
            return
        }

        diaryService.deleteDiary(localId, serverId)
        diaryService.setDiaryView(this)

    }

    override fun onDeleteDiarySuccess(localId: Int, serverId: Int) {

        Thread {
            diaryDao.deleteDiary(localId)
            deleteHasDiary(localId)
            diaryDao.updateDiaryAfterUpload(
                localId,
                1,
                serverId,
                R.string.event_current_default.toString()
            )
        }.start()


        Log.d("deleteDiary", "seccess")
    }


    override fun onDeleteDiaryFailure(localId: Int, serverId: Int) {

        val thread = Thread {
            failList.clear()
            failList.addAll(diaryDao.getNotUploadedDiary() as ArrayList<Diary>)

        }
        thread.start()
        Log.d("deleteDiary", "failure")
    }


    fun getUpload(eventServerId: Int) {

        Thread {
            notUploaded = diaryDao.getNotUploadedDiary()
        }.start()

        diaryService.setDiaryView(this)
        diaryService.addDiaryView(this)

        for (diary in notUploaded) {

            if (diary.diaryServerId == 0) {
                if (diary.state == R.string.event_current_deleted.toString()) return
                else {
                    addDiaryToServer(diary.diaryLocalId, eventServerId, diary.content, diary.images)
                }
            } else {
                if (diary.state == R.string.event_current_deleted.toString()) {
                    deleteDiary(diary.diaryLocalId, diary.diaryServerId)
                } else {
                    editDiaryToServer(
                        diary.diaryLocalId,
                        diary.diaryServerId,
                        diary.content,
                        diary.images
                    )
                }
            }
        }
    }


    suspend fun getCategoryId(categoryId: Int): Category = withContext(Dispatchers.IO) {
        categoryDao.getCategoryContent(categoryId)
    }

    /** get diary **/
    fun setDiary(localId: Int, serverId: Int) {
        if (!NetworkManager.checkNetworkState(context)) {

            Thread {
                val diary = diaryDao.getDiaryDaily(localId)
                Handler(Looper.getMainLooper()).post {
                    fragment?.bindDiary(diary)
                }

            }.start()

        } else {

            diaryService.getDayDiary(localId, serverId) // 다이어리의 서버 아이디
            diaryService.getDayDiaryView(this)

        }
    }

    override fun onGetDayDiarySuccess(localId: Int, result: DiaryResponse.DayDiaryDto) {

        val diary = Diary(localId, result.content, result.imgUrl)
        fragment?.bindDiary(diary)

        Log.d("getDayDiart", "success")

    }

    override fun onGetDayDiaryFailure(localId: Int) {
        Thread {
            val diary = diaryDao.getDiaryDaily(localId)
            Handler(Looper.getMainLooper()).post {
                fragment?.bindDiary(diary)
            }
        }.start()

        Log.d("getDayDiart", "failure")
    }

    suspend fun getDiaryList(yearMonth: String) {

        val diaryItems = getDiaryListLocal(yearMonth)
        fragment2?.getList(diaryItems)

        if (diaryItems.isEmpty()) {
            if (NetworkManager.checkNetworkState(context)) {
                getDiaryListFromServer(yearMonth)
            } else {
                return
            }
        }
    }

    private suspend fun getDiaryListLocal(yearMonth: String): List<DiaryItem> =
        withContext(Dispatchers.IO) {
            val diaryEvent = diaryDao.getDiaryEventList(yearMonth)
            return@withContext diaryEvent.toListItems()
        }


    private fun getDiaryListFromServer(yearMonth: String) {

        val yearMonthSplit = yearMonth.split(".")
        val year = yearMonthSplit[0]
        val month = yearMonthSplit[1].removePrefix("0")
        val formatYearMonth = "$year,$month"

        diaryService.getMonthDiary(formatYearMonth)
        diaryService.getMonthDiaryView(this)
    }

    override fun onGetMonthDiarySuccess(
        result: List<DiaryResponse.MonthDiaryDto>
    ) {
        val diaryList = arrayListOf<DiaryItem>()
        for (it in result) {
            val item = DiaryItem.Content(
                it.eventId.toLong(),
                it.title,
                it.startDate,
                it.categoryId,
                it.placeName,
                1,
                it.content,
                it.imgUrl,
                1,
                R.string.event_current_default.toString(),
                it.scheduleIdx
            )
            diaryList.add(item)
        }

    }

    override fun onGetMonthDiaryFailure() {

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
                    task.has_diary,
                    task.content,
                    task.images,
                    task.event_upload,
                    task.event_state,
                    task.event_server_idx

                )
            )

            // 그룹 날짜를 바로 이전 날짜로 설정
            groupHeaderDate = task.event_start
        }

        return result
    }

    private fun updateHasDiary(localId: Int) {
        diaryDao.updateHasDiary(1, localId)
    }

    private fun deleteHasDiary(localId: Int) {
        diaryDao.deleteHasDiary(0, localId)
    }

}


