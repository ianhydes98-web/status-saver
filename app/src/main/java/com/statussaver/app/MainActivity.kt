package com.statussaver.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.statussaver.app.service.AutoSaveService
import com.statussaver.app.ui.navigation.AppNavigation
import com.statussaver.app.ui.theme.StatusSaverTheme
import com.statussaver.app.util.PermissionUtils
import com.statussaver.app.util.Prefs

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Restart auto-save service if the user had previously enabled it
        if (Prefs.isAutoSaveEnabled(this)) {
            AutoSaveService.start(this)
        }

        setContent {
            StatusSaverTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        onRequestPermission = {
                            PermissionUtils.requestStoragePermission(this)
                        }
                    )
                }
            }
        }
    }
}
