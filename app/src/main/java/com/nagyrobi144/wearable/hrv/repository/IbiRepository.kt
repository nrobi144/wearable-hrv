package com.nagyrobi144.wearable.hrv.repository

import com.nagyrobi144.wearable.hrv.db.Ibi
import com.nagyrobi144.wearable.hrv.db.IbiDatabase

class IbiRepository(private val ibiDatabase: IbiDatabase) {

    fun add(ibi: Ibi) {
        ibiDatabase.ibiDao().insertAll(ibi)
    }
}