package com.edgeside.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            EdgeSideTheme {
                Surface(color = MaterialTheme.colors.background) {
                    EdgeSideApp(this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun EdgeSideTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val primary = Color(0xFF1976D2)
    val colors = if (darkTheme) {
        darkColors(primary = primary, primaryVariant = Color(0xFF0D47A1), secondary = Color(0xFF42A5F5))
    } else {
        lightColors(primary = primary, primaryVariant = Color(0xFF0D47A1), secondary = Color(0xFF42A5F5))
    }
    MaterialTheme(colors = colors, typography = MaterialTheme.typography, content = content)
}

@Composable
fun EdgeSideApp(activity: ComponentActivity) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                activity = activity,
                onNavigateToPickApps = { navController.navigate("pick") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("pick") { PickAppsScreen(activity = activity, onBack = { navController.popBackStack() }) }
        composable("settings") { SettingsScreen(activity = activity, onBack = { navController.popBackStack() }) }
    }
}
