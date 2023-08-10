package com.example.namo.data.remote.diary


import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.entity.home.Category
import com.example.namo.utils.NetworkManager
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


class DiaryRepository(
    val context: Context,
) : DiaryView, DiaryDetailView {

    private val diaryService = DiaryService()
    private val db = NamoDatabase.getInstance(context)
    private val diaryDao = db.diaryDao
    private val categoryDao = db.categoryDao

    private val scope = CoroutineScope(IO)

    private lateinit var notUploaded: List<Diary>
    private var callback: DiaryModifyCallback? = null

    private val failList = ArrayList<Diary>()

    fun setCallBack(callback: DiaryModifyCallback) {
        this.callback = callback
    }

    interface DiaryModifyCallback {
        fun onModify()
        fun onDelete()
    }

    /** add diary **/
    fun addDiary(
        diaryLocalId: Long, // eventId
        content: String,
        images: List<String>?,
        serverId: Long // eventServerId
    ) {

        scope.launch {
            val diary = Diary(
                diaryLocalId,
                serverId,
                content,
                images,
                R.string.event_current_added.toString()
            )
            diaryDao.insertDiary(diary)
            updateHasDiary(diaryLocalId)
        } // 일단 roomdb에 다이어리 데이터 추가함

        if (NetworkManager.checkNetworkState(context)) {

            // 와이파이 연결 시, 서버에 데이터 추가
            addDiaryToServer(diaryLocalId, serverId, content, images)
            diaryService.addDiaryView(this)

        }
    }

    private fun addDiaryToServer(
        localId: Long,
        scheduleId: Long,
        content: String,
        images: List<String>?
    ) {

        val imgList = imageToMultipart(images)

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryService.addDiary(
            localId,
            imgList,
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
                response.result.scheduleIdx,
                1,
                R.string.event_current_default.toString()
            )
        }

        Log.d("addDiaryServerSuccess", response.result.toString())
    }

    override fun onAddDiaryFailure(message: String) {

        printNotUploaded()
        Log.d("addDiaryServerFailure", message)
    }


    /** edit diary **/
    fun editDiary(
        diaryLocalId: Long,
        content: String,
        images: List<String>?,
        serverId: Long
    ) {

        scope.launch {
            val diary = images?.let {
                Diary(
                    diaryLocalId,
                    serverId,
                    content,
                    it,
                    R.string.event_current_edited.toString()
                )
            }
            if (diary != null) {
                diaryDao.updateDiary(diary)
            }
        }

        if (NetworkManager.checkNetworkState(context)) {

            editDiaryToServer(diaryLocalId, serverId, content, images)
            diaryService.setDiaryView(this)
        }

    }


    private fun editDiaryToServer(
        localId: Long,
        scheduleId: Long,
        content: String,
        images: List<String>?
    ) {

        val imgList = imageToMultipart(images)

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryService.editDiary(
            localId,
            scheduleId,
            imgList,
            contentRequestBody,
            scheduleIdRequestBody
        )

    }


    override fun onEditDiarySuccess(
        response: DiaryResponse.DiaryEditResponse,
        localId: Long,
        serverId: Long
    ) {

        scope.launch {
            diaryDao.updateDiaryAfterUpload(
                localId,
                serverId,
                1,
                R.string.event_current_default.toString()
            )
        }

        runBlocking {
            callback?.onModify()
        }

        Log.d("editDiaryServerSuccess", response.result)
    }


    override fun onEditDiaryFailure(message: String) {

        printNotUploaded()
        callback?.onModify()
        Log.d("editDiaryServerFailure", message)

    }

    private fun imageToMultipart(images: List<String>?): List<MultipartBody.Part>? {
        return images?.map { path ->
            val file = File(absolutelyPath(path.toUri(), context))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }
    }


    /** delete diary **/
    fun deleteDiary(localId: Long, serverId: Long) {

        scope.launch {
            diaryDao.updateDiaryAfterUpload(
                localId,
                serverId,
                0,
                R.string.event_current_deleted.toString()
            )
            deleteHasDiary(localId) // roomdb hasDiary 0으로 변경
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
        }

        callback?.onDelete()
        Log.d("deleteDiary", response.result)
    }


    @SuppressLint("ResourceType")
    override fun onDeleteDiaryFailure(message: String) {

        printNotUploaded()
        callback?.onDelete()
        Log.d("deleteDiary", message)
    }

    suspend fun getDiary(localId: Long): Diary = withContext(IO) {
        return@withContext diaryDao.getDiaryDaily(localId)
    }

    suspend fun getDiaryList(yearMonth: String, page: Int, size: Int): List<DiaryItem> =
        withContext(IO) {
            val diaryEvent = diaryDao.getDiaryEventList(yearMonth, page, size)
            return@withContext diaryEvent.toListItems()
        }


    suspend fun uploadDiaryToServer() {

        scope.launch {
            notUploaded = diaryDao.getNotUploadedDiary()
        }.join()
            for (diary in notUploaded) {

                if (diary.serverId == 0L) { // 서버 아이디 없는 것들
                    if (diary.state == R.string.event_current_deleted.toString()) {
                        return
                    }
                } else {
                    if (diary.state == R.string.event_current_deleted.toString()) {
                        deleteDiary(diary.diaryId, diary.serverId)
                    } else {
                        diary.content?.let {
                            editDiaryToServer(
                                diary.diaryId,
                                diary.serverId,
                                it,
                                diary.images
                            )
                        }
                    }
                }
            }

        diaryService.setDiaryView(this)
    }

    fun postDiaryToServer(eventServerId: Long, eventId: Long) {

        scope.launch {
            notUploaded = diaryDao.getNotUploadedDiary()

            for (diary in notUploaded) {

                if (diary.serverId == 0L) { // 서버 아이디 없는 것들
                    if (diary.state !== R.string.event_current_deleted.toString() && eventId == diary.diaryId) {
                        diary.content?.let {
                            addDiaryToServer(
                                diary.diaryId,
                                eventServerId,
                                it,
                                diary.images
                            )
                        }
                    }
                }
            }
        }

        diaryService.addDiaryView(this)
    }


    /** 같은 날짜끼리 묶어서 그룹 헤더로 추가 **/
    private fun List<DiaryEvent>.toListItems(): List<DiaryItem> {
        val result = arrayListOf<DiaryItem>() // 결과를 리턴할 리스트

        var groupHeaderDate: Long = 0 // 그룹날짜
        this.forEach { task ->
            // 날짜가 달라지면 그룹 헤더를 추가

            if (groupHeaderDate * 1000 != task.event_start * 1000) {
                result.add(DiaryItem.Header(task.event_start * 1000))
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

    private fun printNotUploaded() {
        scope.launch {
            failList.clear()
            failList.addAll(diaryDao.getNotUploadedDiary() as ArrayList<Diary>)
        }

        Log.d("diary", "Not uploaded Diary : $failList")
    }
}