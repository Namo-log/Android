package com.example.namo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.example.namo.data.entity.diary.Diary
import com.example.namo.data.entity.diary.Gallery

@Dao
interface DiaryDao {

    @Update
    fun updateDiary(diary: Diary)

    @Update
    fun updateDiaryImg(gallery: Gallery)

    @Delete
    fun deleteDiary(diary: Diary)

    @Delete
    fun deleteDiaryImg(gallery: Gallery)

    @Query(
        "SELECT * FROM diaryTable " +
                "INNER JOIN homeCalendarEvent ON homeCalendarEvent.eventId = diaryTable.scheduleIdx " +
                "INNER JOIN diaryGalleryTable ON diaryGalleryTable.diaryIdx =diaryTable.diaryIdx " +
                "WHERE homeCalendarEvent.startLong LIKE :month"
    )
    fun getDiaryList(month: String): List<Diary>

}