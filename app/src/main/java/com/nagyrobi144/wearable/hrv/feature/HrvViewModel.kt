package com.nagyrobi144.wearable.hrv.feature

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagyrobi144.wearable.hrv.health.HealthServicesManager
import com.nagyrobi144.wearable.hrv.model.ChartData
import com.nagyrobi144.wearable.hrv.repository.IbiRepository
import com.nagyrobi144.wearable.hrv.repository.LocalPreferences
import com.nagyrobi144.wearable.hrv.ui.ChartValue
import com.nagyrobi144.wearable.hrv.ui.XAxisValue
import com.nagyrobi144.wearable.hrv.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class HrvViewModel @Inject constructor(
    repository: IbiRepository,
    private val localPreferences: LocalPreferences,
    private val healthServicesManager: HealthServicesManager
) : ViewModel() {

    val passiveDataEnabled = localPreferences.passiveDataEnabled
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    private val xAxisValues = (0..24).map { hour ->
        XAxisValue(
            label = hour.takeIf { it % 6 == 0 }?.toString(),
            value = hour
        )
    }

    val chartData = repository.ibi.onEach {
        Log.i(TAG, "Data is $it")
    }.map { ibiList ->
        val dailyIbiList = ibiList.filterTodaysData()

        val earliest = dailyIbiList.minOf { it.timestamp }
        val latest = dailyIbiList.maxOf { it.timestamp }

        Log.i(TAG, "earliest: ${Instant.ofEpochMilli(earliest)}")
        Log.i(TAG, "latest: ${Instant.ofEpochMilli(latest)}")

        val timestampGroups = createEpochsFrom(earliest, latest)
        Log.i(TAG, "timestampGroups: ${timestampGroups.joinToString(" --- ")}")


        val groupedIbi = dailyIbiList
            .groupBy { ibi -> timestampGroups.indexOfFirst { it.isAfter(ibi.instant) } }
            .values

        val chartValues = groupedIbi.mapNotNull { ibi ->
            val rMSSd =
                ibi.map { it.value }.normaliseRRIntervals().rMSSD() ?: return@mapNotNull null
            val hour = ibi.first().instant.deviceTimeZone().hour
            ChartValue(x = hour, y = rMSSd.toInt())
        }
//            // Calculate averages?
            .groupBy { it.x }
            .values.mapNotNull { rMSSDs ->
                val hour = rMSSDs.firstOrNull()?.x ?: return@mapNotNull null
                val average = rMSSDs.map { it.y }.average().toInt()

                Log.i(TAG, "rMSSd: $average at $hour")
                ChartValue(x = hour, y = average)
            }
        ChartData(chartValues, xAxisValues)
    }.catch {
        Log.w(TAG, it.stackTraceToString())
        emit(ChartData(emptyList(), xAxisValues))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ChartData(emptyList(), xAxisValues))

    val lowAndHighRMSSD = chartData.map { data ->
        val min = data.values.minOfOrNull { it.y } ?: return@map ""
        val max = data.values.maxOfOrNull { it.y } ?: return@map ""
        "$min - $max rMSSD"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val averageRMSSD = chartData.map { data ->
        data.values.map { it.y }.average().toString()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    init {
        viewModelScope.launch {
            passiveDataEnabled.collect { enabled ->
                viewModelScope.launch {
                    if (enabled)
                        healthServicesManager.registerForHeartRateData()
                    else
                        healthServicesManager.unregisterForHeartRateData()
                }
            }
        }
    }

    fun togglePassiveData(enabled: Boolean) {
        viewModelScope.launch {
            localPreferences.setPassiveDataEnabled(enabled)
        }
    }
}
