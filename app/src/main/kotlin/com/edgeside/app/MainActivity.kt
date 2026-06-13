package com.edgeside.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.edgeside.app.service.EdgeSideService
import com.edgeside.app.ui.nav.RootNavGraph
import com.edgeside.app.ui.theme.EdgeSideTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EdgeSideTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootNavGraph(navController = rememberNavController())
                }
            }
        }
    }

    /** ä¾ UI å±è°ç¨çç»ä¸å¥å£ï¼é¿åå ViewModel åéå¤æ ·æ¿ã */
    fun startOverlayService() {
        val intent = Intent(this, EdgeSideService::class.java).apply {
            action = EdgeSideService.ACTION_SHOW
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun stopOverlayService() {
        // ä¼åç¨ ACTION_HIDE è½¯éèï¼service ä»å¨ï¼ç§çº§æ¢å¤ï¼ï¼stopService çç»"å®å¨éåº"è·¯å¾
        val intent = Intent(this, EdgeSideService::class.java).apply {
            action = EdgeSideService.ACTION_HIDE
        }
        startService(intent)
    }
}

@Composable
private fun ActivityAccess() = Unit  // å ä½ï¼æªæ¥å¯è½å  Activity-scoped CompositionLocalï¼
