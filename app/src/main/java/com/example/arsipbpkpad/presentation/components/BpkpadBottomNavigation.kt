package com.example.arsipbpkpad.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.arsipbpkpad.R

enum class BottomNavItem(val route: String) {
    HOME("home"),
    ARCHIVE("archive"),
    ADD("add"),
    PROFILE("profile")
}

@Composable
fun BpkpadBottomNavigation(
    currentRoute: String?,
    onNavigate: (BottomNavItem) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            selected = currentRoute == BottomNavItem.HOME.route,
            onClick = { onNavigate(BottomNavItem.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_home)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == BottomNavItem.ARCHIVE.route,
            onClick = { onNavigate(BottomNavItem.ARCHIVE) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_archive)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == BottomNavItem.ADD.route,
            onClick = { onNavigate(BottomNavItem.ADD) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_add)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
        NavigationBarItem(
            selected = currentRoute == BottomNavItem.PROFILE.route,
            onClick = { onNavigate(BottomNavItem.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_profile)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
    }
}
