package com.mongmong.namo.data.remote.diary


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.mongmong.namo.R
import com.mongmong.namo.data.NamoDatabase
import com.mongmong.namo.data.entity.diary.Diary
import com.mongmong.namo.data.entity.home.Category
import com.mongmong.namo.utils.NetworkManager
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*

class DiaryRepository(
    val context: Context
) : DiaryDetailView {

    private val diaryService = DiaryService()
    private val db = NamoDatabase.getInstance(context)
    private val diaryDao = db.diaryDao
    private val categoryDao = db.categoryDao

    private lateinit var notUploaded: List<Diary>

    private val failList = ArrayList<Diary>()
    private lateinit var imgFile: File

    private lateinit var category: Category
    private lateinit var diary: Diary

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
            scheduleIdRequestBody,
            object : AddPersonalDiaryView {
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
            }
        )
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
        diaryService.setDiaryView(this)
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

        Log.d("editDiaryServerSuccess", response.result)
    }


    override fun onEditDiaryFailure(message: String) {

        printNotUploaded()
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

        Log.d("deleteDiary", response.result)
    }


    @SuppressLint("ResourceType")
    override fun onDeleteDiaryFailure(message: String) {

        printNotUploaded()
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
        money: Long,
        members: List<Long>?,
        images: List<String>?,
        callback: DiaryBasicView
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
            imgList,
            callback
        )
    }


    /** 그룹 다이어리 별 장소 수정 **/
    fun editGroupPlace(
        moimPlaceId: Long,
        place: String,
        money: Long,
        members: List<Long>?,
        images: List<String>?,
        callback: DiaryBasicView
    ) {
        val placeRequestBody = place.toRequestBody("text/plain".toMediaTypeOrNull())
        val moneyRequestBody = money.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val member = members?.joinToString(",") ?: ""
        val membersRequestBody = member.toRequestBody("text/plain".toMediaTypeOrNull())

        val imgList = imageToMultipart(images)

        diaryService.editGroupDiary(
            moimPlaceId,
            placeRequestBody,
            moneyRequestBody,
            membersRequestBody,
            imgList,
            callback
        )
    }

    private fun imageToMultipart(images: List<String>?): List<MultipartBody.Part>? {
        return images?.map { path ->

            val uri = Uri.parse(path)
            val file = imageToFile(uri, context)

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }
    }

    // 이미지를 압축하고 크기를 조정하여 파일로 저장
    private fun imageToFile(uri: Uri, context: Context): File {

        val db = Thread {
            val bitmap = loadWebImageToBitmap(context, uri.toString())
            val bitmapHash = bitmap.hashCode()
            val fileName = "image_$bitmapHash.jpg"
            imgFile = File(context.cacheDir, fileName)

            if (!imgFile.exists()) {
                try {
                    val outputStream = FileOutputStream(imgFile)
                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Log.d("cache", "Image already exists: ${imgFile.path}")
            }
        }

        db.start()
        try {
            db.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return imgFile

    }


    private fun loadWebImageToBitmap(context: Context, imageUrl: String): Bitmap? {
        return try {
            val requestOptions = RequestOptions()
                .override(SIZE_ORIGINAL) // 원본 크기로 로딩
                .fitCenter() // 이미지를 중앙에 맞춤
                .disallowHardwareConfig() // 하드웨어 가속을 사용하지 않음

            val bitmap: Bitmap? = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(requestOptions)
                .submit()
                .get()

            Log.d("bitmap", bitmap.toString())
            bitmap // 비트맵 반환
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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