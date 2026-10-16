package com.statussaver.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.statussaver.app.data.model.StatusMedia
import com.statussaver.app.ui.screens.home.HomeScreen
import com.statussaver.app.ui.screens.preview.PreviewScreen
import com.statussaver.app.ui.screens.settings.SettingsScreen
import java.io.File

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Preview : Screen("preview/{path}/{isVideo}") {
        fun createRoute(media: StatusMedia) =
            "preview/${media.path.replace("/", "___")}/${media.isVideo}"
    }
    data object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    onRequestPermission: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onMediaClick = { media ->
                    navController.navigate(Screen.Preview.createRoute(media))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onRequestPermission = onRequestPermission
            )
        }

        composable(
            route = Screen.Preview.route,
            arguments = listOf(
                navArgument("path") { type = NavType.StringType },
                navArgument("isVideo") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val rawPath = backStackEntry.arguments?.getString("path")?.replace("___", "/") ?: return@composable
            val isVideo = backStackEntry.arguments?.getBoolean("isVideo") ?: false
            val file = File(rawPath)

            val media = remember(rawPath) {
                StatusMedia(
                    id = rawPath.hashCode().toString(),
                    file = file,
                    type = if (isVideo) StatusMedia.MediaType.VIDEO else StatusMedia.MediaType.IMAGE,
                    lastModified = file.lastModified(),
                    size = file.length()
                )
            }

            PreviewScreen(
                media = media,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
