package com.example.namo.data.remote.diary


import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import com.example.namo.data.dao.CategoryDao
import com.example.namo.data.dao.DiaryDao
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.*

class DiaryRepository(
    private val diaryDao: DiaryDao,
    private val categoryDao: CategoryDao,
    private val diaryService: DiaryService,
    val context: Context
    ) {

    /** retrofit scheduleId는 스케줄 생성 후 response로 가져오기... **/

    fun addDiaryLocal(scheduleId: Int, hasDiary: Boolean, content: String, imgs: List<String>) {

        diaryDao.addDiary(scheduleId, hasDiary, content, imgs)
    }

    // 이미지 절대경로 변환
    @SuppressLint("Recycle")
    private fun absolutelyPath(path: Uri, context: Context): String {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val c: Cursor? = context.contentResolver.query(path, proj, null, null, null)
        val index = c?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        c?.moveToFirst()
        val result = c?.getString(index!!)

        return result!!
    }

    fun addDiaryRetrofit(scheduleId: Int, content: String, imgs: List<String>) {

        val imageMultiPart = imgs.map { imgPath ->
            val file = File(absolutelyPath(imgPath.toUri(), context))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryService.addDiary(imageMultiPart, contentRequestBody, scheduleIdRequestBody)
    }


    fun editDiaryLocal(scheduleId: Int, content: String, imgs: List<String>) {

        diaryDao.updateDiary(scheduleId, content, imgs)
    }

    fun editDiaryRetrofit(scheduleId: Int, content: String, imgs: List<String>) {

        val imageMultiPart = imgs.map { imgPath ->
            val file = File(absolutelyPath(imgPath.toUri(), context))
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryService.editDiary(imageMultiPart, contentRequestBody, scheduleIdRequestBody)
    }


    fun deleteDiaryLocal(scheduleId: Int, hasDiary: Boolean, content: String, imgs: List<String>) {

        diaryDao.deleteDiary(scheduleId, hasDiary, content, imgs)
    }

    fun deleteDiaryRetrofit(scheduleId: Int) {

        diaryService.deleteDiary(scheduleId)
    }

    fun getDateList(startMonth: Long, nextMonth: Long, hasDiary: Boolean): List<Long> {
        return diaryDao.getDateList(startMonth, nextMonth, hasDiary)
    }

    fun getMonthDiaryLocal(startMonth: Long, nextMonth: Long, hasDiary: Boolean): List<DiaryItem> {

        val eventList = diaryDao.getDiaryList(startMonth, nextMonth, hasDiary)
        return eventList.toListItems()

    }

    /** 같은 날짜끼리 묶어서 그룹 헤더로 추가 **/
    private fun List<Event>.toListItems(): List<DiaryItem> {
        val result = arrayListOf<DiaryItem>() // 결과를 리턴할 리스트

        var groupHeaderDate: Long = 0 // 그룹날짜
        this.forEach { task ->
            // 날짜가 달라지면 그룹 헤더를 추가

            if (groupHeaderDate != task.startLong) {
                result.add(DiaryItem.Header(task.startLong))
            }
            //  task 추가

            val category = categoryDao.getCategoryContent(task.categoryIdx)

            result.add(
                DiaryItem.Content(
                    task.eventId,
                    task.title,
                    task.startLong,
                    task.placeName,
                    task.categoryIdx,
                    category.color,
                    task.hasDiary,
                    task.content,
                    task.imgs
                )
            )

            // 그룹 날짜를 바로 이전 날짜로 설정
            groupHeaderDate = task.startLong
        }

        return result
    }


    @SuppressLint("SimpleDateFormat")
    fun getMonthDiaryRetrofit(yearMonth: String) {

        val yearMonthSplit = yearMonth.split(".")
        val year = yearMonthSplit[0]
        val month = yearMonthSplit[1].removePrefix("0")
        val formatYearMonth = "$year,$month"

        diaryService.getMonthDiary(formatYearMonth)
    }


    fun getDayDiaryLocal(scheduleId: Int): Event {

        return diaryDao.getSchedule(scheduleId)
    }

    fun getCategoryIdLocal(scheduleId: Int): Category {
        val event = getDayDiaryLocal(scheduleId)
        return categoryDao.getCategoryContent(event.categoryIdx)
    }

    fun getDayDiaryRetrofit(scheduleId: Int) {

        diaryService.getDayDiary(scheduleId)
    }

}


