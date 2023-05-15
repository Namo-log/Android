package com.example.namo.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.namo.data.entity.home.Event


@Dao
interface DiaryDao {

    @Query("SELECT * FROM calendar_event_table WHERE eventId=:scheduleId")
    fun getSchedule(scheduleId: Int):Event

    @Query("UPDATE calendar_event_table SET event_has_diary=:hasDiary, diary_content=:content,diary_img=:imgs WHERE eventId=:scheduleId")
    fun addDiary(scheduleId:Int,hasDiary:Boolean, content:String, imgs:List<String>)

    @Query("UPDATE calendar_event_table SET diary_content=:content,diary_img=:imgs WHERE eventId=:scheduleId")
    fun updateDiary(scheduleId: Int,content: String,imgs: List<String>)

    @Query("UPDATE calendar_event_table SET event_has_diary=:hasDiary,diary_content=:content,diary_img=:imgs WHERE eventId=:scheduleId ")
    fun deleteDiary(scheduleId: Int,hasDiary: Boolean,content:String, imgs:List<String>)

    @Query("SELECT DISTINCT event_start FROM calendar_event_table WHERE event_start >= :startMonth AND event_start < :nextMonth AND event_has_diary=:hasDiary ORDER BY event_start")
    fun getDateList(startMonth:Long,nextMonth:Long,hasDiary: Boolean):List<Long>

    @Query("SELECT * FROM calendar_event_table WHERE event_start >= :startMonth AND event_start < :nextMonth AND event_has_diary=:hasDiary ORDER BY event_start")
    fun getDiaryList(startMonth:Long,nextMonth:Long,hasDiary: Boolean):List<Event>

}