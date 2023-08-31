package com.example.namo.data.dao

import androidx.room.*
import com.example.namo.R
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.diary.DiaryEvent
import com.example.namo.data.entity.home.Category
import com.example.namo.data.entity.home.Event

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertDiary(diary: Diary)

    @Update
    fun updateDiary(diary: Diary)

    @Query("DELETE FROM diaryTable WHERE diaryId=:eventId")
    fun deleteDiary(eventId: Long)

    @Query("UPDATE calendar_event_table SET has_diary= 1 WHERE eventId =:scheduleIdx")
    fun updateHasDiary(scheduleIdx: Long)

    @Query("UPDATE calendar_event_table SET has_diary= 0 WHERE eventId =:scheduleIdx")
    fun deleteHasDiary(scheduleIdx: Long)

    @Query("SELECT * FROM diaryTable WHERE diaryId=:scheduleId")
    fun getDiaryDaily(scheduleId: Long): Diary

//    @Query(
//        "SELECT * FROM calendar_event_table JOIN diaryTable ON diaryId = eventId " +
//                "WHERE strftime('%Y.%m', event_start, 'unixepoch') = :yearMonth AND diary_state != ${R.string.event_current_deleted} " +
//                "AND event_is_group = 0 ORDER BY event_start DESC LIMIT :size OFFSET :page"
//    )
//    fun getDiaryEventList(yearMonth: String, page: Int, size: Int): List<DiaryEvent>

    @Query(
        "SELECT * FROM calendar_event_table JOIN diaryTable ON diaryId = eventId " +
                "WHERE strftime('%Y.%m', event_start, 'unixepoch') = :yearMonth AND diary_state != ${R.string.event_current_deleted} " +
                "AND event_is_group = 0 ORDER BY event_start DESC "
    )
    fun getDiaryEventList(yearMonth: String): List<DiaryEvent>

    @Query("UPDATE diaryTable SET diary_upload=:isUpload, serverId=:serverId, diary_state=:state WHERE diaryId=:localId")
    fun updateDiaryAfterUpload(localId: Long, serverId: Long, isUpload: Int, state: String)

    @Query("SELECT * FROM diaryTable WHERE diary_upload = 0")
    fun getNotUploadedDiary(): List<Diary>

    @Query("SELECT * FROM calendar_event_table")
    fun getAllEvent(): List<Event>

}