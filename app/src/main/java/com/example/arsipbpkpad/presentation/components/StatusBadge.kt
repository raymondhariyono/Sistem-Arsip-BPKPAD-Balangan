package com.example.arsipbpkpad.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arsipbpkpad.ui.theme.DarkGreen
import com.example.arsipbpkpad.ui.theme.ErrorRed
import com.example.arsipbpkpad.ui.theme.White

@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RetentionStatusBadge(isExpired: Boolean) {
    val backgroundColor = if (isExpired) ErrorRed else DarkGreen
    val text = if (isExpired) "EXPIRED" else "AKTIF"
    StatusBadge(text = text, backgroundColor = backgroundColor, textColor = White)
}

@Composable
fun ConditionBadge(condition: String) {
    val backgroundColor = when (condition.uppercase()) {
        "GOOD" -> DarkGreen
        "DAMAGED" -> Color(0xFFFFA000) // Amber
        "LOST" -> ErrorRed
        else -> MaterialTheme.colorScheme.secondary
    }
    StatusBadge(text = condition, backgroundColor = backgroundColor, textColor = White)
}

@Composable
fun DocStatusBadge(status: String) {
    val backgroundColor = when (status.uppercase()) {
        "AVAILABLE" -> DarkGreen
        "BORROWED" -> Color(0xFF1976D2) // Blue
        "DISPOSED" -> Color(0xFF757575) // Grey
        "UNVERIFIED" -> Color(0xFFFFA000) // Amber
        else -> MaterialTheme.colorScheme.secondary
    }
    StatusBadge(text = status, backgroundColor = backgroundColor, textColor = White)
}
