package com.nagyrobi144.wearable.hrv.ui

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import com.nagyrobi144.wearable.hrv.model.ChartData

@Composable
fun Chart(data: ChartData, modifier: Modifier = Modifier) {

    val chartValueColor = MaterialTheme.colors.primary

    Canvas(modifier = modifier.fillMaxSize()) {
        val maxValue = data.values.maxOfOrNull { it.y } ?: size.height.toInt()
        val padding = (size.width / data.xAxisValues.size) * 0.8f
        val heightMultiplier = (size.height / maxValue) * 0.8f
        drawIntoCanvas { canvas ->
            val radius = 1.dp.toPx()
            val textSize = 14.sp.toPx()
            val textPadding = 2.dp.toPx()
            val rectWidth = 2.dp.toPx()

            data.values.forEach { value ->
                val xOffset = data.xAxisValues.indexOfFirst { it.value == value.x }
                val yOffset = size.height - radius / 2 - textSize - textPadding - 4.dp.toPx()
                val height = heightMultiplier * value.y

                drawRect(
                    chartValueColor,
                    topLeft = Offset(
                        x = xOffset * (radius + padding) - rectWidth / 2,
                        y = yOffset - height
                    ),
                    size = Size(rectWidth, height)
                )
            }
            data.xAxisValues.forEachIndexed { index, xAxisValue ->
                drawCircle(
                    color = Color.Gray, radius = radius, center = Offset(
                        x = index * (radius + padding),
                        y = size.height - radius / 2 - textSize - textPadding,
                    )
                )
                val textPaint = Paint().apply {
                    color = Color.Gray.toArgb()
                    this.textSize = textSize
                }
                xAxisValue.label?.let {
                    val bounds = Rect()
                    textPaint.getTextBounds(it, 0, it.length, bounds)
                    canvas.nativeCanvas.drawText(
                        it,
                        index * (radius + padding) - bounds.width() / 2,
                        size.height - radius / 2,
                        textPaint
                    )
                }
            }
        }
    }
}