package com.mongmong.namo.presentation.di

import android.content.Context
import androidx.room.Room
import com.mongmong.namo.data.local.NamoDatabase
import com.mongmong.namo.data.local.dao.DiaryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideNamoDatabase(@ApplicationContext context: Context): NamoDatabase =
        Room.databaseBuilder(context, NamoDatabase::class.java, "namo_database").build()

    /** 기록 */
    @Provides
    fun provideDiaryDao(database: NamoDatabase): DiaryDao = database.diaryDao
}