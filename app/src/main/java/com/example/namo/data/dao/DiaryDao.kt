package com.example.namo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.namo.data.entity.diary.Diary
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Insert
    fun insertDiary(diary: Diary)

    @Update
    fun updateDiary(diary: Diary)

    @Delete
    fun deleteDiary(diary: Diary)

    @Query("SELECT * FROM diaryTable WHERE yearMonth =:yearMonth")
    fun getDiaryList(yearMonth: String) : List<Diary>

    @Query("SELECT * FROM diaryTable")
    fun getAll():List<Diary>
}