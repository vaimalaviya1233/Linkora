package com.sakethh.linkora

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalConfiguration
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.utils.isTablet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val localConfiguration = LocalConfiguration.current
            LinkoraTheme(typography = AndroidTypography) {
                Surface {
                    App(platform = if (isTablet(localConfiguration) || localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.AndroidTablet else Platform.AndroidMobile)
                }
            }
        }
    }
}