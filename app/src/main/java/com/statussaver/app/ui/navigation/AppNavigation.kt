package com.statussaver.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.statussaver.app.data.model.StatusMedia
import com.statussaver.app.ui.screens.grid.GridScreen
import com.statussaver.app.ui.screens.home.HomeScreen
import com.statussaver.app.ui.screens.permission.PermissionScreen
import com.statussaver.app.ui.screens.preview.PreviewScreen
import com.statussaver.app.ui.screens.settings.SettingsScreen
import com.statussaver.app.util.PermissionUtils
import androidx.compose.ui.platform.LocalContext

object Routes {
    const val PERMISSION = "permission"
    const val HOME = "home"
    const val GRID_IMAGES = "grid/images"
    const val GRID_VIDEOS = "grid/videos"
    const val PREVIEW = "preview"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(onRequestPermission: () -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // We use a state variable to force recomposition of the NavHost start destination
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasStoragePermission(context)) }
    
    // Check permission status whenever the AppNavigation recomposes
    hasPermission = PermissionUtils.hasStoragePermission(context)
    
    val startRoute = if (hasPermission) Routes.HOME else Routes.PERMISSION

    NavHost(navController = navController, startDestination = startRoute) {

        composable(Routes.PERMISSION) {
            PermissionScreen(
                onGrant = onRequestPermission,
                onExit = {
                    (context as? android.app.Activity)?.finish()
                },
                onPermissionGranted = {
                    // Navigate to Home and clear the back stack
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOpenImages = { navController.navigate(Routes.GRID_IMAGES) },
                onOpenVideos = { navController.navigate(Routes.GRID_VIDEOS) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(
            route = "${Routes.GRID_IMAGES}?type={type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType; defaultValue = "images" })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "images"
            GridScreen(
                mediaType = type,
                onBack = { navController.popBackStack() },
                onOpenPreview = { media ->
                    PendingMedia.current = media
                    navController.navigate(Routes.PREVIEW)
                }
            )
        }

        composable(Routes.GRID_VIDEOS) {
            GridScreen(
                mediaType = "videos",
                onBack = { navController.popBackStack() },
                onOpenPreview = { media ->
                    PendingMedia.current = media
                    navController.navigate(Routes.PREVIEW)
                }
            )
        }

        composable(Routes.PREVIEW) {
            val media = PendingMedia.current
            if (media != null) {
                PreviewScreen(
                    media = media,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

object PendingMedia {
    var current: StatusMedia? = null
}
