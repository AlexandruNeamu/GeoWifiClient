package com.example.android.whileinuselocation.di

import android.content.Context
import com.example.android.whileinuselocation.data.db.ResultsDao
import com.example.android.whileinuselocation.data.db.ResultsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ResultModule {

    @Provides
    @Singleton
    fun provideData(@ApplicationContext context: Context): ResultsDatabase =
        ResultsDatabase.create(context)


    @Provides
    fun provideDao(database: ResultsDatabase): ResultsDao {
        return database.resultsDao()
    }
}