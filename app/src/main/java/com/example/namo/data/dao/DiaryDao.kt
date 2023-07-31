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

    @Query("DELETE FROM diaryTable WHERE diaryLocalId=:eventId")
    fun deleteDiary(eventId:Long)

    @Query("UPDATE calendar_event_table SET has_diary= :hasDiary WHERE eventId =:scheduleIdx")
    fun updateHasDiary(hasDiary: Int, scheduleIdx: Long)

    @Query("UPDATE calendar_event_table SET has_diary= :hasDiary WHERE eventId =:scheduleIdx")
    fun deleteHasDiary(hasDiary: Int, scheduleIdx: Long)

    @Query("SELECT * FROM diaryTable WHERE diaryLocalId=:scheduleId")
    fun getDiaryDaily(scheduleId: Long): Diary

    @Query(
        "SELECT * FROM calendar_event_table JOIN diaryTable ON diaryLocalId = eventId " +
                "WHERE strftime('%Y.%m', event_start/1000, 'unixepoch') = :yearMonth  ORDER BY event_start DESC")
    fun getDiaryEventList(yearMonth: String): List<DiaryEvent>

    @Query("UPDATE diaryTable SET diary_upload=:isUpload, scheduleId=:serverIdx, diary_state=:state WHERE diaryLocalId=:localId")
    fun updateDiaryAfterUpload(localId :Long, isUpload : Int, serverIdx : Long, state : String)

    @Query("SELECT * FROM diaryTable WHERE diary_upload = 0")
    fun getNotUploadedDiary() : List<Diary>

}