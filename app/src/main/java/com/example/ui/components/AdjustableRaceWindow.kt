package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.obd.RaceWindowGeometry
import com.example.obd.RaceWindowId
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalRacePalette
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Touch-movable and touch-resizable window container for Race dashboard gauges.
 * - Drag anywhere inside the window body to reposition it on the Race board.
 * - Drag the bottom-right corner grip handle to resize width & height.
 */
@Composable
fun AdjustableRaceWindow(
    windowId: RaceWindowId,
    geometry: RaceWindowGeometry,
    boardWidthPx: Float,
    isLocked: Boolean,
    onBringToFront: (RaceWindowId) -> Unit,
    onMoveDelta: (RaceWindowId, Float, Float) -> Unit,
    onResizeDelta: (RaceWindowId, Float, Float) -> Unit,
    onCommitGesture: () -> Unit,
    content: @Composable () -> Unit
) {
    val palette = LocalRacePalette.current
    val density = LocalDensity.current
    var isDraggingMove by remember { mutableStateOf(false) }
    var isDraggingResize by remember { mutableStateOf(false) }
    val isActive = isDraggingMove || isDraggingResize

    val animX = remember { Animatable(geometry.xFraction) }
    val animY = remember { Animatable(geometry.yDp) }
    LaunchedEffect(geometry.xFraction, geometry.yDp, isActive) {
        if (isActive) {
            animX.snapTo(geometry.xFraction)
            animY.snapTo(geometry.yDp)
        } else {
            launch {
                animX.animateTo(
                    targetValue = geometry.xFraction,
                    animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMedium)
                )
            }
            launch {
                animY.animateTo(
                    targetValue = geometry.yDp,
                    animationSpec = spring(dampingRatio = 0.86f, stiffness = Spring.StiffnessMedium)
                )
            }
        }
    }

    val displayXFrac = if (isActive) geometry.xFraction else animX.value
    val displayYDp = if (isActive) geometry.yDp else animY.value
    val xPx = (displayXFrac * boardWidthPx).roundToInt()
    val yPx = with(density) { displayYDp.dp.toPx() }.roundToInt()
    val widthDp = with(density) { (geometry.widthFraction * boardWidthPx).toDp() }
    val heightDp = geometry.heightDp.dp

    val windowShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .offset { IntOffset(xPx, yPx) }
            .size(width = widthDp, height = heightDp)
            .zIndex(geometry.zOrder.toFloat() + if (isActive) 1000f else 0f)
            .clip(windowShape)
            .then(
                if (!isLocked) {
                    Modifier.pointerInput(windowId, isLocked, boardWidthPx) {
                        detectDragGestures(
                            onDragStart = {
                                isDraggingMove = true
                                onBringToFront(windowId)
                            },
                            onDragEnd = {
                                isDraggingMove = false
                                onCommitGesture()
                            },
                            onDragCancel = {
                                isDraggingMove = false
                                onCommitGesture()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onMoveDelta(windowId, dragAmount.x, dragAmount.y)
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .then(
                if (isActive) {
                    Modifier.border(
                        width = 2.dp,
                        color = if (isDraggingResize) palette.zone3 else palette.zone1,
                        shape = windowShape
                    )
                } else if (!isLocked) {
                    Modifier.border(
                        width = 1.dp,
                        color = palette.zone1.copy(alpha = 0.25f),
                        shape = windowShape
                    )
                } else {
                    Modifier
                }
            )
            .testTag("adjustable_window_${windowId.key}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }

        if (!isLocked) {
            if (isActive) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = palette.boardBg.copy(alpha = 0.88f),
                    border = BorderStroke(
                        1.dp,
                        if (isDraggingResize) palette.zone3 else palette.zone1
                    )
                ) {
                    Text(
                        text = "${widthDp.value.roundToInt()}×${heightDp.value.roundToInt()}dp",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = palette.textPrimary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // 48dp touch target corner resize grip in bottom-right corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(48.dp)
                    .pointerInput(windowId, isLocked, boardWidthPx) {
                        detectDragGestures(
                            onDragStart = {
                                isDraggingResize = true
                                onBringToFront(windowId)
                            },
                            onDragEnd = {
                                isDraggingResize = false
                                onCommitGesture()
                            },
                            onDragCancel = {
                                isDraggingResize = false
                                onCommitGesture()
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onResizeDelta(windowId, dragAmount.x, dragAmount.y)
                            }
                        )
                    }
                    .testTag("resize_handle_${windowId.key}"),
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomEnd = 10.dp))
                        .background(palette.boardBg.copy(alpha = 0.78f))
                        .padding(4.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gripColor = if (isDraggingResize) palette.zone3 else palette.zone1
                        drawLine(
                            color = gripColor,
                            start = Offset(size.width * 0.25f, size.height),
                            end = Offset(size.width, size.height * 0.25f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = gripColor,
                            start = Offset(size.width * 0.65f, size.height),
                            end = Offset(size.width, size.height * 0.65f),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }
}
