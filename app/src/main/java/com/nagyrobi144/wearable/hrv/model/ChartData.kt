package com.nagyrobi144.wearable.hrv.model

import com.nagyrobi144.wearable.hrv.ui.ChartValue
import com.nagyrobi144.wearable.hrv.ui.XAxisValue

data class ChartData(val values: List<ChartValue>, val xAxisValues: List<XAxisValue>)