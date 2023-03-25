package com.example.namo.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.namo.data.entity.home.Event


@Dao
interface DiaryDao {

//    @Insert
//    fun insertDiary(diary: Diary)
//
//    @Update
//    fun updateDiary(diary: Diary)
//
//    @Delete
//    fun deleteDiary(diary: Diary)
//
//    @Query("SELECT * FROM calendar_event_table WHERE eventId =:scheduleIdx")
//    fun getScheduleContent(scheduleIdx:Int): Event
//    @Query("SELECT * FROM diaryTable WHERE scheduleIdx=:scheduleIdx")
//    fun getDiaryContent(scheduleIdx: Int):Diary
//
//    /** 다이어리가 insert 되면 스케줄 idx에 따른 다이어리 여부 변경**/
//    @Query("UPDATE calendar_event_table SET event_has_diary= :hasDiary WHERE eventId =:scheduleIdx")
//    fun updateHasDiary(hasDiary: Boolean, scheduleIdx: Int)
//
//    /** 다이어리가 delete 되면 스케줄 idx에 따른 다이어리 여부 변경**/
//    @Query("UPDATE calendar_event_table SET event_has_diary= :hasDiary WHERE eventId =:scheduleIdx")
//    fun deleteHasDiary(hasDiary: Boolean, scheduleIdx: Int)
//
//    /** 월 별 **/
//    @Query("SELECT DISTINCT event_start FROM calendar_event_table WHERE event_start >= :startMonth AND event_start < :nextMonth ORDER BY event_start ")
//    fun getMonthList(startMonth:Long,nextMonth:Long):List<Long>
//
//    /** 날짜 별 **/
//    @Query("SELECT * FROM calendar_event_table JOIN diaryTable ON scheduleIdx = eventId " +
//            "WHERE event_start == :date ")
//    fun getDateList(date:Long):List<DiaryList>

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

    /** 월 별 다이어리 가져오기 **/
    @Query("SELECT * FROM calendar_event_table WHERE event_start >= :startMonth AND event_start < :nextMonth AND event_has_diary=:hasDiary ORDER BY event_start")
    fun getMonthList(startMonth:Long,nextMonth:Long,hasDiary: Boolean):List<Event>
}