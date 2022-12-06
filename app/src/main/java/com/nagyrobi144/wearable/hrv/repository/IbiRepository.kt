package com.nagyrobi144.wearable.hrv.repository

import com.nagyrobi144.wearable.hrv.db.Ibi
import com.nagyrobi144.wearable.hrv.db.IbiDatabase
import javax.inject.Inject

class IbiRepository @Inject constructor(private val ibiDatabase: IbiDatabase) {

    val ibi = ibiDatabase.ibiDao().getAll()

    fun add(ibi: Ibi) {
        ibiDatabase.ibiDao().insertAll(ibi)
    }
}