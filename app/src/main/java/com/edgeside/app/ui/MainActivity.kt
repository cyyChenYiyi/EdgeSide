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
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.edgeside.app.ui.theme.IosBackground
import com.edgeside.app.ui.theme.IosBlue
import com.edgeside.app.ui.theme.IosBlueDark
import com.edgeside.app.ui.theme.IosCardBg
import com.edgeside.app.ui.theme.IosShapes
import com.edgeside.app.ui.theme.IosTypography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // iOS-style light status bar (dark icons on light background)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
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
    val colors = if (darkTheme) {
        darkColors(
            primary = IosBlue,
            primaryVariant = IosBlueDark,
            secondary = IosBlue,
            background = Color(0xFF000000),
            surface = Color(0xFF1C1C1E),
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColors(
            primary = IosBlue,
            primaryVariant = IosBlueDark,
            secondary = IosBlue,
            background = IosBackground,
            surface = IosCardBg,
            onPrimary = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black
        )
    }
    MaterialTheme(colors = colors, typography = IosTypography, shapes = IosShapes, content = content)
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
