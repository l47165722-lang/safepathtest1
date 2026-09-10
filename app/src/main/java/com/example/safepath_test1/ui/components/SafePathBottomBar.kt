package com.example.safepath_test1.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safepath_test1.ui.SafePathTab
import com.example.safepath_test1.ui.theme.SafeBlue
import com.example.safepath_test1.ui.theme.TextMuted
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val GlassShape = RoundedCornerShape(28.dp)
private val DropletShape = RoundedCornerShape(50)

@Composable
fun SafePathBottomBar(
    selectedTab: SafePathTab,
    onTabSelected: (SafePathTab) -> Unit,
    modifier: Modifier = Modifier,
    opaqueBackground: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (opaqueBackground) {
                    Modifier.background(Color.White.copy(alpha = 0.92f))
                } else {
                    Modifier
                },
            )
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp),
    ) {
        LiquidGlassTabBar(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
        )
    }
}

@Composable
private fun LiquidGlassTabBar(
    selectedTab: SafePathTab,
    onTabSelected: (SafePathTab) -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val tabs = SafePathTab.entries

    var tabOffsetsPx by remember { mutableStateOf(FloatArray(tabs.size)) }
    var tabWidthsPx by remember { mutableStateOf(FloatArray(tabs.size)) }
    var layoutReady by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var pressedIndex by remember { mutableIntStateOf(selectedTab.ordinal) }
    var trackFraction by remember { mutableFloatStateOf(selectedTab.ordinal.toFloat()) }

    val indicatorX = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragWidth by remember { mutableFloatStateOf(0f) }

    val displayX = if (isDragging) dragX else indicatorX.value
    val displayWidth = if (isDragging) dragWidth else indicatorWidth.value

    fun layoutForFraction(fraction: Float): Pair<Float, Float> {
        if (tabs.size == 1) {
            return tabOffsetsPx[0] to tabWidthsPx[0]
        }
        val clamped = fraction.coerceIn(0f, tabs.lastIndex.toFloat())
        val left = clamped.toInt().coerceIn(0, tabs.lastIndex - 1)
        val right = (left + 1).coerceAtMost(tabs.lastIndex)
        val t = (clamped - left).coerceIn(0f, 1f)
        val x = lerp(tabOffsetsPx[left], tabOffsetsPx[right], t)
        val baseW = lerp(tabWidthsPx[left], tabWidthsPx[right], t)
        val stretch = 1f + 0.22f * (1f - abs(t - 0.5f) * 2f)
        return x to (baseW * stretch)
    }

    fun fractionForTouchX(touchX: Float): Float {
        if (tabs.size == 1) return 0f
        val centers = FloatArray(tabs.size) { index ->
            tabOffsetsPx[index] + tabWidthsPx[index] / 2f
        }
        if (touchX <= centers.first()) return 0f
        if (touchX >= centers.last()) return tabs.lastIndex.toFloat()
        for (index in 0 until tabs.lastIndex) {
            val start = centers[index]
            val end = centers[index + 1]
            if (touchX in start..end) {
                return index + ((touchX - start) / (end - start))
            }
        }
        return selectedTab.ordinal.toFloat()
    }

    fun nearestTab(fraction: Float): SafePathTab {
        return tabs[fraction.roundToInt().coerceIn(0, tabs.lastIndex)]
    }

    LaunchedEffect(selectedTab, layoutReady, isDragging) {
        if (!layoutReady || isDragging) return@LaunchedEffect
        val index = selectedTab.ordinal
        val targetX = tabOffsetsPx[index]
        val targetW = tabWidthsPx[index]
        if (targetW <= 0f) return@LaunchedEffect

        trackFraction = index.toFloat()
        pressedIndex = index

        val travel = abs(targetX - indicatorX.value)
        val stretchWidth = max(targetW, targetW + travel * 0.28f)

        if (indicatorWidth.value == 0f) {
            indicatorX.snapTo(targetX)
            indicatorWidth.snapTo(targetW)
            return@LaunchedEffect
        }

        launch {
            indicatorX.animateTo(
                targetValue = targetX,
                animationSpec = spring(
                    dampingRatio = 0.72f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
        launch {
            indicatorWidth.animateTo(
                targetValue = stretchWidth,
                animationSpec = spring(
                    dampingRatio = 0.86f,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
            indicatorWidth.animateTo(
                targetValue = targetW,
                animationSpec = spring(
                    dampingRatio = 0.68f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                elevation = 18.dp,
                shape = GlassShape,
                ambientColor = Color.Black.copy(alpha = 0.10f),
                spotColor = Color.Black.copy(alpha = 0.18f),
            )
            .clip(GlassShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.78f),
                        Color.White.copy(alpha = 0.42f),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.28f),
                    ),
                ),
                shape = GlassShape,
            )
            .padding(4.dp)
            .pointerInput(layoutReady) {
                if (!layoutReady) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    scope.launch { indicatorX.stop(); indicatorWidth.stop() }

                    fun followTouch(touchX: Float) {
                        val fraction = fractionForTouchX(touchX)
                        trackFraction = fraction
                        pressedIndex = nearestTab(fraction).ordinal
                        val (x, w) = layoutForFraction(fraction)
                        dragX = x
                        dragWidth = w
                    }

                    followTouch(down.position.x)
                    isDragging = true
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        followTouch(change.position.x)
                        change.consume()
                    }

                    val target = nearestTab(trackFraction)
                    val settleX = dragX
                    val settleW = dragWidth
                    isDragging = false
                    scope.launch {
                        indicatorX.snapTo(settleX)
                        indicatorWidth.snapTo(settleW)
                        onTabSelected(target)
                    }
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.55f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        if (layoutReady && displayWidth > 0f) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(displayX.roundToInt(), 0) }
                    .width(with(density) { displayWidth.toDp() })
                    .fillMaxHeight()
                    .shadow(
                        elevation = 8.dp,
                        shape = DropletShape,
                        ambientColor = SafeBlue.copy(alpha = 0.25f),
                        spotColor = SafeBlue.copy(alpha = 0.45f),
                    )
                    .clip(DropletShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3B82F6),
                                SafeBlue,
                                Color(0xFF1D4ED8),
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(200f, 120f),
                        ),
                    ),
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val highlighted = pressedIndex == tab.ordinal
                val contentColor by animateColorAsState(
                    targetValue = if (highlighted) Color.White else TextMuted,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "tabContentColor",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .onGloballyPositioned { coordinates ->
                            val index = tab.ordinal
                            val nextOffsets = tabOffsetsPx.copyOf()
                            val nextWidths = tabWidthsPx.copyOf()
                            nextOffsets[index] = coordinates.positionInParent().x
                            nextWidths[index] = coordinates.size.width.toFloat()
                            if (!nextOffsets.contentEquals(tabOffsetsPx) ||
                                !nextWidths.contentEquals(tabWidthsPx)
                            ) {
                                tabOffsetsPx = nextOffsets
                                tabWidthsPx = nextWidths
                            }
                            if (nextWidths.all { it > 0f }) {
                                layoutReady = true
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        Text(
                            text = tab.icon,
                            fontSize = 16.sp,
                            color = contentColor,
                        )
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                        )
                    }
                }
            }
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
