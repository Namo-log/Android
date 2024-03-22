package com.mongmong.namo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mongmong.namo.data.local.dao.CategoryDao
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.dao.ScheduleDao
import com.mongmong.namo.data.local.entity.diary.Diary
import com.mongmong.namo.data.local.entity.home.Schedule
import com.mongmong.namo.data.local.entity.home.Category

@Database(entities = [Schedule::class, Category::class, Diary::class], version = 1, exportSchema = false)
@TypeConverters(
    value = [
        StringListConverters::class,
        IntListConverters::class,
        ScheduleListConverters::class
    ]
)
abstract class NamoDatabase : RoomDatabase() {
    abstract val scheduleDao : ScheduleDao
    abstract val diaryDao : DiaryDao
    abstract val categoryDao : CategoryDao

    companion object {
        @Volatile
        private var INSTANCE : NamoDatabase? = null

        fun getInstance(context : Context) : NamoDatabase {
            synchronized(this){
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        NamoDatabase::class.java,
                        "namo_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}