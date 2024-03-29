package com.nagyrobi144.wearable.hrv.health

import android.os.SystemClock
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.HeartRateAccuracy
import androidx.health.services.client.data.SampleDataPoint
import androidx.wear.tiles.TileService
import com.nagyrobi144.wearable.hrv.feature.tiles.HrvTileService
import com.nagyrobi144.wearable.hrv.repository.Ibi
import com.nagyrobi144.wearable.hrv.repository.IbiRepository
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class PassiveDataService : PassiveListenerService() {

    @Inject
    lateinit var repository: IbiRepository

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        val ibiList = dataPoints.getData(DataType.HEART_RATE_BPM).ibiBatch()
        repository.add(*ibiList.toTypedArray())
        TileService.getUpdater(this).requestUpdate(HrvTileService::class.java)
    }

}

private const val IBI_QUALITY_SHIFT = 15
private const val IBI_MASK = 0x1
private const val IBI_QUALITY_MASK = 0x7FFF

fun List<SampleDataPoint<Double>>.ibiBatch(): List<Ibi> {
    val heartData = this
        // dataPoints can have multiple types (e.g. if the app is registered for multiple types).
        .filter { it.dataType == DataType.HEART_RATE_BPM }
        // where accuracy information is available, only show readings that are of medium or
        // high accuracy. (Where accuracy information isn't available, show the reading if it is
        // a positive value).
        .filter {
            it.accuracy == null ||
                    setOf(
                        HeartRateAccuracy.SensorStatus.ACCURACY_HIGH,
                        HeartRateAccuracy.SensorStatus.ACCURACY_MEDIUM
                    ).contains((it.accuracy as HeartRateAccuracy).sensorStatus)
        }
        .filter {
            it.value > 0
        }

    return heartData
        .mapNotNull {
            val rawData = it.metadata.getInt("hr_rri")
            val ibi = rawData and IBI_QUALITY_MASK
            val instant =
                it.getTimeInstant(Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime()))

            val quality = (rawData shr IBI_QUALITY_SHIFT) and IBI_MASK

            if (quality == 1) return@mapNotNull null // ignore bad ibi

            Ibi(
                value = ibi,
                instant = instant,
            )
        }
        .distinctBy { it.timestamp }
}
