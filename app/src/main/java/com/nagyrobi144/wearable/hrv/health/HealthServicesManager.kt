package com.nagyrobi144.wearable.hrv.health

import android.content.Context
import android.util.Log
import androidx.concurrent.futures.await
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import com.nagyrobi144.wearable.hrv.TAG

class HealthServicesManager(context: Context) {

    private val client = HealthServices.getClient(context)
    private val monitoringClient = client.passiveMonitoringClient
    private val dataTypes = setOf(DataType.HEART_RATE_BPM)

    suspend fun hasHeartRateCapability() =
        DataType.HEART_RATE_BPM in monitoringClient.getCapabilitiesAsync()
            .await()
            .supportedDataTypesPassiveMonitoring

    suspend fun registerForHeartRateData() {
        val passiveListenerConfig = PassiveListenerConfig.builder()
            .setDataTypes(dataTypes)
            .build()

        Log.i(TAG, "Registering listener")
        monitoringClient.setPassiveListenerServiceAsync(
            PassiveDataService::class.java,
            passiveListenerConfig
        ).await()
    }

    suspend fun unregisterForHeartRateData() {
        Log.i(TAG, "Unregistering listeners")
        monitoringClient.clearPassiveListenerServiceAsync().await()
    }}