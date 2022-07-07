package com.example.android.whileinuselocation.data

import androidx.annotation.WorkerThread
import com.example.android.whileinuselocation.data.db.LocationDao
import com.example.android.whileinuselocation.model.Location
import javax.inject.Inject

class LocationRepository @Inject constructor(
        private val locationDao: LocationDao
) {

    fun getLocations() = locationDao.getLocations()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateLocation(location: Location) {
        locationDao.updateLocation(location)
    }
}
