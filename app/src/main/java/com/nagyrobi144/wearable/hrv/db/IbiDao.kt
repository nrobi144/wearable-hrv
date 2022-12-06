package com.nagyrobi144.wearable.hrv.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IbiDao {

    @Query("SELECT * FROM ibi")
    fun getAll(): Flow<List<Ibi>>

    @Insert
    fun insertAll(vararg ibi: Ibi)

    @Delete
    fun delete(ibi: Ibi)
}