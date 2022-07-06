package com.example.android.whileinuselocation.data.db

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.room.*
import com.example.android.whileinuselocation.model.GetElement
import com.google.android.gms.maps.model.Marker




@Dao
interface ResultsDao {
    @Transaction
     fun updateResults(results:GetElement)
    {
        results.let {

            insertResult(it)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun insertResult(result:com.example.android.whileinuselocation.model.GetElement)

    @Query("DELETE FROM Results_table")
     fun deleteResults()

    @Query("SELECT * FROM Results_table")
    fun getResults(): List<GetElement>

}