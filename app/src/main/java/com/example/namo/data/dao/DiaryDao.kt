package com.example.namo.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.namo.data.entity.home.Event


@Dao
interface DiaryDao {


    @Query("SELECT * FROM calendar_event_table WHERE eventId=:scheduleId")
    fun getSchedule(scheduleId: Int):Event

    /** 다이어리 추가 (hasDiary=True)**/
    @Query("UPDATE calendar_event_table SET event_has_diary=:hasDiary, diary_content=:content,diary_img=:imgs WHERE eventId=:scheduleId")
    fun addDiary(scheduleId:Int,hasDiary:Boolean, content:String, imgs:List<String>)
    /** 다이어리 수정 **/

    @Query("UPDATE calendar_event_table SET diary_content=:content,diary_img=:imgs WHERE eventId=:scheduleId")
    fun updateDiary(scheduleId: Int,content: String,imgs: List<String>)

    /** 다이어리 삭제 (hasDiary=False)**/
    @Query("UPDATE calendar_event_table SET event_has_diary=:hasDiary,diary_content=:content,diary_img=:imgs WHERE eventId=:scheduleId ")
    fun deleteDiary(scheduleId: Int,hasDiary: Boolean,content:String, imgs:List<String>)

    /** 월 별 날짜 리스트 **/
    @Query("SELECT DISTINCT event_start FROM calendar_event_table WHERE event_start >= :startMonth AND event_start < :nextMonth AND event_has_diary=:hasDiary ORDER BY event_start")
    fun getMonthList(startMonth:Long,nextMonth:Long,hasDiary: Boolean):List<Long>

    /** 날짜 별 다이어리 리스트 **/
    @Query("SELECT * FROM calendar_event_table WHERE event_start == :date ")
    fun getDateList(date:Long):List<Event>


}