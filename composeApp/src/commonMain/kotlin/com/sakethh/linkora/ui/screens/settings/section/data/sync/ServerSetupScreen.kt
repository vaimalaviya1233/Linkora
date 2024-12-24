package com.sakethh.linkora.ui.screens.settings.section.data.sync

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.ui.InfoCard
import com.sakethh.linkora.common.utils.fillMaxWidthWithPadding
import com.sakethh.linkora.domain.SyncType
import com.sakethh.linkora.ui.domain.model.ServerConnection
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.linkora.ui.utils.rememberMutableEnum
import com.sakethh.poppinsFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSetupScreen(
    navController: NavController, serverManagementViewModel: ServerManagementViewModel
) {
    val serverUrl = rememberSaveable {
        mutableStateOf(AppPreferences.serverUrl.value)
    }
    val securityToken = rememberSaveable {
        mutableStateOf(AppPreferences.serverSecurityToken.value)
    }
    val selectedSyncType = rememberMutableEnum(SyncType::class.java) {
        mutableStateOf(AppPreferences.serverSyncType.value)
    }
    SettingsSectionScaffold(
        topAppBarText = Navigation.Settings.Data.ServerSetupScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                .navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item {
                Spacer(Modifier)
            }
            item {
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                TextField(
                    textStyle = TextStyle(fontFamily = poppinsFontFamily),
                    modifier = Modifier.fillMaxWidthWithPadding(),
                    value = serverUrl.value,
                    onValueChange = {
                        serverUrl.value = if (it.endsWith("/testBearer")) {
                            it
                        } else {
                            it.replace("/", "").plus("/testBearer")
                        }
                    },
                    label = {
                        Text(text = "Server URL", style = MaterialTheme.typography.titleMedium)
                    },
                    supportingText = {
                        Text(
                            text = "Ensure the server is running. If hosted locally, the server URL should include the correct port number. No port is needed if the server is not hosted locally.",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    readOnly = serverManagementViewModel.serverSetupState.value.isConnectedSuccessfully && serverManagementViewModel.serverSetupState.value.isConnecting.not()
                )
            }

            item {
                TextField(
                    textStyle = TextStyle(fontFamily = poppinsFontFamily),
                    modifier = Modifier.fillMaxWidthWithPadding(),
                    value = securityToken.value,
                    onValueChange = {
                        securityToken.value = it
                    },
                    label = {
                        Text(text = "Security Token", style = MaterialTheme.typography.titleMedium)
                    },
                    readOnly = serverManagementViewModel.serverSetupState.value.isConnectedSuccessfully && serverManagementViewModel.serverSetupState.value.isConnecting.not()
                )
            }

            item {
                if (serverManagementViewModel.serverSetupState.value.isConnecting) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidthWithPadding()
                    )
                } else if (serverManagementViewModel.serverSetupState.value.isConnectedSuccessfully) {
                    InfoCard(
                        info = "Server Exists and Is Reachable!",
                        paddingValues = PaddingValues(start = 15.dp, end = 15.dp)
                    )
                } else {
                    Button(
                        onClick = {
                            serverManagementViewModel.testServerConnection(
                                serverUrl = serverUrl.value, token = securityToken.value
                            )
                        }, modifier = Modifier.fillMaxWidthWithPadding().pulsateEffect()
                    ) {
                        Text(
                            text = "Test Server Availability",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            if (serverManagementViewModel.serverSetupState.value.isConnectedSuccessfully.not()) {
                return@LazyColumn
            }
            item {
                HorizontalDivider(modifier = Modifier.fillMaxWidthWithPadding())
            }
            item {
                Text(
                    text = "Sync Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                SyncType.entries.forEachIndexed { index, syncType ->
                    if (index > 0) {
                        Spacer(Modifier.height(5.dp))
                    } else {
                        Spacer(Modifier.height(15.dp))
                    }
                    Column(modifier = Modifier.clickable(onClick = {
                        selectedSyncType.value = syncType
                    }, indication = null, interactionSource = remember {
                        MutableInteractionSource()
                    }).pulsateEffect().fillMaxWidthWithPadding()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = syncType == selectedSyncType.value, onClick = {
                                    selectedSyncType.value = syncType
                                })
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = syncType.asUIString(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = syncType.description(),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        serverManagementViewModel.saveServerConnection(
                            serverConnection = ServerConnection(
                                serverUrl = serverUrl.value,
                                authToken = securityToken.value,
                                syncType = selectedSyncType.value
                            )
                        )
                    }, modifier = Modifier.fillMaxWidthWithPadding().pulsateEffect()
                ) {
                    Text(
                        text = "Use This Connection",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            item {
                Spacer(Modifier)
            }
        }
    }
}