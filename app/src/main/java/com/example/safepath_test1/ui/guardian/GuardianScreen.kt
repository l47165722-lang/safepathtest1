package com.example.safepath_test1.ui.guardian

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safepath_test1.location.shareLocation
import com.example.safepath_test1.model.GeoPoint
import com.example.safepath_test1.ui.components.PageHeader
import com.example.safepath_test1.ui.theme.AppBorder
import com.example.safepath_test1.ui.theme.DangerRed
import com.example.safepath_test1.ui.theme.FieldBg
import com.example.safepath_test1.ui.theme.SafeBlue
import com.example.safepath_test1.ui.theme.SafeGreen
import com.example.safepath_test1.ui.theme.TextMuted

@Composable
fun GuardianScreen(
    currentLocation: GeoPoint?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        PageHeader("보호자", "위치와 안전 상태를 간편하게 공유하세요")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = SafeBlue.copy(alpha = 0.1f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("◇", color = SafeBlue, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("보호자 연결", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "서비스 연동 준비 중",
                            color = TextMuted,
                            fontSize = 13.sp,
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = FieldBg,
                    ) {
                        Text(
                            "미연동",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                HorizontalDivider(color = AppBorder)
                Text(
                    "현재 위치는 원하는 앱으로 바로 공유할 수 있습니다.",
                    color = SafeGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Button(
            onClick = { shareLocation(context, currentLocation, isEmergency = false) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SafeBlue),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text("현재 위치 공유", style = MaterialTheme.typography.labelLarge)
        }
        Text("긴급 공유", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { shareLocation(context, currentLocation, isEmergency = true) },
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = DangerRed,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("!", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("긴급 알림 보내기", fontWeight = FontWeight.SemiBold)
                    Text(
                        "위치와 긴급 메시지를 함께 공유합니다.",
                        color = TextMuted,
                        fontSize = 12.sp,
                    )
                }
                Text("›", color = DangerRed, fontSize = 24.sp)
            }
        }
    }
}
