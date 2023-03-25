package com.example.namo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.namo.data.entity.home.Event

@Dao
interface EventDao {
    @Insert
    fun insertEvent(event : Event)

    @Delete
    fun deleteEvent(event : Event)

    @Update
    fun updateEvent(event : Event)

    @Query("SELECT * FROM calendar_event_table WHERE event_start <= :todayEnd AND event_end >= :todayStart ORDER BY event_day_interval DESC")
    fun getEventDaily(todayStart : Long, todayEnd : Long) : List<Event>

    @Query("SELECT * FROM calendar_event_table WHERE event_start <= :monthEnd AND event_end >= :monthStart ORDER BY event_day_interval DESC")
    fun getEventMonth(monthStart : Long, monthEnd : Long) : List<Event>

    @Query("SELECT * FROM calendar_event_table WHERE event_start <= :calendarEnd AND event_end >= :calendarStart ORDER BY event_day_interval DESC")
    fun getEventCalendar(calendarStart : Long, calendarEnd : Long) : List<Event>

    @Query("UPDATE calendar_event_table SET event_title = :title, event_start = :startLong, event_end = :endLong, event_day_interval = :dayInterval, event_category_color = :categoryColor, event_category_name = :categoryName, event_category_idx = :categoryIdx, event_place = :place WHERE eventId = :eventId")
    fun updateWithEventID(eventId : Int, title : String, startLong: Long, endLong: Long, dayInterval : Int, categoryColor : Int, categoryName: String, categoryIdx : Int, place : String)
}