package com.example.safepath_test1.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safepath_test1.model.GeoPoint
import com.example.safepath_test1.ui.map.SafePathMapboxView
import com.example.safepath_test1.ui.theme.DestRed
import com.example.safepath_test1.ui.theme.FieldBg
import com.example.safepath_test1.ui.theme.SafeBlue
import com.example.safepath_test1.ui.theme.TextMain
import com.example.safepath_test1.ui.theme.TextMuted

private enum class RouteType(val title: String, val icon: String) {
    Safe("안전", "🛡"),
    Shortest("최단", "⚡"),
    Recommended("추천", "★"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentLocation: GeoPoint?,
    hasLocationPermission: Boolean,
    origin: String,
    destination: String,
    onOriginChanged: (String) -> Unit,
    onDestinationChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var routeType by rememberSaveable { mutableStateOf(RouteType.Safe.name) }
    var recenterToken by remember { mutableIntStateOf(0) }
    var showSafetyFacilities by rememberSaveable { mutableStateOf(true) }
    val sheetState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        modifier = modifier.fillMaxSize(),
        scaffoldState = sheetState,
        sheetPeekHeight = 220.dp,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContainerColor = Color.White.copy(alpha = 0.96f),
        sheetShadowElevation = 14.dp,
        sheetContent = {
            RouteBottomSheet(
                destination = destination,
                routeType = RouteType.valueOf(routeType),
            )
        },
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            SafePathMapboxView(
                currentLocation = currentLocation,
                hasLocationPermission = hasLocationPermission,
                recenterToken = recenterToken,
                modifier = Modifier.fillMaxSize(),
            )

            RouteSearchCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                origin = origin,
                destination = destination,
                onOriginChanged = onOriginChanged,
                onDestinationChanged = onDestinationChanged,
                onSwap = {
                    val previousOrigin = origin
                    onOriginChanged(destination)
                    onDestinationChanged(previousOrigin)
                },
                selectedRouteType = RouteType.valueOf(routeType),
                onRouteTypeSelected = { routeType = it.name },
            )

            MapSideControls(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                locationEnabled = hasLocationPermission,
                safetyFacilitiesEnabled = showSafetyFacilities,
                onRecenter = { recenterToken++ },
                onToggleSafetyFacilities = {
                    showSafetyFacilities = !showSafetyFacilities
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteBottomSheet(
    destination: String,
    routeType: RouteType,
) {
    val context = LocalContext.current
    val hasDestination = destination.isNotBlank()
    val routeMinutes = when (routeType) {
        RouteType.Safe -> 24
        RouteType.Shortest -> 20
        RouteType.Recommended -> 22
    }
    val routeDistance = when (routeType) {
        RouteType.Safe -> "1.8 km"
        RouteType.Shortest -> "1.6 km"
        RouteType.Recommended -> "1.7 km"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (!hasDestination) {
            Text(
                text = "목적지를 설정해 주세요",
                style = MaterialTheme.typography.titleLarge,
                color = TextMain,
            )
            Text(
                text = "목적지를 입력하면 안전도와 주변 안전시설을 확인할 수 있어요.",
                color = TextMuted,
                fontSize = 12.sp,
            )
            PrimaryAction(
                text = "목적지 검색",
                onClick = {
                    Toast.makeText(context, "상단 도착지 입력창을 이용해 주세요.", Toast.LENGTH_SHORT).show()
                },
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = destination,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${routeType.icon} ${routeType.title} 경로 · ${routeMinutes}분 · $routeDistance",
                        color = TextMuted,
                        fontSize = 13.sp,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEAF8F1),
                ) {
                    Text(
                        text = "매우 안전",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color(0xFF15803D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("안전도", color = TextMain, fontWeight = FontWeight.SemiBold)
                HorizontalGap(8.dp)
                LinearProgressIndicator(
                    progress = { 0.87f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color(0xFF22C55E),
                    trackColor = FieldBg,
                )
                HorizontalGap(8.dp)
                Text("87", color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
            }

            Text(
                text = "안전시설을 많이 지나고, 밝은 도로를 우선하는 경로예요.",
                color = TextMuted,
                fontSize = 12.sp,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SafetyMetric("📷", "CCTV", "18개", Modifier.weight(1f))
                SafetyMetric("💡", "가로등", "34개", Modifier.weight(1f))
                SafetyMetric("📍", "경찰시설", "2개", Modifier.weight(1f))
            }

            PrimaryAction(
                text = "${routeType.title} 경로로 출발",
                onClick = {
                    Toast.makeText(context, "경로 안내는 서버 연결 후 제공됩니다.", Toast.LENGTH_SHORT).show()
                },
            )
        }

        // Keeps sheet actions clear of the floating app navigation.
        Spacer(modifier = Modifier.height(92.dp))
    }
}

@Composable
private fun SafetyMetric(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = FieldBg,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(icon, fontSize = 15.sp)
            Text(value, color = TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PrimaryAction(
    text: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = SafeBlue,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RouteSearchCard(
    modifier: Modifier = Modifier,
    origin: String,
    destination: String,
    onOriginChanged: (String) -> Unit,
    onDestinationChanged: (String) -> Unit,
    onSwap: () -> Unit,
    selectedRouteType: RouteType,
    onRouteTypeSelected: (RouteType) -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.96f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(SafeBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("S", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                HorizontalGap(7.dp)
                Text(
                    text = "SafePath",
                    color = TextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RouteFieldRow(
                    leading = {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(SafeBlue),
                        )
                    },
                    value = origin,
                    placeholder = "내 위치",
                    onValueChange = onOriginChanged,
                    modifier = Modifier.weight(1f),
                )

                Surface(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .size(28.dp)
                        .clickable(onClick = onSwap),
                    shape = CircleShape,
                    color = FieldBg,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⇄", color = SafeBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                RouteFieldRow(
                    leading = { Text("📍", fontSize = 11.sp, color = DestRed) },
                    value = destination,
                    placeholder = "도착지",
                    onValueChange = onDestinationChanged,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(FieldBg)
                    .padding(2.dp),
            ) {
                RouteType.entries.forEach { type ->
                    RouteTypeChip(
                        type = type,
                        selected = type == selectedRouteType,
                        onClick = { onRouteTypeSelected(type) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteFieldRow(
    leading: @Composable () -> Unit,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(FieldBg)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(14.dp),
            contentAlignment = Alignment.Center,
        ) {
            leading()
        }
        HorizontalGap(5.dp)
        Box(modifier = Modifier.weight(1f)) {
            if (value.isBlank()) {
                Text(
                    text = placeholder,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = TextMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                cursorBrush = SolidColor(SafeBlue),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RouteTypeChip(
    type: RouteType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = if (selected) SafeBlue else Color.Transparent
    val content = if (selected) Color.White else TextMuted
    Row(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(type.icon, fontSize = 11.sp, color = content)
        HorizontalGap(4.dp)
        Text(
            text = type.title,
            color = content,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MapSideControls(
    modifier: Modifier,
    locationEnabled: Boolean,
    safetyFacilitiesEnabled: Boolean,
    onRecenter: () -> Unit,
    onToggleSafetyFacilities: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RoundMapButton(
            label = "◎",
            enabled = locationEnabled,
            selected = false,
            onClick = onRecenter,
        )
        RoundMapButton(
            label = "🛡",
            enabled = true,
            selected = safetyFacilitiesEnabled,
            onClick = onToggleSafetyFacilities,
        )
    }
}

@Composable
private fun RoundMapButton(
    label: String,
    enabled: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(42.dp)
            .shadow(5.dp, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = if (selected) SafeBlue else Color.White,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = when {
                    selected -> Color.White
                    enabled -> SafeBlue
                    else -> Color.Gray
                },
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun HorizontalGap(width: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.width(width))
}
