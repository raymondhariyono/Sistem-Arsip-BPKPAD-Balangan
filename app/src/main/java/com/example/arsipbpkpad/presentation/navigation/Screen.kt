package com.example.arsipbpkpad.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ArchiveList : Screen("archive_list")
    data object ArchiveDetail : Screen("archive_detail/{archiveId}") {
        fun createRoute(archiveId: String) = "archive_detail/$archiveId"
    }
    data object ArchiveReview : Screen("archive_review")
    data object Scan : Screen("scan")
    data object BoxContext : Screen("box_context")
    data object RapidInput : Screen("rapid_input")
}
