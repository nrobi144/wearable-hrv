package com.nagyrobi144.wearable.hrv.feature.tiles

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.glance.wear.tiles.GlanceTileService
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import com.nagyrobi144.wearable.hrv.feature.TAG
import com.nagyrobi144.wearable.hrv.repository.IbiRepository
import com.nagyrobi144.wearable.hrv.util.createEpochsFrom
import com.nagyrobi144.wearable.hrv.util.filterTodaysData
import com.nagyrobi144.wearable.hrv.util.normaliseRRIntervals
import com.nagyrobi144.wearable.hrv.util.rMSSD
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// TODO submit an issue for Glance
abstract class UmbrellaGlanceTileService : GlanceTileService()

@AndroidEntryPoint
class HrvTileService : UmbrellaGlanceTileService(), LifecycleOwner {

    @Inject
    lateinit var ibiRepository: IbiRepository

    private lateinit var stateFlow: StateFlow<HrvTileState?>

    private val dispatcher = ServiceLifecycleDispatcher(this)

    override fun onCreate() {
        super.onCreate()
        stateFlow = ibiRepository.ibi
            .map { ibiList ->
                val dailyIbiList = ibiList.filterTodaysData()

                val earliest = dailyIbiList.minOf { it.timestamp }
                val latest = dailyIbiList.maxOf { it.timestamp }

                val timestampGroups = createEpochsFrom(earliest, latest)

                val groupedIbi = dailyIbiList
                    .groupBy { ibi -> timestampGroups.indexOfFirst { it.isAfter(ibi.instant) } }
                    .values

                val values = groupedIbi.mapNotNull { ibi ->
                    ibi.map { it.value }.normaliseRRIntervals().rMSSD()
                }
                val min = values.minOrNull() ?: return@map null
                val max = values.maxOrNull() ?: return@map null
                val average = values.average()
                Log.d(TAG, "onCreate: $min $max $average")
                HrvTileState(
                    averageHrv = average.toInt(),
                    minHrv = min.toInt(),
                    maxHrv = max.toInt()
                )
            }
            .catch {
                emit(null)
            }
            .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(), null)
    }

    override fun getLifecycle() = dispatcher.lifecycle

    @Composable
    override fun Content() {
        val state by stateFlow.collectAsState()
        HrvTile(state)
    }
}