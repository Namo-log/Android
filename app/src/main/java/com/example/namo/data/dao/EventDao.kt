package com.example.namo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.namo.data.entity.home.calendar.Event

@Dao
interface EventDao {
    @Insert
    fun insertEvent(event : Event)

    @Delete
    fun deleteEvent(event : Event)

    @Update
    fun updateEvent(event : Event)

    @Query("SELECT * FROM calendar_event_table WHERE event_start <= :todayEnd AND event_end >= :todayStart")
    fun getEventDaily(todayStart : Long, todayEnd : Long) : List<Event>
}