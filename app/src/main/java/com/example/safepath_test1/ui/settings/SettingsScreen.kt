package com.example.safepath_test1.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safepath_test1.ui.components.PageHeader
import com.example.safepath_test1.ui.theme.AppBorder
import com.example.safepath_test1.ui.theme.TextMuted

@Composable
fun SettingsScreen(
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences("safe_path_settings", Context.MODE_PRIVATE)
    }
    var shareLocation by remember { mutableStateOf(preferences.getBoolean("share_location", true)) }
    var safetyAlerts by remember { mutableStateOf(preferences.getBoolean("safety_alerts", true)) }
    var avoidRiskAreas by remember { mutableStateOf(preferences.getBoolean("avoid_risk_areas", true)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        PageHeader("설정", "안전 기능과 위치 권한을 관리하세요")
        SettingsGroup(
            rows = listOf(
                Triple("실시간 위치 공유", shareLocation) {
                    shareLocation = it
                    preferences.edit().putBoolean("share_location", it).apply()
                },
                Triple("주변 위험 알림", safetyAlerts) {
                    safetyAlerts = it
                    preferences.edit().putBoolean("safety_alerts", it).apply()
                },
                Triple("위험지역 자동 회피", avoidRiskAreas) {
                    avoidRiskAreas = it
                    preferences.edit().putBoolean("avoid_risk_areas", it).apply()
                },
            ),
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column {
                SettingsLinkRow(
                    title = "위치 권한",
                    value = if (hasLocationPermission) "허용됨" else "허용 필요",
                ) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        },
                    )
                }
                HorizontalDivider(
                    Modifier.padding(horizontal = 16.dp),
                    color = AppBorder,
                )
                SettingsLinkRow("개인정보 처리방침", "준비 중")
                HorizontalDivider(
                    Modifier.padding(horizontal = 16.dp),
                    color = AppBorder,
                )
                SettingsLinkRow("앱 버전", "1.0.0")
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    rows: List<Triple<String, Boolean, (Boolean) -> Unit>>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        row.first,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Switch(checked = row.second, onCheckedChange = row.third)
                }
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        Modifier.padding(start = 18.dp),
                        color = AppBorder,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsLinkRow(
    title: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(value, color = TextMuted, fontSize = 13.sp)
        if (onClick != null) {
            Text("  ›", color = TextMuted, fontSize = 20.sp)
        }
    }
}
