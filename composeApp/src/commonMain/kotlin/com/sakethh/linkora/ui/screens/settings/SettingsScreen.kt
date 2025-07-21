package com.sakethh.linkora.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SettingsInputSvideo
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.addEdgeToEdgeScaffoldPadding
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.ItemDivider
import com.sakethh.linkora.ui.utils.pulsateEffect
import linkora.composeapp.generated.resources.Res
import linkora.composeapp.generated.resources.discord
import linkora.composeapp.generated.resources.github
import linkora.composeapp.generated.resources.twitter
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val navController = LocalNavController.current
    val topAppBarScrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uriHandler = LocalUriHandler.current
    Scaffold(topBar = {
        Column {
            LargeTopAppBar(
                scrollBehavior = topAppBarScrollState, title = {
                    Text(
                        text = Localization.rememberLocalizedString(Localization.Key.Settings),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 22.sp
                    )
                })
        }
    }) { it ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().addEdgeToEdgeScaffoldPadding(it)
                .nestedScroll(topAppBarScrollState.nestedScrollConnection)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(
                        start = 15.dp, end = 15.dp, top = 7.5.dp, bottom = 7.5.dp
                    ).clip(
                        RoundedCornerShape(15.dp)
                    ).fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(top = 7.5.dp)
                ) {
                    Row(modifier = Modifier.padding(top = 7.5.dp, start = 15.dp)) {
                        Text(
                            text = Localization.Key.Linkora.rememberLocalizedString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            fontSize = 18.sp,
                            modifier = Modifier.alignByBaseline(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = Constants.APP_VERSION_NAME,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                            fontSize = 12.sp,
                            modifier = Modifier.alignByBaseline(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Row(
                        modifier = Modifier.padding(start = 10.dp, top = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(modifier = Modifier.pulsateEffect(), onClick = {
                            uriHandler.openUri("https://www.github.com/LinkoraApp")
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.github),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        FilledIconButton(modifier = Modifier.pulsateEffect(), onClick = {
                            uriHandler.openUri("https://discord.gg/ZDBXNtv8MD")
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.discord),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        FilledIconButton(modifier = Modifier.pulsateEffect(), onClick = {
                            uriHandler.openUri("https://www.twitter.com/LinkoraApp")
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.twitter),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                    ItemDivider(
                        paddingValues = PaddingValues(
                            start = 15.dp, end = 15.dp, top = 7.5.dp, bottom = 7.5.dp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        colorOpacity = 0.25f,
                        thickness = 0.25.dp,
                    )
                    Button(
                        onClick = {
                            uriHandler.openUri("https://github.com/LinkoraApp/localization-server")
                        }, modifier = Modifier.padding(start = 15.dp).pulsateEffect()
                    ) {
                        Icon(imageVector = Icons.Default.Translate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.5.dp))
                        Text(
                            text = "Translate",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.5.sp
                        )
                    }
                    Button(
                        onClick = {
                            uriHandler.openUri("https://ko-fi.com/sakethpathike")
                        },
                        modifier = Modifier.padding(start = 15.dp, bottom = 15.dp).pulsateEffect()
                    ) {
                        Icon(imageVector = Icons.Default.Coffee, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.5.dp))
                        Text(
                            text = "Buy me a Coffee",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.5.sp
                        )
                    }
                }
            }
            items(settingsScreenOptions(navController)) {
                SettingSectionComponent(
                    SettingSectionComponentParam(
                        onClick = it.onClick,
                        sectionTitle = it.sectionTitle,
                        sectionIcon = it.sectionIcon,
                        shouldArrowIconAppear = it.shouldArrowIconAppear
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(200.dp))
            }
        }
    }
}

private fun settingsScreenOptions(navController: NavController): List<SettingSectionComponentParam> {
    return listOf(
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(Navigation.Settings.ThemeSettingsScreen)
            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.Theme),
            sectionIcon = Icons.Default.ColorLens
        ),
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(Navigation.Settings.GeneralSettingsScreen)
            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.General),
            sectionIcon = Icons.Default.SettingsInputSvideo
        ),
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(Navigation.Settings.AdvancedSettingsScreen)
            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.Advanced),
            sectionIcon = Icons.Default.Build
        ),
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(Navigation.Settings.LayoutSettingsScreen)
            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.Layout),
            sectionIcon = Icons.Default.Dashboard
        ),
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(Navigation.Settings.LanguageSettingsScreen)
            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.Language),
            sectionIcon = Icons.Default.Language
        ),
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(Navigation.Settings.DataSettingsScreen)
            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.Data),
            sectionIcon = Icons.Default.Storage
        ),
        /*SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.Privacy),
            sectionIcon = Icons.Default.PrivacyTip
        ),*/
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(Navigation.Settings.AboutSettingsScreen)
            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.About),
            sectionIcon = Icons.Default.Info
        ),
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(Navigation.Settings.AcknowledgementSettingsScreen)
            },
            sectionTitle = Localization.getLocalizedString(Localization.Key.Acknowledgments),
            sectionIcon = Icons.Default.Group
        ),
    )
}