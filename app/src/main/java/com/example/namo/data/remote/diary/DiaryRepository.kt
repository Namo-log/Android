package com.example.namo.data.remote.diary


import DiaryItem
import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.example.namo.R
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.entity.home.Category
import com.example.namo.utils.NetworkManager
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URL


class DiaryRepository(
    val context: Context,
) : DiaryView, DiaryDetailView, AddGroupDiaryView, EditGroupDiaryView, DeleteGroupDiaryView {

    private val diaryService = DiaryService()
    private val db = NamoDatabase.getInstance(context)
    private val diaryDao = db.diaryDao
    private val categoryDao = db.categoryDao

    private lateinit var notUploaded: List<Diary>
    private var callback: DiaryModifyCallback? = null

    private val failList = ArrayList<Diary>()

    private lateinit var category: Category
    private lateinit var diary: Diary


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

        val storeDB = Thread {
            val diary = Diary(
                diaryLocalId,
                serverId,
                content,
                images,
                R.string.event_current_added.toString()
            )
            diaryDao.insertDiary(diary)
            updateHasDiary(diaryLocalId)
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }  // 일단 roomdb에 다이어리 데이터 추가함

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

        val storeDB = Thread {
            diaryDao.updateDiaryAfterUpload(
                localId,
                response.result.scheduleIdx,
                1,
                R.string.event_current_default.toString()
            )
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
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

        val storeDB = Thread {
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
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
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
        response: DiaryResponse.DiaryResponse,
        localId: Long,
        serverId: Long
    ) {

        val storeDB = Thread {
            diaryDao.updateDiaryAfterUpload(
                localId,
                serverId,
                1,
                R.string.event_current_default.toString()
            )
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        callback?.onModify()  // 화면 이동

        Log.d("editDiaryServerSuccess", response.result)
    }


    override fun onEditDiaryFailure(message: String) {

        printNotUploaded()
        callback?.onModify()
        Log.d("editDiaryServerFailure", message)

    }


    /** delete diary **/
    fun deleteDiary(localId: Long, serverId: Long) {

        val storeDB = Thread {
            diaryDao.updateDiaryAfterUpload(
                localId,
                serverId,
                0,
                R.string.event_current_deleted.toString()
            )
            deleteHasDiary(localId) // roomdb hasDiary 0으로 변경
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } // 일단 delete 상태로 업로드

        if (!NetworkManager.checkNetworkState(context)) {
            //인터넷 연결 안 됨
            Log.d("deleteDiary", "WIFI ERROR")
            callback?.onDelete()
            return
        }

        diaryService.deleteDiary(localId, serverId)
        diaryService.setDiaryView(this)

    }

    override fun onDeleteDiarySuccess(response: DiaryResponse.DiaryResponse, localId: Long) {

        val storeDB = Thread {
            diaryDao.deleteDiary(localId) // roomDB에서 삭제
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
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

    fun getDiary(localId: Long): Diary {
        val storeDB = Thread {
            diary = diaryDao.getDiaryDaily(localId)
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return diary
    }


    /** 서버에 있던 것 룸디비에 업데이트 **/
    fun uploadDiaryToServer() {

        val storeDB = Thread {
            notUploaded = diaryDao.getNotUploadedDiary()
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

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

    /** 서버 아이디 없던 것 post로 올리기 **/
    fun postDiaryToServer(eventServerId: Long, eventId: Long) {

        val storeDB = Thread {
            notUploaded = diaryDao.getNotUploadedDiary()
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

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

        diaryService.addDiaryView(this)
    }


    /** 월 별 개인 다이어리 리스트 조회 **/
    fun getDiaryList(yearMonth: String, page: Int, size: Int): List<DiaryItem> {
        return diaryDao.getDiaryEventList(yearMonth).toListItems()
    }


    private fun List<DiaryEvent>.toListItems(): List<DiaryItem> { // 같은 날짜끼리 묶어서 그룹 헤더로 추
        val result = arrayListOf<DiaryItem>() // 결과를 리턴할 리스트

        var groupHeaderDate: Long = 0 // 그룹날짜
        this.forEach { task ->
            // 날짜가 달라지면 그룹 헤더를 추가

            if (groupHeaderDate * 1000 != task.event_start * 1000) {
                result.add(DiaryItem.Header(task.eventId, task.event_start * 1000))
            }
            //  task 추가
            Log.d("ewer", groupHeaderDate.toString())
            Log.d("ewr", task.event_start.toString())

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
                    task.event_category_server_idx,
                    task.eventId

                )
            )

            // 그룹 날짜를 바로 이전 날짜로 설정
            groupHeaderDate = task.event_start
        }

        return result
    }

    /** 카테고리 id로 Category 조회 **/
    fun getCategory(categoryId: Long, categoryServerId: Long): Category {

        val db = Thread {
            val categoryList = categoryDao.getCategoryList()
            category = categoryList.find {
                if (it.serverIdx != 0L) it.serverIdx == categoryServerId
                else it.categoryIdx == categoryId
            } ?: Category()
        }

        db.start()
        try {
            db.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return category
    }

    private fun updateHasDiary(localId: Long) {
        diaryDao.updateHasDiary(localId)
    }

    private fun deleteHasDiary(localId: Long) {
        diaryDao.deleteHasDiary(localId)
    }

    /** 그룹 다이어리 추가 **/
    fun addMoimDiary(
        moimSchduleId: Long,
        place: String,
        money: Int,
        members: List<Int>?,
        images: List<String>?
    ) {

        val placeRequestBody = place.toRequestBody("text/plain".toMediaTypeOrNull())
        val moneyRequestBody = money.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val member = members?.joinToString(",") ?: ""
        val membersRequestBody = member.toRequestBody("text/plain".toMediaTypeOrNull())

        val imgList = imageToMultipart(images)

        diaryService.addGroupDiary(
            moimSchduleId,
            placeRequestBody,
            moneyRequestBody,
            membersRequestBody,
            imgList
        )
        diaryService.addGroupDiaryView(this)
    }

    override fun onAddGroupDiarySuccess(response: DiaryResponse.DiaryResponse) {
        Log.d("ADD_GROUP_DIARY", response.message)
    }

    override fun onAddGroupDiaryFailure(message: String) {
        Log.d("ADD_GROUP_DIARY", message)
    }


    /** 그룹 다이어리 별 장소 수정 **/
    fun editGroupPlace(
        moimPlaceId: Long,
        place: String,
        money: Int,
        members: List<Int>?,
        images: List<String>?
    ) {
        val placeRequestBody = place.toRequestBody("text/plain".toMediaTypeOrNull())
        val moneyRequestBody = money.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val member = members?.joinToString(",") ?: ""
        val membersRequestBody = member.toRequestBody("text/plain".toMediaTypeOrNull())

        val imgList = imageToMultipart(images)

        Log.d("erwer",imgList.toString())
        Log.d("ewe",images.toString())

        diaryService.editGroupDiary(
            moimPlaceId,
            placeRequestBody,
            moneyRequestBody,
            membersRequestBody,
            imgList
        )
        diaryService.editGroupDiaryView(this)
    }


    override fun onEditGroupDiarySuccess(response: DiaryResponse.DiaryResponse) {
        Log.d("EDIT_GROUP_DIARY", response.message)
    }

    override fun onEditGroupDiaryFailure(message: String) {
        Log.d("EDIT_GROUP_DIARY", message)
    }

    /** 그룹 다이어리 별 장소 삭제 **/
    fun deleteGroupPlace(moimPlaceId: Long) {
        diaryService.deleteGroupDiary(moimPlaceId)
        diaryService.deleteGroupDiaryView(this)
    }

    override fun onDeleteGroupDiarySuccess(response: DiaryResponse.DiaryResponse) {
        Log.d("DELETE_GROUP_DIARY", response.message)
    }

    override fun onDeleteGroupDiaryFailure(message: String) {
        Log.d("DELETE_GROUP_DIARY", message)
    }


    private fun imageToMultipart(images: List<String>?): List<MultipartBody.Part>? {
        return images?.map { path ->
            val file = File(absolutelyPath(path.toUri(), context))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }
    }

//    private fun imageToMultipart(images: List<String>?): List<MultipartBody.Part>? {
//        return images?.map { pathOrUrl ->
//            val requestFile: RequestBody = if (pathOrUrl.startsWith("http")) {
//                // 웹 이미지의 경우 URL을 통해 이미지를 다운로드
//                val url = URL(pathOrUrl)
//                val inputStream = url.openStream()
//                val buffer = inputStream.readBytes()
//                inputStream.close()
//                buffer.toRequestBody("image/*".toMediaTypeOrNull())
//            } else {
//                // 갤러리에서 가져온 이미지의 경우 절대 경로를 사용
//                val file = File(absolutelyPath(pathOrUrl.toUri(),context))
//                file.asRequestBody("image/*".toMediaTypeOrNull())
//            }
//
//            MultipartBody.Part.createFormData("imgs", null, requestFile)
//        }
//    }



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

        val storeDB = Thread {
            failList.clear()
            failList.addAll(diaryDao.getNotUploadedDiary() as ArrayList<Diary>)
        }
        storeDB.start()
        try {
            storeDB.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        Log.d("diary", "Not uploaded Diary : $failList")
    }
}