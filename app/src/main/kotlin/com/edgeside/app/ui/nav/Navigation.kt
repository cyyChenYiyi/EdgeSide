package com.edgeside.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.edgeside.app.ui.apps.AppsPickScreen
import com.edgeside.app.ui.home.HomeScreen
import com.edgeside.app.ui.permission.PermissionGuideScreen
import com.edgeside.app.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val PERMISSION = "permission"
    const val APPS_PICK = "apps_pick"
    const val SETTINGS = "settings"
}

@Composable
fun RootNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onRequestPermission = { navController.navigate(Routes.PERMISSION) },
                onPickApps = { navController.navigate(Routes.APPS_PICK) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.PERMISSION) {
            PermissionGuideScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.APPS_PICK) {
            AppsPickScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
