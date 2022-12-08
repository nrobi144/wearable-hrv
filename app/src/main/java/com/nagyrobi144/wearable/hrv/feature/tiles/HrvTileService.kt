package com.nagyrobi144.wearable.hrv.feature.tiles

import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.ResourceBuilders
import androidx.wear.tiles.TileBuilders
import com.google.android.horologist.tiles.ExperimentalHorologistTilesApi
import com.google.android.horologist.tiles.SuspendingTileService
import com.nagyrobi144.wearable.hrv.repository.IbiRepository
import com.nagyrobi144.wearable.hrv.util.createEpochsFrom
import com.nagyrobi144.wearable.hrv.util.filterTodaysData
import com.nagyrobi144.wearable.hrv.util.rMSSD
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalHorologistTilesApi::class)
@AndroidEntryPoint
class HrvTileService : SuspendingTileService() {

    @Inject
    lateinit var ibiRepository: IbiRepository
    private lateinit var hrvTileRenderer: HrvTileRenderer

    private lateinit var stateFlow: StateFlow<HrvTileState?>

    override fun onCreate() {
        super.onCreate()
        hrvTileRenderer = HrvTileRenderer(this)

        stateFlow = ibiRepository.ibi
            .map { ibiList ->
                val dailyIbiList = ibiList.filterTodaysData()

                val earliest = dailyIbiList.minOf { it.timestamp }
                val latest = dailyIbiList.maxOf { it.timestamp }

                Log.i(
                    com.nagyrobi144.wearable.hrv.feature.TAG,
                    "earliest: ${Instant.ofEpochMilli(earliest)}"
                )
                Log.i(
                    com.nagyrobi144.wearable.hrv.feature.TAG,
                    "latest: ${Instant.ofEpochMilli(latest)}"
                )

                val timestampGroups = createEpochsFrom(earliest, latest)
                Log.i(
                    com.nagyrobi144.wearable.hrv.feature.TAG,
                    "timestampGroups: ${timestampGroups.joinToString(" --- ")}"
                )


                val groupedIbi = dailyIbiList
                    .groupBy { ibi -> timestampGroups.indexOfFirst { it.isAfter(ibi.instant) } }
                    .values

                val values = groupedIbi.mapNotNull { ibi ->
                    ibi.map { it.value }.rMSSD()
                }
                val min = values.minOrNull() ?: return@map null
                val max = values.maxOrNull() ?: return@map null
                val average = values.average()
                HrvTileState(
                    averageHrv = average.toInt(),
                    minHrv = min.toInt(),
                    maxHrv = max.toInt()
                )
            }
            .catch {
                Log.w(com.nagyrobi144.wearable.hrv.feature.TAG, it.stackTraceToString())
                emit(null)
            }
            .stateIn(
                lifecycleScope,
                SharingStarted.WhileSubscribed(5000),
                null
            )
    }

    override suspend fun resourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ResourceBuilders.Resources =
        hrvTileRenderer.produceRequestedResources(Unit, requestParams)

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile =
        hrvTileRenderer.renderTimeline(stateFlow.filterNotNull().first(), requestParams)
}