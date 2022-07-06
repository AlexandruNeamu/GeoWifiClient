package com.example.android.whileinuselocation.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.android.whileinuselocation.model.GetElement

private const val DB_NAME="Results_table"


@Database(entities = [(GetElement::class)], version = 1)
abstract class ResultsDatabase:RoomDatabase() {
    abstract fun resultsDao(): ResultsDao

    companion object{
        fun create(context:Context):ResultsDatabase
        {
            return Room.databaseBuilder(context,
                ResultsDatabase::class.java,
                DB_NAME
            ).build()
        }
    }

}