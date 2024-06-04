package com.mongmong.namo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mongmong.namo.data.local.entity.home.Schedule

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insertSchedule(schedule : Schedule) : Long // scheduleId

    @Delete
    fun deleteSchedule(schedule : Schedule)

    @Update
    fun updateSchedule(schedule : Schedule)

    @Query("DELETE FROM schedule_table")
    fun deleteAllSchedules()

    @Query("SELECT COUNT(*) FROM schedule_table")
    fun getAllSchedule() : Int

    @Query("SELECT * FROM schedule_table WHERE startDate <= :todayEnd AND endDate >= :todayStart AND state != 'DELETED' ORDER BY dayInterval DESC")
    fun getScheduleDaily(todayStart : Long, todayEnd : Long) : List<Schedule>

    @Query("SELECT * FROM schedule_table WHERE startDate <= :monthEnd AND endDate >= :monthStart AND state != 'DELETED' ORDER BY dayInterval DESC")
    fun getScheduleMonth(monthStart : Long, monthEnd : Long) : List<Schedule>

    @Query("SELECT * FROM schedule_table WHERE startDate <= :calendarEnd AND endDate >= :calendarStart AND state != 'DELETED' ORDER BY dayInterval DESC")
    fun getScheduleCalendar(calendarStart : Long, calendarEnd : Long) : List<Schedule>

    @Query("SELECT * FROM schedule_table WHERE categoryId == :categoryId")
    fun getScheduleWithcategoryId(categoryId : Int) : List<Schedule>

    @Query("SELECT * FROM schedule_table WHERE isUpload = 0")
    fun getNotUploadedSchedule() : List<Schedule>

    @Query("UPDATE schedule_table SET isUpload=:isUpload, serverId=:serverId, state=:state WHERE scheduleId=:scheduleId")
    suspend fun updateScheduleAfterUpload(scheduleId : Long, isUpload : Boolean, serverId : Long, state : String)

    @Query("DELETE FROM schedule_table WHERE scheduleId=:scheduleId")
    fun deleteScheduleById(scheduleId : Long)

    @Query("SELECT * FROM schedule_table WHERE scheduleId=:scheduleId")
    fun getScheduleById(scheduleId: Long) : Schedule

    @Query("DELETE FROM schedule_table WHERE isMoim=:isGroup")
    fun deleteMoimSchedule(isGroup: Boolean)

    @Query("SELECT * FROM schedule_table WHERE isMoim=:isGroup")
    fun getMoimSchedule(isGroup: Boolean) : List<Schedule>
}