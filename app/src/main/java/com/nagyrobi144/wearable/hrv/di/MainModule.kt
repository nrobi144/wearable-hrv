package com.nagyrobi144.wearable.hrv.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.health.services.client.HealthServices
import androidx.health.services.client.HealthServicesClient
import androidx.room.Room
import com.nagyrobi144.wearable.hrv.db.IbiDatabase
import com.nagyrobi144.wearable.hrv.repository.PREFERENCES_FILENAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {
    @Singleton
    @Provides
    fun provideHealthServicesClient(@ApplicationContext context: Context): HealthServicesClient =
        HealthServices.getClient(context)

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context) = context.dataStore

    @Singleton
    @Provides
    fun provideIbiDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, IbiDatabase::class.java, "ibi_database").build()
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_FILENAME)
