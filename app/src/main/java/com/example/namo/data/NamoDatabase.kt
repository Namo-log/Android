package com.example.namo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.namo.data.dao.CategoryDao
import com.example.namo.data.dao.DiaryDao
import com.example.namo.data.dao.EventDao
import com.example.namo.data.entity.home.Event
import com.example.namo.data.entity.diary.DiaryList
import com.example.namo.data.entity.diary.Diary
import com.example.namo.ui.bottom.diary.adapter.Converters
import com.example.namo.ui.bottom.home.schedule.data.Category

@Database(entities = [Event::class,Category::class,Diary::class,DiaryList::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NamoDatabase : RoomDatabase() {
    abstract val eventDao : EventDao
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