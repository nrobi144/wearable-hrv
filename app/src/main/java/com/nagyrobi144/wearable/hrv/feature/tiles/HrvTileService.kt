package com.nagyrobi144.wearable.hrv.feature.tiles

import androidx.compose.runtime.*
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.LocalGlanceId
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.wear.tiles.GlanceTileService
import androidx.glance.wear.tiles.state.updateWearTileState
import com.nagyrobi144.wearable.hrv.repository.IbiRepository
import com.nagyrobi144.wearable.hrv.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// TODO submit an issue for Glance
abstract class UmbrellaGlanceTileService : GlanceTileService()

private val minHrvKey = intPreferencesKey("minHrv")
private val maxHrvKey = intPreferencesKey("maxHrv")
private val averageHrvKey = intPreferencesKey("averageHrv")

@AndroidEntryPoint
class HrvTileService : UmbrellaGlanceTileService() {

    @Inject
    lateinit var ibiRepository: IbiRepository

    override val stateDefinition = PreferencesGlanceStateDefinition

    private suspend fun writePreferences(hrvTileState: HrvTileState, glanceId: GlanceId) {
        updateWearTileState(
            this@HrvTileService,
            PreferencesGlanceStateDefinition,
            glanceId
        ) { prefs ->
            prefs.toMutablePreferences().apply {
                set(minHrvKey, hrvTileState.minHrv)
                set(maxHrvKey, hrvTileState.maxHrv)
                set(averageHrvKey, hrvTileState.averageHrv)
            }
        }
    }

    @Composable
    private fun readPreferences(): HrvTileState {
        val minHrv = currentState<Preferences>()[minHrvKey]
        val maxHrv = currentState<Preferences>()[maxHrvKey]
        val averageHrv = currentState<Preferences>()[averageHrvKey]
        return HrvTileState(
            minHrv = minHrv ?: 0,
            maxHrv = maxHrv ?: 0,
            averageHrv = averageHrv ?: 0
        )
    }

    private suspend fun subscribeToData(glanceId: GlanceId) {
        ibiRepository.ibi
            .map { ibiList ->
                val dailyIbiList = ibiList.filterTodaysData()

                val earliest = dailyIbiList.minOf { it.timestamp }
                val latest = dailyIbiList.maxOf { it.timestamp }

                val timestampGroups = createEpochsFrom(earliest, latest)

                val groupedIbi = dailyIbiList
                    .groupBy { ibi -> timestampGroups.indexOfFirst { it.isAfter(ibi.instant) } }
                    .values

                val values = groupedIbi.mapNotNull { ibi ->
                    ibi.map { it.value }.sdnn()
                }
                val min = values.minOrNull() ?: return@map null
                val max = values.maxOrNull() ?: return@map null
                val average = values.average()

                writePreferences(
                    HrvTileState(
                        averageHrv = average.toInt(),
                        minHrv = min.toInt(),
                        maxHrv = max.toInt()
                    ), glanceId
                )
            }
            .catch {
                emit(null)
            }
            .collect()
    }

    @Composable
    override fun Content() {
        val state = readPreferences()
        val glanceId = LocalGlanceId.current
        HrvTile(state)
        LaunchedEffect(glanceId) {
            subscribeToData(glanceId)
        }
    }
}
