package com.sakethh.linkora.ui.screens.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.CoilImage
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.utils.pulsateEffect

@Composable
fun OnboardingIntroScreen() {
    val navController = LocalNavController.current
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(15.dp).fillMaxWidth().align(Alignment.BottomCenter)) {
            CoilImage(
                modifier = Modifier.clip(RoundedCornerShape(15.dp))
                    .sizeIn(maxWidth = 100.dp, maxHeight = 100.dp).wrapContentSize().border(
                        shape = RoundedCornerShape(15.dp),
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer,
                            )
                        )
                    ), imgURL = "https://avatars.githubusercontent.com/u/183308434", userAgent = ""
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Linkora keeps your links private.\nSync and organize—nothing leaves your device unless you set up your own server.\nNo tracking, no cloud.",
                style = MaterialTheme.typography.titleSmall,
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(15.dp))
            FilledTonalButton(modifier = Modifier.pulsateEffect().fillMaxWidth(), onClick = {

            }) {
                Text(
                    text = "Connect to Sync Server", style = MaterialTheme.typography.titleMedium
                )
            }
            Button(modifier = Modifier.pulsateEffect().fillMaxWidth(), onClick = {
                navController.navigate(Navigation.OnBoarding.SlidesScreen)
            }) {
                Text(
                    text = "Use Locally",
                    lineHeight = 16.sp,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}