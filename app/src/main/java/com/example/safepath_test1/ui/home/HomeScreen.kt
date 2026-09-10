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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    Safe("안전 경로", "🛡"),
    Shortest("최단 경로", "⇄"),
    Recommended("추천 경로", "★"),
}

@Composable
fun HomeScreen(
    currentLocation: GeoPoint?,
    hasLocationPermission: Boolean,
    origin: String,
    destination: String,
    onOriginChanged: (String) -> Unit,
    onDestinationChanged: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var routeType by rememberSaveable { mutableStateOf(RouteType.Safe.name) }
    var recenterToken by remember { mutableIntStateOf(0) }

    Box(modifier = modifier.fillMaxSize()) {
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
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp),
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
            onOpenSettings = onOpenSettings,
        )

        MapSideControls(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            enabled = hasLocationPermission,
            onRecenter = { recenterToken++ },
        )

        RouteSummaryPanel(
            routeType = RouteType.valueOf(routeType),
            hasDestination = destination.isNotBlank(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 88.dp),
        )
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
    onOpenSettings: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SafeBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("S", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                HorizontalGap(8.dp)
                Text(
                    text = "Safe Path",
                    modifier = Modifier.weight(1f),
                    color = TextMain,
                    style = MaterialTheme.typography.titleMedium,
                )
                Surface(
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(onClick = onOpenSettings),
                    shape = CircleShape,
                    color = FieldBg,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⚙", fontSize = 15.sp, color = TextMuted)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RouteFieldRow(
                    leading = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
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
                        .padding(horizontal = 6.dp)
                        .size(32.dp)
                        .clickable(onClick = onSwap),
                    shape = CircleShape,
                    color = FieldBg,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⇄", color = SafeBlue, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                RouteFieldRow(
                    leading = {
                        Text("📍", fontSize = 12.sp, color = DestRed)
                    },
                    value = destination,
                    placeholder = "도착지",
                    onValueChange = onDestinationChanged,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            .clip(RoundedCornerShape(10.dp))
            .background(FieldBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            leading()
        }
        HorizontalGap(6.dp)
        Box(modifier = Modifier.weight(1f)) {
            if (value.isBlank()) {
                Text(
                    text = placeholder,
                    color = TextMuted,
                    fontSize = 13.sp,
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
                    fontSize = 13.sp,
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
    val background = if (selected) SafeBlue else Color.White
    val content = if (selected) Color.White else TextMuted
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(type.icon, fontSize = 12.sp, color = content)
        HorizontalGap(4.dp)
        Text(
            text = type.title,
            color = content,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MapSideControls(
    modifier: Modifier,
    enabled: Boolean,
    onRecenter: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RoundMapButton(label = "◎", enabled = enabled, onClick = onRecenter)
        RoundMapButton(
            label = "▦",
            enabled = true,
            onClick = {
                Toast.makeText(context, "지도 레이어는 준비 중입니다.", Toast.LENGTH_SHORT).show()
            },
        )
        RoundMapButton(
            label = "➤",
            enabled = true,
            onClick = {
                Toast.makeText(context, "경로 안내는 서버 연결 후 제공됩니다.", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
private fun RoundMapButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .shadow(6.dp, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = Color.White,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (enabled) SafeBlue else Color.Gray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun RouteSummaryPanel(
    routeType: RouteType,
    hasDestination: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val summaryTitle = when {
        !hasDestination -> "도착지를 입력해 주세요"
        routeType == RouteType.Safe -> "안전 경로 약 28분"
        routeType == RouteType.Shortest -> "최단 경로 약 20분"
        else -> "추천 경로 약 25분"
    }
    val distance = when {
        !hasDestination -> ""
        routeType == RouteType.Safe -> "(2.1 km)"
        routeType == RouteType.Shortest -> "(1.6 km)"
        else -> "(1.9 km)"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = summaryTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                )
                if (distance.isNotEmpty()) {
                    HorizontalGap(6.dp)
                    Text(
                        text = distance,
                        fontSize = 13.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            Text(
                text = if (hasDestination) {
                    "CCTV 15개  |  지킴이집 3개  |  비상벨 2개  |  경찰서 1개"
                } else {
                    "목적지를 입력하면 안전시설 정보가 표시됩니다"
                },
                color = TextMuted,
                fontSize = 11.sp,
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val message = if (!hasDestination) {
                            "도착지를 입력해 주세요."
                        } else {
                            "경로 안내는 서버 연결 후 제공됩니다."
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(12.dp),
                color = SafeBlue,
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "이 경로로 시작하기",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    HorizontalGap(4.dp)
                    Text("›", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HorizontalGap(width: androidx.compose.ui.unit.Dp) {
    Spacer(modifier = Modifier.width(width))
}
