package com.example.arsipbpkpad.presentation.components

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.arsipbpkpad.R

enum class BottomNavItem(val route: String) {
    HOME("home"),
    ARCHIVE("archive"),
    ADD("add"),
    ANALYTICS("analytics")
}

@Composable
fun BpkpadBottomNavigation(
    currentRoute: String?,
    onNavigate: (BottomNavItem) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    NavigationBar(
        modifier = if (isLandscape) Modifier.height(64.dp) else Modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            selected = currentRoute == BottomNavItem.HOME.route,
            onClick = { onNavigate(BottomNavItem.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { if (!isLandscape) Text(stringResource(R.string.nav_home)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == BottomNavItem.ARCHIVE.route,
            onClick = { onNavigate(BottomNavItem.ARCHIVE) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { if (!isLandscape) Text(stringResource(R.string.nav_archive)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == BottomNavItem.ADD.route,
            onClick = { onNavigate(BottomNavItem.ADD) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { if (!isLandscape) Text(stringResource(R.string.nav_add)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == BottomNavItem.ANALYTICS.route,
            onClick = { onNavigate(BottomNavItem.ANALYTICS) },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { if (!isLandscape) Text(stringResource(R.string.nav_analytics)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
    }
}
