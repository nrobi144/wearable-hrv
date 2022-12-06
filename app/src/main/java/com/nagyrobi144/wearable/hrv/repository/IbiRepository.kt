package com.nagyrobi144.wearable.hrv.repository

import com.nagyrobi144.wearable.hrv.db.IbiDatabase
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IbiRepository @Inject constructor(private val ibiDatabase: IbiDatabase) {

    val ibi = ibiDatabase.ibiDao().getAll().map { list -> list.map { it.toIbi() } }

    fun add(ibi: Ibi) {
        ibiDatabase.ibiDao().insertAll(ibi.toIbiEntity())
    }
}