package com.example.arsipbpkpad.presentation.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object ArchiveList : Screen("archive_list?year={year}") {
        fun createRoute(year: Int?) = if (year != null) "archive_list?year=$year" else "archive_list"
    }
    data object ArchiveDetail : Screen("archive_detail/{archiveId}") {
        fun createRoute(archiveId: String) = "archive_detail/$archiveId"
    }
    data object Scan : Screen("scan")
    data object StagingBoxList : Screen("staging_box_list")
    data object RapidInput : Screen("rapid_input/{sessionId}") {
        fun createRoute(sessionId: String) = "rapid_input/$sessionId"
    }
    data object EditArchive : Screen("edit_archive/{archiveId}") {
        fun createRoute(archiveId: String) = "edit_archive/$archiveId"
    }
    data object Analytics : Screen("analytics")
}
