package com.example.arsipbpkpad.presentation.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.presentation.home.screen.RecentArchive
import com.example.arsipbpkpad.ui.theme.ChipBlue
import com.example.arsipbpkpad.ui.theme.ChipBlueBg
import com.example.arsipbpkpad.ui.theme.SuccessGreen
import com.example.arsipbpkpad.ui.theme.TextDark
import com.example.arsipbpkpad.ui.theme.TextPrimary
import com.example.arsipbpkpad.ui.theme.TextSecondary
import com.example.arsipbpkpad.ui.theme.TextTertiary

@Composable
fun HeaderSection() {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            text = stringResource(R.string.overview),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
fun PrimaryStatCard(
    title: String,
    count: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = count,
                color = contentColor,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SecondaryStatCard(
    modifier: Modifier = Modifier,
    count: String,
    label: String,
    icon: ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        TextButton(onClick = onActionClick, contentPadding = PaddingValues(0.dp)) {
            Text(
                text = actionText,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RecentArchiveItem(item: RecentArchive, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item.id) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.id,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            modifier = Modifier.weight(0.25f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            modifier = Modifier
                .weight(0.45f)
                .padding(horizontal = 8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = item.type,
                style = MaterialTheme.typography.labelSmall,
                color = ChipBlue,
                modifier = Modifier
                    .background(ChipBlueBg, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Row(
            modifier = Modifier.weight(0.15f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            val statusColor = if (item.isAvailable) SuccessGreen else MaterialTheme.colorScheme.error
            val statusText =
                if (item.isAvailable) stringResource(R.string.status_gudang) else stringResource(R.string.status_keluar)

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }

    @Composable
    fun RecentArchiveTable(items: List<RecentArchive>) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
            elevation = CardDefaults.cardElevation(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Header Tabel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE3F2FD)) // Biru muda yang sangat soft
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.header_id), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF616161), modifier = Modifier.weight(0.2f))
                    Text(text = stringResource(R.string.header_title), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF616161), modifier = Modifier.weight(0.4f))
                    Text(text = stringResource(R.string.header_type), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF616161), modifier = Modifier.weight(0.15f))
                    Text(text = stringResource(R.string.header_status), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF616161), modifier = Modifier.weight(0.25f))
                }

                // Baris Data Tabel
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.id, fontSize = 11.sp, color = Color(0xFF757575), modifier = Modifier.weight(0.2f), maxLines = 3, overflow = TextOverflow.Ellipsis)
                        Text(text = item.title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121), modifier = Modifier.weight(0.4f).padding(end = 8.dp), maxLines = 3, overflow = TextOverflow.Ellipsis)

                        Box(modifier = Modifier.weight(0.15f)) {
                            Text(
                                text = item.type,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0),
                                modifier = Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Row(modifier = Modifier.weight(0.25f), verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = if (item.isAvailable) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                            val statusText = if (item.isAvailable) "di Gudang" else "Perlu Verifikasi"

                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = statusText, fontSize = 10.sp, color = Color(0xFF616161))
                        }
                    }
                    if (index < items.size - 1) {
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
                    }
                }
            }
        }
    }
}
