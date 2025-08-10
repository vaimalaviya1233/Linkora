package com.sakethh.linkora.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
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
import com.sakethh.linkora.Localization
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.linkora.ui.utils.rememberDeserializableObject
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class AddItemFABParam @OptIn(ExperimentalMaterial3Api::class) constructor(
    val showBtmSheetForNewLinkAddition: MutableState<Boolean>,
    val isReducedTransparencyBoxVisible: MutableState<Boolean>,
    val showDialogForNewFolder: MutableState<Boolean>,
    val shouldShowAddLinkDialog: MutableState<Boolean>,
    val isMainFabRotated: MutableState<Boolean>,
    val rotationAnimation: Animatable<Float, AnimationVector1D>,
    val inASpecificScreen: Boolean
)


// There are a couple of things in this file I'd do differently if I was writing it now.
// I mostly copy-pasted this during the KMP migration from Android-only, so it’s been some time since I wrote it.


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
    Column {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            modifier = Modifier.align(Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (addItemFABParam.isMainFabRotated.value) {
                AnimatedVisibility(
                    visible = addItemFABParam.isMainFabRotated.value, enter = fadeIn(
                        tween(
                            200
                        )
                    ), exit = fadeOut(
                        tween(
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
                    animationSpec = tween(
                        300
                    )
                ),
                exit = androidx.compose.animation.scaleOut(
                    tween(300)
                )
            ) {
                FloatingActionButton(
                    modifier = Modifier.pulsateEffect(), onClick = {
                        addItemFABParam.isReducedTransparencyBoxVisible.value = false
                        addItemFABParam.showDialogForNewFolder.value = true
                        addItemFABParam.isMainFabRotated.value = false
                        coroutineScope.launch {
                            addItemFABParam.rotationAnimation.snapTo(-180f)
                        }
                    }) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder, contentDescription = null
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
                    visible = addItemFABParam.isMainFabRotated.value, enter = fadeIn(
                        tween(
                            300
                        )
                    ), exit = fadeOut(
                        tween(
                            300
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
                modifier = Modifier.rotate(
                        addItemFABParam.rotationAnimation.value
                    ).pulsateEffect(), onClick = {
                    if (addItemFABParam.isMainFabRotated.value) {
                        addItemFABParam.isReducedTransparencyBoxVisible.value = false
                        addItemFABParam.shouldShowAddLinkDialog.value = true
                        addItemFABParam.isMainFabRotated.value = false
                        coroutineScope.launch {
                            addItemFABParam.rotationAnimation.snapTo(-180f)
                        }
                    } else {
                        coroutineScope.launch {
                            kotlinx.coroutines.awaitAll(async {
                                addItemFABParam.rotationAnimation.animateTo(
                                    180f, animationSpec = tween(500)
                                )
                            }, async {
                                addItemFABParam.isReducedTransparencyBoxVisible.value = true
                                kotlinx.coroutines.delay(10L)
                                addItemFABParam.isMainFabRotated.value = true
                            })
                        }
                    }
                }) {
                AnimatedContent(targetState = currentIconForMainFAB.value, transitionSpec = {
                    fadeIn(tween(500)) togetherWith fadeOut(tween(250))
                }) {
                    Icon(
                        imageVector = it, contentDescription = null
                    )
                }
            }
        }
    }
}