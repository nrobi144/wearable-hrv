package com.nagyrobi144.wearable.hrv.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IbiDao {

    @Query("SELECT * FROM ibi ORDER BY timestamp DESC")
    fun getAll(): Flow<List<IbiEntity>>

    @Insert
    fun insertAll(vararg ibi: IbiEntity)

    @Delete
    fun delete(ibi: IbiEntity)
}