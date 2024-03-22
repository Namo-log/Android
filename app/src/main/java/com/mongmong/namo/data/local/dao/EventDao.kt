package com.mongmong.namo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mongmong.namo.R
import com.mongmong.namo.data.local.entity.home.Event

@Dao
interface EventDao {
    @Insert
    suspend fun insertEvent(event : Event)

    @Delete
    fun deleteEvent(event : Event)

    @Update
    fun updateEvent(event : Event)

    @Query("DELETE FROM schedule_table")
    fun deleteAllEvents()

    @Query("SELECT COUNT(*) FROM schedule_table")
    fun getAllEvent() : Int

    @Query("SELECT * FROM schedule_table WHERE startDate <= :todayEnd AND endDate >= :todayStart AND state != ${R.string.event_current_deleted} ORDER BY dayInterval DESC")
    fun getEventDaily(todayStart : Long, todayEnd : Long) : List<Event>

    @Query("SELECT * FROM schedule_table WHERE startDate <= :monthEnd AND endDate >= :monthStart AND state != ${R.string.event_current_deleted} ORDER BY dayInterval DESC")
    fun getEventMonth(monthStart : Long, monthEnd : Long) : List<Event>

    @Query("SELECT * FROM schedule_table WHERE startDate <= :calendarEnd AND endDate >= :calendarStart AND state != ${R.string.event_current_deleted} ORDER BY dayInterval DESC")
    fun getEventCalendar(calendarStart : Long, calendarEnd : Long) : List<Event>

    @Query("SELECT * FROM schedule_table WHERE categoryId == :categoryId")
    fun getEventWithcategoryId(categoryId : Int) : List<Event>

    @Query("SELECT * FROM schedule_table WHERE isUpload = 0")
    fun getNotUploadedEvent() : List<Event>

    @Query("UPDATE schedule_table SET isUpload=:isUpload, serverId=:serverId, state=:state WHERE scheduleId=:scheduleId")
    suspend fun updateEventAfterUpload(scheduleId : Long, isUpload : Int, serverId : Long, state : String)

    @Query("DELETE FROM schedule_table WHERE scheduleId=:scheduleId")
    fun deleteEventById(scheduleId : Long)

    @Query("SELECT * FROM schedule_table WHERE scheduleId=:scheduleId")
    fun getEventById(scheduleId: Long) : Event

    @Query("DELETE FROM schedule_table WHERE isMoim=:isGroup")
    fun deleteMoimEvent(isGroup: Boolean)

    @Query("SELECT * FROM schedule_table WHERE isMoim=:isGroup")
    fun getMoimEvent(isGroup: Boolean) : List<Event>
}