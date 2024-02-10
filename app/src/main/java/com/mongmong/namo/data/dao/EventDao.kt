package com.mongmong.namo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mongmong.namo.R
import com.mongmong.namo.data.entity.home.Event

@Dao
interface EventDao {
    @Insert
    fun insertEvent(event : Event) : Long

    @Delete
    fun deleteEvent(event : Event)

    @Update
    fun updateEvent(event : Event)

    @Query("DELETE FROM calendar_event_table")
    fun deleteAllEvents()

    @Query("SELECT COUNT(*) FROM calendar_event_table")
    fun getAllEvent() : Int

    @Query("SELECT * FROM calendar_event_table WHERE event_start <= :todayEnd AND event_end >= :todayStart AND event_state != ${R.string.event_current_deleted} ORDER BY event_day_interval DESC")
    fun getEventDaily(todayStart : Long, todayEnd : Long) : List<Event>

    @Query("SELECT * FROM calendar_event_table WHERE event_start <= :monthEnd AND event_end >= :monthStart AND event_state != ${R.string.event_current_deleted} ORDER BY event_day_interval DESC")
    fun getEventMonth(monthStart : Long, monthEnd : Long) : List<Event>

    @Query("SELECT * FROM calendar_event_table WHERE event_start <= :calendarEnd AND event_end >= :calendarStart AND event_state != ${R.string.event_current_deleted} ORDER BY event_day_interval DESC")
    fun getEventCalendar(calendarStart : Long, calendarEnd : Long) : List<Event>

    @Query("SELECT * FROM calendar_event_table WHERE event_category_idx == :categoryIdx")
    fun getEventWithCategoryIdx(categoryIdx : Int) : List<Event>

    @Query("SELECT * FROM calendar_event_table WHERE event_upload = 0")
    fun getNotUploadedEvent() : List<Event>

    @Query("UPDATE calendar_event_table SET event_upload=:isUpload, event_server_idx=:serverIdx, event_state=:state WHERE eventId=:eventId")
    fun updateEventAfterUpload(eventId : Long, isUpload : Int, serverIdx : Long, state : String)

    @Query("DELETE FROM calendar_event_table WHERE eventId=:eventId")
    fun deleteEventById(eventId : Long)

    @Query("SELECT * FROM calendar_event_table WHERE eventId=:eventId")
    fun getEventById(eventId: Long) : Event

    @Query("DELETE FROM calendar_event_table WHERE event_is_group=:isGroup")
    fun deleteMoimEvent(isGroup: Boolean)

    @Query("SELECT * FROM calendar_event_table WHERE event_is_group=:isGroup")
    fun getMoimEvent(isGroup: Boolean) : List<Event>
}