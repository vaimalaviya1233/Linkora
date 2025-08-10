package com.sakethh.linkora.ui.screens.settings.section

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.Localization
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.utils.addEdgeToEdgeScaffoldPadding
import com.sakethh.linkora.utils.rememberLocalizedString
import com.sakethh.linkora.di.linkoraViewModel
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.CoilImage
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.linkora.platform.platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcknowledgementSettingsScreen() {
    val navController = LocalNavController.current
    val settingsScreenViewModel: SettingsScreenViewModel = linkoraViewModel()
    LocalUriHandler.current
    val platform = platform()
    SettingsSectionScaffold(
        topAppBarText = Navigation.Settings.AcknowledgementSettingsScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().addEdgeToEdgeScaffoldPadding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item {
                if (platform is Platform.Desktop) {
                    AcknowledgeComponent(
                        btnText = Localization.Key.MondsternOnDiscord.rememberLocalizedString(),
                        imgUrl = "https://pxscdn.com/public/m/_v2/13678/19c3981d1-f52fd9/vbCgbTLnHFSO/tHhaGE4jNdoGe4TiJAS1zUhhKrJBj5R9QXdpdale.jpg",
                        btnRedirectUrl = "https://pixelfed.social/mondstern",
                        text = Localization.Key.MondsternAck.rememberLocalizedString(),
                        imgModifier = Modifier.padding(start = 15.dp, top = 15.dp, bottom = 15.dp)
                            .clip(RoundedCornerShape(15.dp)).size(65.dp)
                    )
                }
                if (platform !is Platform.Android.Mobile) {
                    AcknowledgeComponent(
                        btnText = Localization.Key.LOLCATplOnDiscord.rememberLocalizedString(),
                        imgUrl = "https://i.ibb.co/ZK4yGwQ/Untitled-design.png",
                        btnRedirectUrl = "https://discord.com/users/494115165927637007",
                        text = Localization.Key.LOLCATplAck.rememberLocalizedString()
                    )
                }
                if (platform !is Platform.Android.Mobile) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, top = 15.dp)
                    )
                }
            }
            item {
                Text(
                    text = Localization.Key.LinkoraOpenSourceAcknowledgement.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp)
                )
            }
            items(settingsScreenViewModel.acknowledgementSection()) {
                SettingComponent(
                    settingComponentParam = it
                )
            }
            item {
                Text(
                    text = Localization.Key.AckEndingText.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp)
                )
            }
            item {
                Spacer(modifier = Modifier)
            }
        }
    }
}

@Composable
private fun AcknowledgeComponent(
    imgUrl: String,
    text: String,
    btnRedirectUrl: String,
    btnText: String,
    imgModifier: Modifier = Modifier.fillMaxWidth(0.45f).height(250.dp)
) {
    val localUriHandler = LocalUriHandler.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(15.dp).clip(RoundedCornerShape(15.dp)).border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(0.5f),
            shape = RoundedCornerShape(15.dp)
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CoilImage(
            imgURL = imgUrl,
            modifier = imgModifier,
            contentDescription = null,
            userAgent = AppPreferences.primaryJsoupUserAgent.value
        )
        Column(modifier = Modifier.padding(top = 7.5.dp, bottom = 7.5.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(start = 15.dp, end = 15.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            FilledTonalButton(
                onClick = {
                    localUriHandler.openUri(btnRedirectUrl)
                },
                modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp)
                    .pulsateEffect()
            ) {
                Text(
                    text = btnText,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}