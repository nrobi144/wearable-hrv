package com.nagyrobi144.wearable.hrv

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagyrobi144.wearable.hrv.health.HealthServicesManager
import com.nagyrobi144.wearable.hrv.repository.IbiRepository
import com.nagyrobi144.wearable.hrv.repository.LocalPreferences
import com.nagyrobi144.wearable.hrv.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class HrvViewModel @Inject constructor(
    repository: IbiRepository,
    private val localPreferences: LocalPreferences,
    private val healthServicesManager: HealthServicesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Startup)
    val uiState: StateFlow<UiState> = _uiState

    val passiveDataEnabled = localPreferences.passiveDataEnabled
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val rMSSDs = repository.ibi.onEach {
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

        groupedIbi.mapNotNull { ibi ->
            val rMSSd = ibi.map { it.value }.rMSSD() ?: return@mapNotNull null
            rMSSd.toInt() to ibi.first().instant.deviceTimeZone().hour
        }
//            // Calculate averages?
//            .groupBy { it.second }
//            .values.mapNotNull { rMSSDs ->
//                val hour = rMSSDs.firstOrNull()?.second ?: return@mapNotNull null
//                val average = rMSSDs.map { it.first }.average().toInt()
//
//                Log.i(TAG, "rMSSd: $average at $hour")
//                average to hour
//            }
    }.catch {
        Log.w(TAG, it.stackTraceToString())
        emit(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val lowAndHighRMSSD = rMSSDs.map { rMSSDs ->
        val min = rMSSDs.minOfOrNull { it.first } ?: return@map ""
        val max = rMSSDs.maxOfOrNull { it.first } ?: return@map ""
        "$min - $max rMSSD"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    init {
        // Check that the device has the heart rate capability and progress to the next state
        // accordingly.
        viewModelScope.launch {
            _uiState.value = if (healthServicesManager.hasHeartRateCapability()) {
                UiState.HeartRateAvailable
            } else {
                UiState.HeartRateNotAvailable
            }
        }

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

fun List<Int>.rMSSD() = if (size <= 1) null else sqrt((0 until lastIndex).map { index ->
    val diff = this[index + 1] - this[index]
    diff * diff
}.average())

sealed class UiState {
    object Startup : UiState()
    object HeartRateAvailable : UiState()
    object HeartRateNotAvailable : UiState()
}