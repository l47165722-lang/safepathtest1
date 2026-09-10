package com.example.safepath_test1

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SafePathApp() }
    }
}

private val SafeBlue = Color(0xFF1478E8)
private val SafeGreen = Color(0xFF20B769)
private val PanelBackground = Color(0xFFF8F9F7)

@Composable
private fun SafePathApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentLocation by remember { mutableStateOf<Point?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) {
            onDispose { }
        } else {
            val locationManager = context.getSystemService(LocationManager::class.java)
            val listener = LocationListener { location ->
                currentLocation = Point.fromLngLat(location.longitude, location.latitude)
            }
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

            try {
                providers
                    .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
                    .maxByOrNull { location -> location.time }
                    ?.let { location ->
                        currentLocation = Point.fromLngLat(location.longitude, location.latitude)
                    }
                providers.forEach { provider ->
                    if (locationManager.isProviderEnabled(provider)) {
                        locationManager.requestLocationUpdates(provider, 1_000L, 1f, listener)
                    }
                }
            } catch (_: SecurityException) {
                currentLocation = null
            }

            onDispose { locationManager.removeUpdates(listener) }
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            HomeScreen(
                currentLocation = currentLocation,
                hasLocationPermission = hasLocationPermission,
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                },
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )

            when (selectedTab) {
                1 -> GuardianScreen(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                2 -> SettingsScreen(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun HomeScreen(
    currentLocation: Point?,
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (currentLocation == null) {
            LocationLoadingScreen(
                hasLocationPermission = hasLocationPermission,
                onRequestPermission = onRequestPermission,
            )
        } else {
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(currentLocation)
                    zoom(16.0)
                    pitch(35.0)
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                SafePathMap(mapViewportState, hasLocationPermission)
                TopStatusCard(hasLocationPermission, onRequestPermission)
                RecenterButton(
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 18.dp),
                    enabled = hasLocationPermission,
                    onClick = { mapViewportState.transitionToFollowPuckState() },
                )
            }
        }
        FloatingDirectionsPanel(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            expandedHeight = maxHeight * 0.72f,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SafePathMap(
    mapViewportState: MapViewportState,
    hasLocationPermission: Boolean,
) {
    MapboxMap(
        modifier = Modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        compass = { },
        scaleBar = { },
        logo = {
            Logo(
                modifier = Modifier.padding(top = 102.dp, start = 8.dp),
                contentPadding = PaddingValues(0.dp),
                alignment = Alignment.TopStart,
            )
        },
        attribution = {
            Attribution(
                modifier = Modifier.padding(top = 102.dp, end = 8.dp),
                contentPadding = PaddingValues(0.dp),
                alignment = Alignment.TopEnd,
            )
        },
    ) {
        if (hasLocationPermission) {
            MapEffect(hasLocationPermission) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearingEnabled = true
                    puckBearing = PuckBearing.HEADING
                    enabled = true
                }
            }
        }
    }
}

@Composable
private fun TopStatusCard(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
) {
    Card(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF5FFFFFF)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SafePath",
                    color = SafeBlue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = if (hasLocationPermission) "● 현재 위치를 실시간 추적 중" else "위치 권한이 필요합니다",
                    color = if (hasLocationPermission) SafeGreen else Color(0xFFD35B48),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
            if (!hasLocationPermission) {
                Text(
                    text = "권한 허용",
                    modifier = Modifier
                        .clickable(onClick = onRequestPermission)
                        .padding(8.dp),
                    color = SafeBlue,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun RecenterButton(modifier: Modifier, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .size(52.dp)
            .shadow(8.dp, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = Color.White,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("➤", color = if (enabled) SafeBlue else Color.Gray, fontSize = 24.sp)
        }
    }
}

@Composable
private fun FloatingDirectionsPanel(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    expandedHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val collapsedHeight = 132.dp
    var targetHeight by remember(expandedHeight) { mutableStateOf(collapsedHeight) }
    val panelHeight by animateDpAsState(
        targetValue = targetHeight,
        label = "Safe route panel height",
    )
    val showRouteDetails = targetHeight > collapsedHeight + 56.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .height(panelHeight)
            .pointerInput(expandedHeight, density) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val dragAmountDp = with(density) { dragAmount.toDp() }
                        targetHeight = (targetHeight - dragAmountDp)
                            .coerceIn(collapsedHeight, expandedHeight)
                    },
                    onDragEnd = {
                        val middleHeight = collapsedHeight +
                            (expandedHeight - collapsedHeight) / 2f
                        targetHeight = if (targetHeight >= middleHeight) {
                            expandedHeight
                        } else {
                            collapsedHeight
                        }
                    },
                )
            },
        shape = RoundedCornerShape(28.dp),
        color = PanelBackground,
        shadowElevation = 16.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(210.dp)
                    .height(25.dp)
                    .clickable {
                        targetHeight = if (targetHeight == collapsedHeight) {
                            expandedHeight
                        } else {
                            collapsedHeight
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .background(Color(0xFFBFC4C1), CircleShape),
                )
            }
            if (showRouteDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("안전 경로", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                    TravelModeSelector()
                    RouteInputCard()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OptionChip("지금 출발⌄")
                        OptionChip("위험지역 회피⌄")
                    }
                    RouteSummaryCard()
                }
                HorizontalDivider(color = Color(0xFFE1E4E2))
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            SafePathBottomBar(selectedTab, onTabSelected)
        }
    }
}

@Composable
private fun TravelModeSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE9ECEA), RoundedCornerShape(22.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TravelMode("🚶", true)
        TravelMode("🚲", false)
        TravelMode("🚗", false)
        TravelMode("🚌", false)
    }
}

@Composable
private fun TravelMode(icon: String, selected: Boolean) {
    Surface(
        modifier = Modifier.size(width = 66.dp, height = 38.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Color.White else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) { Text(icon, fontSize = 19.sp) }
    }
}

@Composable
private fun RouteInputCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF1EF)),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            RouteInputRow("●", SafeBlue, "내 위치", "실시간 위치")
            HorizontalDivider(modifier = Modifier.padding(start = 28.dp), color = Color(0xFFD7DAD8))
            RouteInputRow("●", Color(0xFFEB6254), "목적지를 입력하세요", "검색")
        }
    }
}

@Composable
private fun RouteInputRow(marker: String, markerColor: Color, title: String, trailing: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(marker, color = markerColor, fontSize = 14.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium,
        )
        Text(trailing, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun OptionChip(text: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFE8EBE9)) {
        Text(text, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), fontSize = 13.sp)
    }
}

@Composable
private fun RouteSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("가장 안전한 경로", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("목적지를 선택하면 경로를 안내합니다", color = Color.Gray, fontSize = 12.sp)
            }
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("출발", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LocationLoadingScreen(
    hasLocationPermission: Boolean,
    onRequestPermission: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEAF2EE)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (hasLocationPermission) {
                CircularProgressIndicator(color = SafeBlue)
                Text("현재 위치를 확인하고 있습니다", fontWeight = FontWeight.Bold)
                Text(
                    "GPS가 켜져 있는지 확인해 주세요.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                Text("📍", fontSize = 42.sp)
                Text("내 위치를 표시하려면 위치 권한이 필요합니다", fontWeight = FontWeight.Bold)
                Button(onClick = onRequestPermission) { Text("위치 권한 허용") }
            }
        }
    }
}

@Composable
private fun GuardianScreen(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Scaffold(
        containerColor = Color(0xFFF5F7F6),
        bottomBar = { SafePathBottomBar(selectedTab, onTabSelected) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PageHeader("보호자", "내 위치와 안전 상태를 공유합니다")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = Color(0xFFE4F1FF)) {
                            Box(contentAlignment = Alignment.Center) { Text("👤", fontSize = 24.sp) }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("등록된 보호자", color = Color.Gray, fontSize = 12.sp)
                            Text("김보호", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                            Text("010-1234-5678", color = Color.Gray)
                        }
                        Text("연결됨", color = SafeGreen, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color(0xFFE8EAE9))
                    Text("● 실시간 위치 공유 중", color = SafeGreen, fontWeight = FontWeight.Medium)
                }
            }
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SafeBlue),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("보호자에게 현재 위치 보내기", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("+ 보호자 추가")
            }
            Text("긴급 기능", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECE9)),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("SOS", color = Color(0xFFD94B3D), fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("긴급 알림 보내기", fontWeight = FontWeight.Bold)
                        Text("보호자에게 위치와 알림을 전송합니다", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    var shareLocation by remember { mutableStateOf(true) }
    var safetyAlerts by remember { mutableStateOf(true) }
    var avoidRiskAreas by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color(0xFFF5F7F6),
        bottomBar = { SafePathBottomBar(selectedTab, onTabSelected) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PageHeader("설정", "SafePath 사용 환경을 관리합니다")
            SettingsGroup(
                title = "안전 및 위치",
                rows = listOf(
                    Triple("실시간 위치 공유", shareLocation) { shareLocation = it },
                    Triple("주변 위험 알림", safetyAlerts) { safetyAlerts = it },
                    Triple("위험지역 자동 회피", avoidRiskAreas) { avoidRiskAreas = it },
                ),
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column {
                    SettingsLinkRow("위치 권한", "허용됨")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color(0xFFE8EAE9))
                    SettingsLinkRow("개인정보 처리방침", "›")
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color(0xFFE8EAE9))
                    SettingsLinkRow("앱 버전", "1.0.0")
                }
            }
        }
    }
}

@Composable
private fun PageHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.statusBarsPadding(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, color = Color.Gray)
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    rows: List<Triple<String, Boolean, (Boolean) -> Unit>>,
) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(row.first, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Switch(checked = row.second, onCheckedChange = row.third)
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Color(0xFFE8EAE9))
                }
            }
        }
    }
}

@Composable
private fun SettingsLinkRow(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text(value, color = Color.Gray)
    }
}

@Composable
private fun SafePathBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val items = listOf("⌂" to "홈", "♟" to "보호자", "⚙" to "설정")
    NavigationBar(containerColor = PanelBackground, tonalElevation = 0.dp) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Text(item.first, fontSize = 20.sp) },
                label = { Text(item.second) },
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DirectionsPanelPreview() {
    Box(Modifier.fillMaxSize().background(Color(0xFFDDE8DF))) {
        FloatingDirectionsPanel(
            selectedTab = 0,
            onTabSelected = { },
            expandedHeight = 520.dp,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
