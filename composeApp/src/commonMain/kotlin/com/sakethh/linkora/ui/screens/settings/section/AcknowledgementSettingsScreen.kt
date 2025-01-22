package com.sakethh.linkora.ui.screens.settings.section

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.CoilImage
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcknowledgementSettingsScreen() {
    val navController = LocalNavController.current
    val settingsScreenViewModel: SettingsScreenViewModel =
        viewModel(factory = genericViewModelFactory {
            SettingsScreenViewModel(DependencyContainer.preferencesRepo.value)
        })
    val localUriHandler = LocalUriHandler.current
    val platform = platform()
    SettingsSectionScaffold(
        topAppBarText = Navigation.Settings.AcknowledgementSettingsScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                .navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            if (platform !is Platform.Android.Mobile) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(15.dp)
                            .clip(RoundedCornerShape(15.dp)).border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(0.5f),
                                shape = RoundedCornerShape(15.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CoilImage(
                            imgURL = "https://i.ibb.co/ZK4yGwQ/Untitled-design.png",
                            modifier = Modifier.fillMaxWidth(0.45f).height(250.dp),
                            contentDescription = null,
                            userAgent = AppPreferences.primaryJsoupUserAgent.value
                        )
                        Column {
                            Text(
                                text = "Menu Bottom Sheet and Link Dialog Box for Android tablet/desktop platforms are inspired by and based on the mockups created by LOLCATpl.",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.padding(15.dp)
                            )
                            FilledTonalButton(
                                onClick = {
                                    localUriHandler.openUri("https://discord.com/users/494115165927637007")
                                },
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 15.dp, end = 15.dp)
                            ) {
                                Text(
                                    text = "LOLCATpl on Discord",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                    }
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, bottom = 15.dp)
                    )
                }
            } else {
                item {
                    Spacer(Modifier)
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
                Spacer(modifier = Modifier)
            }
        }
    }
}