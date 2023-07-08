package com.example.namo.data.remote.diary


import com.example.namo.data.dao.CategoryDao
import com.example.namo.data.dao.DiaryDao
import com.example.namo.data.entity.diary.DiaryItem
import com.example.namo.data.entity.home.Event
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DiaryRepository(
    private val diaryDao: DiaryDao,
    private val categoryDao: CategoryDao,
    private val diaryService: DiaryService,

    private val diaryView: DiaryView,
    private val diaryDetailView: DiaryDetailView,
    private val getMonthDiaryView: GetMonthDiaryView,
    private val getDayDiaryView: GetDayDiaryView
) {


    /** scheduleId는 스케줄 생성 후 response로 가져오기 **/

    fun addDiary(scheduleId: Int, hasDiary: Boolean, content: String, imgs: List<String>) {

        val imageMultiPart = imgs.map { imgPath ->
            val file = File(imgPath)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryDao.addDiary(scheduleId, hasDiary, content, imgs)
        diaryService.addDiary(imageMultiPart, contentRequestBody, scheduleIdRequestBody)

        diaryService.addDiaryView(diaryView)
    }


    fun editDiary(scheduleId: Int, content: String, imgs: List<String>) {

        val imageMultiPart = imgs.map { imgPath ->
            val file = File(imgPath)
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("imgs", file.name, requestFile)
        }

        val contentRequestBody = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val scheduleIdRequestBody =
            scheduleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        diaryDao.updateDiary(scheduleId, content, imgs)
        diaryService.editDiary(imageMultiPart, contentRequestBody, scheduleIdRequestBody)

        diaryService.setDiaryView(diaryDetailView)

    }

    fun deleteDiary(scheduleId: Int, hasDiary: Boolean, content: String, imgs: List<String>) {

        diaryDao.deleteDiary(scheduleId, hasDiary, content, imgs)
        diaryService.deleteDiary(scheduleId)

        diaryService.setDiaryView(diaryDetailView)
    }

    fun getDateList(startMonth: Long, nextMonth: Long, hasDiary: Boolean): List<Long> {
        return diaryDao.getDateList(startMonth, nextMonth, hasDiary)
    }

    fun getMonthDiaryLocal(startMonth: Long, nextMonth: Long, hasDiary: Boolean): List<DiaryItem> {

        val eventList = diaryDao.getDiaryList(startMonth, nextMonth, hasDiary)
        return eventList.toListItems()

    }

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


    fun getMonthDiaryRetrofit(yearMonth: String) {

        val monthType = SimpleDateFormat("yyyy,M")
        val month = monthType.format(Date(yearMonth))

        diaryService.getMonthDiary(month)
        diaryService.getMonthDiaryView(getMonthDiaryView)
    }


    fun getDayDiaryLocal(scheduleId: Int): Event {
        return diaryDao.getSchedule(scheduleId)
    }

    fun getDayDiaryRetrofit(scheduleId: Int) {
        diaryService.getDayDiary(scheduleId)
        diaryService.getDayDiaryView(getDayDiaryView)
    }

}


