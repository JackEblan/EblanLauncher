package com.eblan.launcher.ui.dialog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialogContainer


@Composable
fun ColorPickerDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onColorSelected: (Int) -> Unit,
) {
    // State: Hue (0-360), Saturation (0-1), Value (0-1), Alpha (0-1)
    var hue by remember { mutableFloatStateOf(0f) }

    var saturation by remember { mutableFloatStateOf(1f) }

    var value by remember { mutableFloatStateOf(1f) }

    var alpha by remember { mutableFloatStateOf(1f) }

    val currentColor by remember {
        derivedStateOf {
            Color.hsv(hue, saturation, value).copy(alpha = alpha)
        }
    }

    val colorWithoutAlpha by remember {
        derivedStateOf {
            Color.hsv(hue, saturation, value)
        }
    }

    EblanDialogContainer(onDismissRequest = onDismissRequest) {
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
        ) {
            // 1. Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(currentColor),
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Saturation & Value Selection Square
            SaturationValuePanel(
                hue = hue,
                saturation = saturation,
                value = value,
                onSaturationSelected = { newSaturation ->
                    saturation = newSaturation
                },
                onValueSelected = { newValue ->
                    value = newValue
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Hue Selection Bar
            HueBar(hue = hue) {
                hue = it
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Alpha Selection Bar
            AlphaBar(alpha = alpha, activeColor = colorWithoutAlpha) {
                alpha = it
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = 10.dp,
                        bottom = 10.dp,
                    ),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.width(5.dp))

                TextButton(
                    onClick = {
                        onColorSelected(currentColor.toArgb())

                        onDismissRequest()
                    },
                ) {
                    Text("Save")
                }
            }

        }
    }
}

@Composable
private fun SaturationValuePanel(
    modifier: Modifier = Modifier,
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationSelected: (Float) -> Unit,
    onValueSelected: (Float) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        val width = constraints.maxWidth.toFloat()

        val height = constraints.maxHeight.toFloat()

        SaturationValueCanvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        onSaturationSelected((change.position.x / width).coerceIn(0f, 1f))

                        onValueSelected(1f - (change.position.y / height).coerceIn(0f, 1f))
                    }
                },
            hue = hue,
            saturation = saturation,
            value = value,
        )
    }
}

@Composable
private fun SaturationValueCanvas(
    modifier: Modifier = Modifier,
    hue: Float,
    saturation: Float,
    value: Float,
) {
    Canvas(modifier = modifier) {
        // Background: Hue color
        drawRect(color = Color.hsv(hue, 1f, 1f))

        // Layer 1: White to Transparent (Saturation)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White, Color.Transparent),
            ),
        )

        // Layer 2: Transparent to Black (Value/Brightness)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black),
            ),
        )

        // Indicator
        val indicatorX = saturation * size.width
        val indicatorY = (1f - value) * size.height

        // Draw shadow/outline for the indicator
        drawCircle(
            color = Color.Black,
            radius = 12f,
            center = Offset(indicatorX, indicatorY),
            style = Stroke(width = 4f),
        )

        // Draw the white ring
        drawCircle(
            color = Color.White,
            radius = 12f,
            center = Offset(indicatorX, indicatorY),
            style = Stroke(width = 2f),
        )
    }
}


@Composable
private fun HueBar(
    modifier: Modifier = Modifier,
    hue: Float,
    onHueSelected: (Float) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        val width = constraints.maxWidth.toFloat()

        HueBarCanvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        onHueSelected((change.position.x / width).coerceIn(0f, 1f) * 360f)
                    }
                },
            hue = hue,
        )
    }
}

@Composable
private fun HueBarCanvas(
    modifier: Modifier,
    hue: Float,
) {
    Canvas(modifier = modifier) {
        // Draw the rainbow gradient
        val hueColors = listOf(
            Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red,
        )
        drawRect(
            brush = Brush.horizontalGradient(hueColors),
            size = size,
        )

        // Indicator (Vertical Line)
        val selectorX = (hue / 360f) * size.width

        // White vertical line
        drawRect(
            color = Color.White,
            topLeft = Offset(selectorX - 4f, 0f),
            size = Size(8f, size.height),
        )

        // Black outline for the line (to make it visible on light colors)
        drawRect(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(selectorX - 4f, 0f),
            size = Size(8f, size.height),
            style = Stroke(width = 1f),
        )
    }
}

@Composable
private fun AlphaBar(
    alpha: Float,
    activeColor: Color,
    onAlphaSelected: (Float) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        val width = constraints.maxWidth.toFloat()

        val height = constraints.maxHeight.toFloat()

        AlphaBarCanvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        onAlphaSelected((change.position.x / width).coerceIn(0f, 1f))
                    }
                },
            alpha = alpha,
            activeColor = activeColor,
            width = width,
            height = height,
        )
    }
}

@Composable
private fun AlphaBarCanvas(
    modifier: Modifier,
    alpha: Float,
    activeColor: Color,
    width: Float,
    height: Float,
) {
    Canvas(modifier = modifier) {
        // Draw the checkerboard pattern
        val squareSize = 20.dp.toPx()
        var isBlackSquareStartOfRow = false
        var y = 0f
        while (y < height) {
            var x = 0f
            var isBlackSquare = isBlackSquareStartOfRow
            while (x < width) {
                val squareColor =
                    if (isBlackSquare) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f)
                drawRect(
                    color = squareColor,
                    topLeft = Offset(x, y),
                    size = Size(squareSize, squareSize),
                )
                isBlackSquare = !isBlackSquare
                x += squareSize
            }
            isBlackSquareStartOfRow = !isBlackSquareStartOfRow
            y += squareSize
        }

        // Draw the transparent to opaque gradient on top of the checkerboard
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, activeColor.copy(alpha = 1f)),
            ),
            size = size,
        )

        // Indicator (Vertical Line)
        val selectorX = alpha * size.width

        // White vertical line
        drawRect(
            color = Color.White,
            topLeft = Offset(selectorX - 4f, 0f),
            size = Size(8f, size.height),
        )

        // Black outline for the line (to make it visible on light colors)
        drawRect(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(selectorX - 4f, 0f),
            size = Size(8f, size.height),
            style = Stroke(width = 1f),
        )
    }
}
