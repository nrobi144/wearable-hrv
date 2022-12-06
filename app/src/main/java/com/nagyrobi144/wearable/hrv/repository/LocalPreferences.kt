package com.nagyrobi144.wearable.hrv.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val PASSIVE_DATA_ENABLED = booleanPreferencesKey("passive_data_enabled")
const val PREFERENCES_FILENAME = "hrv_tracker_prefs"

class LocalPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    val passiveDataEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PASSIVE_DATA_ENABLED] ?: false
    }

    suspend fun setPassiveDataEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PASSIVE_DATA_ENABLED] = enabled
        }
    }

}