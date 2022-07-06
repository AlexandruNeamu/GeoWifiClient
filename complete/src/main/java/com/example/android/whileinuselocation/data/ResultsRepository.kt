package com.example.android.whileinuselocation.data

import androidx.annotation.WorkerThread
import com.example.android.whileinuselocation.data.db.ResultsDao
import com.example.android.whileinuselocation.model.GetElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResultsRepository @Inject constructor(
    private val resultsDao: ResultsDao
) {
    fun getResults()=resultsDao.getResults()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
     fun updateResults(result:GetElement){
        CoroutineScope(Dispatchers.IO).launch {
            println("A intrat aici")
            //println(result)
            resultsDao.updateResults(result)
        }

    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun DeleteResults(){
        CoroutineScope(Dispatchers.IO).launch {
            println("A intrat aici in delete si atat")
            //println(result)
            resultsDao.deleteResults()
       }

    }
}