package com.example.arsipbpkpad.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.arsipbpkpad.R
import com.example.arsipbpkpad.domain.model.UserRole
import com.example.arsipbpkpad.domain.model.canManageStaging
import com.example.arsipbpkpad.domain.model.canManageStorage
import com.example.arsipbpkpad.domain.model.canMutateArchive
import com.example.arsipbpkpad.domain.model.canViewAnalytics

enum class BottomNavItem(val route: String) {
    HOME("home"),
    ARCHIVE("archive"),
    STORAGE("box_management"),
    ADD("add"),
    ANALYTICS("analytics")
}

@Composable
fun BpkpadBottomNavigation(
    currentRoute: String?,
    userRole: UserRole = UserRole.UNKNOWN,
    onNavigate: (BottomNavItem) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    val items = remember(userRole) {
        BottomNavItem.entries.filter { item ->
            when (item) {
                BottomNavItem.HOME -> userRole != UserRole.UNKNOWN
                BottomNavItem.ARCHIVE -> userRole != UserRole.UNKNOWN
                BottomNavItem.STORAGE -> userRole.canManageStorage()
                BottomNavItem.ADD -> userRole.canMutateArchive() || userRole.canManageStaging()
                BottomNavItem.ANALYTICS -> userRole.canViewAnalytics()
            }
        }
    }

    NavigationBar(
        modifier = if (isLandscape) Modifier.height(64.dp) else Modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val icon = when (item) {
                BottomNavItem.HOME -> Icons.Default.Home
                BottomNavItem.ARCHIVE -> Icons.AutoMirrored.Filled.List
                BottomNavItem.STORAGE -> Icons.Default.Inventory
                BottomNavItem.ADD -> Icons.Default.Add
                BottomNavItem.ANALYTICS -> Icons.Default.Info
            }
            
            val label = when (item) {
                BottomNavItem.HOME -> R.string.nav_home
                BottomNavItem.ARCHIVE -> R.string.nav_archive
                BottomNavItem.STORAGE -> R.string.status_warehouse
                BottomNavItem.ADD -> R.string.nav_add
                BottomNavItem.ANALYTICS -> R.string.nav_analytics
            }

            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item) },
                icon = { Icon(icon, contentDescription = null) },
                label = { if (!isLandscape) Text(stringResource(label)) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
            )
        }
    }
}
