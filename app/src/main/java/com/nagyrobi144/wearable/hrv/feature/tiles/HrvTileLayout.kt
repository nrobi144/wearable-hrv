package com.nagyrobi144.wearable.hrv.feature.tiles

import android.content.Context
import androidx.wear.tiles.DeviceParametersBuilders
import androidx.wear.tiles.DimensionBuilders.expand
import androidx.wear.tiles.LayoutElementBuilders.*
import androidx.wear.tiles.material.layouts.PrimaryLayout
import com.nagyrobi144.wearable.hrv.R

fun hrvTileLayout(
    state: HrvTileState?,
    context: Context,
    deviceParameters: DeviceParametersBuilders.DeviceParameters
) = PrimaryLayout.Builder(deviceParameters)
    .setContent(
        Text.Builder()
            .setText(
                context.getString(
                    R.string.average_rmssd,
                    state?.averageHrv?.toString() ?: "N/A"
                )
            )
            .build()
    )
    .build()


fun hrvInformation(context: Context, state: HrvTileState?) = Column.Builder()
    .setWidth(expand())
    .setHeight(expand())
    .setHorizontalAlignment(HORIZONTAL_ALIGN_CENTER)
    .addContent(
        Text.Builder()
            .setText(
                context.getString(
                    R.string.average_rmssd,
                    state?.averageHrv?.toString() ?: "N/A"
                )
            )
            .build()
    )
    .addContent(
        Text.Builder()
            .setText("${state?.minHrv} - ${state?.maxHrv}")
            .build()
    )
    .build()