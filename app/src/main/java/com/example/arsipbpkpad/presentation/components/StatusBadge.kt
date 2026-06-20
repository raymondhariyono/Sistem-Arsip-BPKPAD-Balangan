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
    status: String,
    modifier: Modifier = Modifier
) {
    val isExpired = status.equals("Expired", ignoreCase = true)
    val backgroundColor = if (isExpired) ErrorRed else DarkGreen
    val textColor = White

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
