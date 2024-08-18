package com.mongmong.namo.data.local.dao

import androidx.room.*
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.domain.model.DiarySchedule
import com.mongmong.namo.data.local.entity.home.Schedule

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDiary(diary: Diary)

    @Update
    fun updateDiary(diary: Diary)

    @Query("DELETE FROM diary_table WHERE diaryId=:scheduleId")
    fun deleteDiary(scheduleId: Long)

    @Query("DELETE FROM diary_table")
    fun deleteAllDiaries()

    @Query("UPDATE schedule_table SET hasDiary= 1 WHERE scheduleId =:scheduleIdx")
    fun updateHasDiary(scheduleIdx: Long)

    @Query("UPDATE schedule_table SET hasDiary= 0 WHERE scheduleId =:scheduleIdx")
    fun deleteHasDiary(scheduleIdx: Long)

    @Query("SELECT * FROM diary_table WHERE diaryId=:scheduleId")
    fun getDiaryDaily(scheduleId: Long): Diary

//    @Query(
//        "SELECT * FROM schedule_table e JOIN diary_table d ON diaryId = scheduleId " +
//                "WHERE e.startDate >= :monthStartDate AND e.startDate < :nextMonthEndDate AND d.state != 'DELETED' " +
//                "AND e.isMoim = 0 ORDER BY e.startDate DESC LIMIT :size OFFSET :page * :size"
//    )
//    fun getDiaryScheduleList(monthStartDate: Long, nextMonthEndDate: Long, page: Int, size: Int): List<DiarySchedule>

    @Query("UPDATE diary_table SET isUpload=:isUpload, scheduleServerId=:serverId, state=:state WHERE diaryId=:localId")
    suspend fun updateDiaryAfterUpload(localId: Long, serverId: Long, isUpload: Boolean, state: String)

    @Query("SELECT * FROM diary_table WHERE isUpload = 0")
    fun getNotUploadedDiary(): List<Diary>

    @Query("SELECT * FROM schedule_table")
    fun getAllSchedule(): List<Schedule>

}