package com.sakethh.linkora.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.linkora.ui.utils.rememberDeserializableObject
import com.sakethh.platform
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class AddItemFABParam @OptIn(ExperimentalMaterial3Api::class) constructor(
    val newLinkBottomModalSheetState: SheetState,
    val shouldBtmSheetForNewLinkAdditionBeEnabled: MutableState<Boolean>,
    val shouldScreenTransparencyDecreasedBoxVisible: MutableState<Boolean>,
    val shouldDialogForNewFolderAppear: MutableState<Boolean>,
    val shouldDialogForNewLinkAppear: MutableState<Boolean>,
    val isMainFabRotated: MutableState<Boolean>,
    val rotationAnimation: Animatable<Float, AnimationVector1D>,
    val inASpecificScreen: Boolean
)

@Composable
fun AddItemFab(
    addItemFABParam: AddItemFABParam
) {
    val currentIconForMainFAB = remember(addItemFABParam.isMainFabRotated.value) {
        mutableStateOf(
            if (addItemFABParam.isMainFabRotated.value) {
                Icons.Default.AddLink
            } else {
                Icons.Default.Add
            }
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val rootRouteList = rememberDeserializableObject {
        listOf(
            Navigation.Root.HomeScreen,
            Navigation.Root.SearchScreen,
            Navigation.Root.CollectionsScreen,
            Navigation.Root.SettingsScreen,
        )
    }
    val currentBackStackEntryState = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntryState.value?.destination
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(
            bottom = if (!addItemFABParam.inASpecificScreen && platform() == Platform.Android.Mobile && rootRouteList.any {
                    currentRoute?.hasRoute(it::class) == true
                }) 82.dp else 0.dp
        )
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (addItemFABParam.isMainFabRotated.value) {
                AnimatedVisibility(
                    visible = addItemFABParam.isMainFabRotated.value,
                    enter = androidx.compose.animation.fadeIn(
                        androidx.compose.animation.core.tween(
                            200
                        )
                    ),
                    exit = androidx.compose.animation.fadeOut(
                        androidx.compose.animation.core.tween(
                            200
                        )
                    )
                ) {
                    Text(
                        text = Localization.rememberLocalizedString(Localization.Key.CreateANewFolder),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(
                            end = 15.dp
                        )
                    )
                }
            }
            AnimatedVisibility(
                visible = addItemFABParam.isMainFabRotated.value,
                enter = androidx.compose.animation.scaleIn(
                    animationSpec = androidx.compose.animation.core.tween(
                        300
                    )
                ),
                exit = androidx.compose.animation.scaleOut(
                    androidx.compose.animation.core.tween(300)
                )
            ) {
                FloatingActionButton(
                    modifier = Modifier.pulsateEffect(),
                    onClick = {
                        addItemFABParam.shouldScreenTransparencyDecreasedBoxVisible.value =
                            false
                        addItemFABParam.shouldDialogForNewFolderAppear.value = true
                        addItemFABParam.isMainFabRotated.value = false
                    }) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = null
                    )
                }
            }

        }
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.height(
                15.dp
            )
        )
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (addItemFABParam.isMainFabRotated.value) {
                AnimatedVisibility(
                    visible = addItemFABParam.isMainFabRotated.value,
                    enter = androidx.compose.animation.fadeIn(
                        androidx.compose.animation.core.tween(
                            200
                        )
                    ),
                    exit = androidx.compose.animation.fadeOut(
                        androidx.compose.animation.core.tween(
                            200
                        )
                    )
                ) {
                    Text(
                        text = Localization.rememberLocalizedString(Localization.Key.AddANewLink),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(
                            end = 15.dp
                        )
                    )
                }
            }
            FloatingActionButton(
                modifier = Modifier
                    .rotate(
                        addItemFABParam.rotationAnimation.value
                    )
                    .pulsateEffect(),
                onClick = {
                    if (addItemFABParam.isMainFabRotated.value) {
                        addItemFABParam.shouldScreenTransparencyDecreasedBoxVisible.value =
                            false
                        addItemFABParam.shouldDialogForNewLinkAppear.value = true
                        addItemFABParam.isMainFabRotated.value = false
                    } else {
                        coroutineScope.launch {
                            kotlinx.coroutines.awaitAll(async {
                                addItemFABParam.rotationAnimation.animateTo(
                                    360f,
                                    animationSpec = androidx.compose.animation.core.tween(300)
                                )
                            }, async {
                                addItemFABParam.shouldScreenTransparencyDecreasedBoxVisible.value =
                                    true
                                kotlinx.coroutines.delay(10L)
                                addItemFABParam.isMainFabRotated.value = true
                            })
                        }.invokeOnCompletion {
                            coroutineScope.launch {
                                addItemFABParam.rotationAnimation.snapTo(0f)
                            }
                        }
                    }
                }) {
                Icon(
                    imageVector = currentIconForMainFAB.value,
                    contentDescription = null
                )
            }
        }
    }
}