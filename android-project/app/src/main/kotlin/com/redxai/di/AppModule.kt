package com.redxai.di

import android.content.Context
import androidx.room.Room
import com.redxai.data.local.AppDatabase
import com.redxai.data.local.dao.BuildDao
import com.redxai.data.local.dao.ChatDao
import com.redxai.data.local.dao.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "redx_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideBuildDao(db: AppDatabase): BuildDao = db.buildDao()
}
