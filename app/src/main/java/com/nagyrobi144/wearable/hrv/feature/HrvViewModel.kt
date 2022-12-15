package com.nagyrobi144.wearable.hrv.feature

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

    val chartData = repository.ibi.map { ibiList ->
        val dailyIbiList = ibiList.filterTodaysData()

        val earliest = dailyIbiList.minOf { it.timestamp }
        val latest = dailyIbiList.maxOf { it.timestamp }

        val timestampGroups = createEpochsFrom(earliest, latest)

        val groupedIbi = dailyIbiList
            .groupBy { ibi -> timestampGroups.indexOfFirst { it.isAfter(ibi.instant) } }
            .values

        val chartValues = groupedIbi.map { ibi ->
            val hrv =
                ibi.map { it.value }.sdnn()
            val hour = ibi.first().instant.deviceTimeZone().hour
            ChartValue(x = hour, y = hrv.toInt())
        }
//            // Calculate averages?
            .groupBy { it.x }
            .values.mapNotNull { hrvs ->
                val hour = hrvs.firstOrNull()?.x ?: return@mapNotNull null
                val average = hrvs.map { it.y }.average().toInt()

                ChartValue(x = hour, y = average)
            }
        ChartData(chartValues, xAxisValues)
    }.catch {
        emit(ChartData(emptyList(), xAxisValues))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ChartData(emptyList(), xAxisValues))

    val lowAndHighHrv = chartData.map { data ->
        val min = data.values.minOfOrNull { it.y } ?: return@map ""
        val max = data.values.maxOfOrNull { it.y } ?: return@map ""
        "$min - $max SDNN"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    val averageHrv = chartData.map { data ->
        data.values.map { it.y }.average().toInt().toString()
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
