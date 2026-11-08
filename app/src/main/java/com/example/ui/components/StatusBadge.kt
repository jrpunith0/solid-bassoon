package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun TaskStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status.lowercase()) {
        "scheduled" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Color(0xFFFDE68A))
        "published" -> Triple(Color(0xFFD1FAE5), Color(0xFF047857), Color(0xFFA7F3D0))
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFFE2E8F0)) // Draft
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PlatformBadge(
    platform: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (platform.lowercase()) {
        "youtube" -> Pair(YouTubeRed.copy(alpha = 0.15f), YouTubeRed)
        "instagram" -> Pair(InstagramGradientPink.copy(alpha = 0.15f), InstagramGradientPink)
        "facebook" -> Pair(FacebookBlue.copy(alpha = 0.15f), FacebookBlue)
        else -> Pair(StudioPurple.copy(alpha = 0.15f), StudioPurple)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = platform,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProjectStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "idea" -> Pair(Color(0xFFFEF9C3), Color(0xFF854D0E)) // 🟡
        "script ready" -> Pair(Color(0xFFDBEAFE), Color(0xFF1E40AF)) // 🔵
        "production" -> Pair(Color(0xFFF3E8FF), Color(0xFF6B21A8)) // 🟣
        "thumbnail ready" -> Pair(Color(0xFFFFEDD5), Color(0xFF9A3412)) // 🟠
        "ready to publish" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D)) // 🟢
        "published" -> Pair(Color(0xFFD1FAE5), Color(0xFF047857)) // ✅
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
