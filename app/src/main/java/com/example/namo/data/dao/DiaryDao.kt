package com.example.namo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.namo.data.entity.diary.DiaryList
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.home.Event


@Dao
interface DiaryDao {

    @Insert
    fun insertDiary(diary: Diary)

    @Update
    fun updateDiary(diary: Diary)

    @Delete
    fun deleteDiary(diary: Diary)

    /** 스케줄 idx에 따른 내용, 장소, 카테고리 색상, 장소 가져오기 (다이어리 추가, 편집 화면)**/
    @Query("SELECT * FROM calendar_event_table WHERE eventId =:scheduleIdx")
    fun getScheduleContent(scheduleIdx:Int): Event
    @Query("SELECT * FROM diaryTable WHERE scheduleIdx=:scheduleIdx")
    fun getDiaryContent(scheduleIdx: Int):Diary

    /** 다이어리가 insert 되면 스케줄 idx에 따른 다이어리 여부 변경**/
    @Query("UPDATE calendar_event_table SET event_has_diary= :hasDiary WHERE eventId =:scheduleIdx")
    fun updateHasDiary(hasDiary: Boolean, scheduleIdx: Int)

    /** 다이어리가 delete 되면 스케줄 idx에 따른 다이어리 여부 변경**/
    @Query("UPDATE calendar_event_table SET event_has_diary= :hasDiary WHERE eventId =:scheduleIdx")
    fun deleteHasDiary(hasDiary: Boolean, scheduleIdx: Int)

    /** scheduleidx 로 테이블 합쳐서 startDate가 month인 리스트 출력 **/
    @Query("SELECT * FROM calendar_event_table JOIN diaryTable ON scheduleIdx = eventId " +
            "WHERE event_start >= :startMonth AND event_start < :nextMonth ORDER BY event_start ")
    fun getDiaryList(startMonth:Long,nextMonth:Long):List<DiaryList>

}