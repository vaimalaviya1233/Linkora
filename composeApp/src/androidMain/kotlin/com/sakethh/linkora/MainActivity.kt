package com.sakethh.linkora

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.rememberNavController
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.utils.isTablet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val localConfiguration = LocalConfiguration.current
            val navController = rememberNavController()
            LinkoraTheme(typography = AndroidTypography) {
                Surface {
                    App(
                        platform = if (isTablet(localConfiguration) || localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.Android.Tablet else Platform.Android.Mobile,
                        navController = navController,
                        shouldFollowSystemThemeComposableBeVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    )
                }
            }
        }
    }
}