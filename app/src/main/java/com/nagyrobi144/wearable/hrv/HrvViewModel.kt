package com.nagyrobi144.wearable.hrv

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nagyrobi144.wearable.hrv.health.HealthServicesManager
import com.nagyrobi144.wearable.hrv.repository.IbiRepository
import com.nagyrobi144.wearable.hrv.repository.LocalPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    val rMSSD = repository.ibi
        .onEach {
            Log.i(TAG, "Data is $it")
        }
        .map { ibiList ->
            ibiList.mapNotNull {
                if (it.quality == 1) null else it.value
            }.rMSSD()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

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

fun List<Int>.rMSSD() = if (size <= 1) 0.0 else sqrt((0 until lastIndex).map { index ->
    val diff = this[index + 1] - this[index]
    diff * diff
}.average())

sealed class UiState {
    object Startup : UiState()
    object HeartRateAvailable : UiState()
    object HeartRateNotAvailable : UiState()
}