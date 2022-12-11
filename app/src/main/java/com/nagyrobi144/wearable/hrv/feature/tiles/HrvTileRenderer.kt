package com.nagyrobi144.wearable.hrv.feature.tiles

import android.content.Context
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.LayoutElementBuilders
import com.google.android.horologist.tiles.ExperimentalHorologistTilesApi
import com.google.android.horologist.tiles.render.SingleTileLayoutRenderer

@OptIn(ExperimentalHorologistTilesApi::class)
class HrvTileRenderer(context: Context) : SingleTileLayoutRenderer<HrvTileState?, Unit>(context) {

    override fun renderTile(
        state: HrvTileState?,
        deviceParameters: DeviceParametersBuilders.DeviceParameters
    ): LayoutElementBuilders.LayoutElement = hrvTileLayout(state, context, deviceParameters)
}