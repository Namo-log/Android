package com.example.namo.data.dao

import androidx.room.*
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.entity.home.Event


@Dao
interface DiaryDao {

    @Insert
    fun insertDiary(diary: Diary)

    @Update
    fun updateDiary(diary: Diary)

    @Delete
    fun deleteDiary(diary: Diary)

    @Query("UPDATE calendar_event_table SET has_diary= :hasDiary WHERE eventId =:scheduleIdx")
    fun updateHasDiary(hasDiary: Boolean, scheduleIdx: Int)

    @Query("UPDATE calendar_event_table SET has_diary= :hasDiary WHERE eventId =:scheduleIdx")
    fun deleteHasDiary(hasDiary: Boolean, scheduleIdx: Int)

    @Query("SELECT * FROM diaryTable WHERE scheduleIdx=:scheduleId")
    fun getDiaryDaily(scheduleId: Int): Diary

    @Query(
        "SELECT * FROM calendar_event_table JOIN diaryTable ON scheduleIdx = eventId " +
                "WHERE strftime('%Y.%m', event_start/1000, 'unixepoch') = :yearMonth  ORDER BY event_start DESC")
    fun getDiaryEventList(yearMonth: String): List<DiaryEvent>


}