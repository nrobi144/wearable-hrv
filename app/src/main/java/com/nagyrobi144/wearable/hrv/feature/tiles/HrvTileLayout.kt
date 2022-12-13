package com.nagyrobi144.wearable.hrv.feature.tiles

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import com.nagyrobi144.wearable.hrv.R

@Composable
fun HrvTile(state: HrvTileState?) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize().background(Color.DarkGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            context.getString(
                R.string.average_rmssd,
                state?.averageHrv?.toString() ?: "N/A",
            )
        )
        Text(
            text = "${state?.minHrv} - ${state?.maxHrv}",
            modifier = GlanceModifier.padding(top = 16.dp)
        )
    }
}