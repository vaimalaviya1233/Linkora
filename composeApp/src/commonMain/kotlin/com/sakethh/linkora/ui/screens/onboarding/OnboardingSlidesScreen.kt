package com.sakethh.linkora.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.primaryContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.di.LinkoraSDKProvider
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.CoilImage
import com.sakethh.linkora.ui.components.folder.FolderComponent
import com.sakethh.linkora.ui.components.link.LinkListItemComposable
import com.sakethh.linkora.ui.domain.model.FolderComponentParam
import com.sakethh.linkora.ui.domain.model.LinkUIComponentParam
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.ItemDivider
import com.sakethh.linkora.ui.utils.pulsateEffect
import kotlinx.coroutines.launch

private data class OnboardingSlide(val screen: @Composable () -> Unit)

@Composable
fun OnboardingSlidesScreen(onOnboardingComplete: () -> Unit) {
    val pagerState = rememberPagerState { 4 }
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val slides = remember {
        listOf(
            OnboardingSlide {
                Slide1()
            },
            OnboardingSlide {
                Slide2()
            },
            OnboardingSlide {
                Slide3()
            },
            OnboardingSlide {
                Slide4()
            }
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxSize()
                    .graphicsLayer {
                        val pageOffset =
                            (pagerState.currentPage - it) + pagerState.currentPageOffsetFraction
                        val scale = lerp(1f, 2f, pageOffset)
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                slides[it].screen()
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomEnd),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(15.dp)
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage != 0, enter = fadeIn(), exit = fadeOut()
                ) {
                    FilledTonalButton(modifier = Modifier.pulsateEffect(), onClick = {
                        if (pagerState.isScrollInProgress) return@FilledTonalButton
                        coroutineScope.launch {
                            try {
                                pagerState.animateScrollToPage(
                                    pagerState.currentPage - 1, animationSpec = tween(750)
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }) {
                        Text(
                            text = Localization.Key.PreviousPage.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                Spacer(Modifier.width(15.dp))
                Button(modifier = Modifier.pulsateEffect(), onClick = {
                    if (pagerState.currentPage == pagerState.pageCount - 1) {
                        onOnboardingComplete()
                        navController.navigate(Navigation.Root.HomeScreen) {
                            popUpTo(0)
                        }
                        return@Button
                    }
                    if (pagerState.isScrollInProgress) return@Button
                    coroutineScope.launch {
                        try {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1, animationSpec = tween(750)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }) {
                    val text = rememberSaveable(pagerState.currentPage) {
                        if (pagerState.currentPage == pagerState.pageCount - 1) {
                            Localization.Key.Done.getLocalizedString()
                        } else {
                            Localization.Key.NextPage.getLocalizedString()
                        }
                    }
                    AnimatedContent(text, transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    }) {
                        Text(text = it, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun Slide1() {
    Column(
        modifier = Modifier.padding(15.dp).fillMaxSize(), verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.Start
    ) {
        CoilImage(
            modifier = Modifier.clip(RoundedCornerShape(15.dp))
                .sizeIn(maxWidth = 250.dp, maxHeight = 250.dp).wrapContentSize().border(
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
        Spacer(modifier = Modifier.height(25.dp))
        Text(
            text = Localization.Key.AppIntroSlide1WelcomeToLabel.rememberLocalizedString(),
            style = MaterialTheme.typography.titleSmall,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        SlideTitle(Localization.Key.Linkora.rememberLocalizedString())
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = Localization.Key.AppIntroSlide1Label.rememberLocalizedString(),
            style = MaterialTheme.typography.titleSmall,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.height(15.dp))
        SlideDesc(Localization.Key.AppIntroSlide1SwipeLabel.rememberLocalizedString())
        Spacer(modifier = Modifier.height(75.dp))
    }
}

@Composable
private fun SlideTitle(string: String, modifier: Modifier = Modifier, fontSize: TextUnit = 24.sp) {
    Text(
        text = string,
        style = MaterialTheme.typography.titleLarge,
        fontSize = fontSize,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
private fun SlideDesc(string: String, modifier: Modifier = Modifier) {
    Text(
        text = string,
        style = MaterialTheme.typography.titleMedium,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(0.85f),
        modifier = modifier
    )
}

@Composable
private fun Slide2() {
    val localUriHandler = LocalUriHandler.current
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState(1)).fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            FolderComponent(
                FolderComponentParam(
                    folder = Folder(
                        name = Localization.Key.AppIntroSlide2Folder1Name.rememberLocalizedString(),
                        note = Localization.Key.AppIntroSlide2Folder1Note.rememberLocalizedString(),
                        parentFolderId = null
                    ),
                    onClick = {},
                    onLongClick = {},
                    onMoreIconClick = {},
                    isCurrentlyInDetailsView = rememberSaveable {
                        mutableStateOf(false)
                    },
                    showMoreIcon = rememberSaveable {
                        mutableStateOf(true)
                    },
                    isSelectedForSelection = rememberSaveable {
                        mutableStateOf(false)
                    },
                    showCheckBox = rememberSaveable {
                        mutableStateOf(false)
                    },
                    onCheckBoxChanged = {})
            )
            FolderComponent(
                FolderComponentParam(
                    folder = Folder(
                        name = Localization.Key.AppIntroSlide2Folder2Name.rememberLocalizedString(),
                        note = Localization.Key.AppIntroSlide2Folder2Note.rememberLocalizedString(),
                        parentFolderId = null
                    ),
                    onClick = {},
                    onLongClick = {},
                    onMoreIconClick = {},
                    isCurrentlyInDetailsView = rememberSaveable {
                        mutableStateOf(false)
                    },
                    showMoreIcon = rememberSaveable {
                        mutableStateOf(true)
                    },
                    isSelectedForSelection = rememberSaveable {
                        mutableStateOf(false)
                    },
                    showCheckBox = rememberSaveable {
                        mutableStateOf(false)
                    },
                    onCheckBoxChanged = {})
            )
            LinkListItemComposable(
                linkUIComponentParam = LinkUIComponentParam(
                    link = Link(
                        title = "Red Dead Redemption 2 - Rockstar Games",
                        baseURL = "rockstargames.com",
                        imgURL = "https://media-rockstargames-com.akamaized.net/rockstargames-newsite/img/global/downloads/buddyiconsconavatars/rdr2_officialart1_256x256.jpg",
                        url = "https://www.rockstargames.com/reddeadredemption2",
                        userAgent = AppPreferences.primaryJsoupUserAgent.value,
                        linkType = LinkType.SAVED_LINK,
                        localId = 0L,
                        note = "",
                        idOfLinkedFolder = null
                    ),
                    onMoreIconClick = { -> },
                    onLinkClick = { ->
                        localUriHandler.openUri("https://www.rockstargames.com/reddeadredemption2")
                    },
                    onForceOpenInExternalBrowserClicked = { -> },
                    isSelectionModeEnabled = mutableStateOf(false),
                    isItemSelected = mutableStateOf(false),
                    onLongClick = { -> },
                ), forTitleOnlyView = false,
                onShare = {
                    LinkoraSDKProvider.getInstance().nativeUtils.onShare(it)
                }
            )
            LinkListItemComposable(
                linkUIComponentParam = LinkUIComponentParam(
                    link = Link(
                        title = "Nas | Spotify",
                        baseURL = "open.spotify.com",
                        imgURL = "https://ucarecdn.com/9b4d5145-a417-4ff9-a7e5-93a452a443c8/-/crop/974x818/26,197/-/preview/",
                        url = "https://open.spotify.com/artist/20qISvAhX20dpIbOOzGK3q",
                        userAgent = AppPreferences.primaryJsoupUserAgent.value,
                        linkType = LinkType.SAVED_LINK,
                        localId = 0L,
                        note = "",
                        idOfLinkedFolder = null
                    ),
                    onForceOpenInExternalBrowserClicked = { -> },
                    isSelectionModeEnabled = mutableStateOf(false),
                    isItemSelected = mutableStateOf(false),
                    onLongClick = { -> }, onMoreIconClick = { -> },
                    onLinkClick = { ->
                        localUriHandler.openUri("https://open.spotify.com/artist/20qISvAhX20dpIbOOzGK3q")
                    },
                ), forTitleOnlyView = false,
                imageAlignment = Alignment.TopCenter,
                onShare = {
                    LinkoraSDKProvider.getInstance().nativeUtils.onShare(it)
                }
            )
            Spacer(modifier = Modifier.height(5.dp))
            SlideTitle(
                string = Localization.Key.AppIntroSlide2MainLabel.rememberLocalizedString(),
                modifier = Modifier.padding(start = 15.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            SlideDesc(
                modifier = Modifier.padding(start = 15.dp, bottom = 75.dp),
                string = Localization.Key.AppIntroSlide2MainLabelDesc.rememberLocalizedString()
            )
        }
    }
}

@Composable
private fun Slide3() {
    val pagerState = rememberPagerState { 2 }
    val localUriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
        Text(
            text = Localization.Key.SelectedPanel.rememberLocalizedString(),
            color = MaterialTheme.colorScheme.primary.copy(0.9f),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 10.dp, bottom = 5.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.pulsateEffect().fillMaxWidth().padding(start = 5.dp, end = 5.dp),
        ) {
            Spacer(Modifier.width(5.dp))
            FilledTonalIconButton(onClick = {

            }, modifier = Modifier.size(22.dp)) {
                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = Localization.Key.AppIntroSlide3PanelName.rememberLocalizedString(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(0.8f)
            )
        }
        ScrollableTabRow(
            modifier = Modifier.fillMaxWidth(), selectedTabIndex = pagerState.currentPage,
            divider = {}
        ) {
            (0..1).forEach {
                key(it) {
                    Tab(selected = pagerState.currentPage == it, onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(it)
                        }.start()
                    }) {
                        Text(
                            text = if (it == 0) Localization.Key.AppIntroSlide2Folder1Name.rememberLocalizedString() else Localization.Key.AppIntroSlide3Folder2Name.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(15.dp),
                            color = if (pagerState.currentPage == it) primaryContentColor else MaterialTheme.colorScheme.onSurface.copy(
                                0.70f
                            )
                        )
                    }
                }
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                when (it) {
                    0 -> {
                        FolderComponent(
                            FolderComponentParam(
                                folder = Folder(
                                    name = Localization.Key.AppIntroSlide3Folder2_1Name.rememberLocalizedString(),
                                    note = Localization.Key.AppIntroSlide3Folder2_1Note.rememberLocalizedString(),
                                    parentFolderId = null
                                ),
                                onClick = {},
                                onLongClick = {},
                                onMoreIconClick = {},
                                isCurrentlyInDetailsView = rememberSaveable {
                                    mutableStateOf(false)
                                },
                                showMoreIcon = rememberSaveable {
                                    mutableStateOf(true)
                                },
                                isSelectedForSelection = rememberSaveable {
                                    mutableStateOf(false)
                                },
                                showCheckBox = rememberSaveable {
                                    mutableStateOf(false)
                                },
                                onCheckBoxChanged = {})
                        )
                        LinkListItemComposable(
                            linkUIComponentParam = LinkUIComponentParam(
                                link = Link(
                                    title = "A Plague Tale: Requiem | Download and Buy Today - Epic Games Store",
                                    baseURL = "store.epicgames.com",
                                    imgURL = remember {
                                        listOf(
                                            "https://pbs.twimg.com/media/FUPM2TrWYAAQsXm?format=jpg",
                                            "https://pbs.twimg.com/media/FLJx9epWYAADM0O?format=jpg",
                                            "https://pbs.twimg.com/media/FAdLIY8WUAEgLRM?format=jpg",
                                            "https://pbs.twimg.com/media/ETUI-RDWsAE2UYR?format=jpg",
                                            "https://pbs.twimg.com/media/ET9J7vTWsAYVtvG?format=jpg",
                                            "https://pbs.twimg.com/media/GRo2CKkWUAEsdEl?format=jpg",
                                            "https://pbs.twimg.com/media/FezZxQYWQAQ4K3f?format=jpg",
                                            "https://pbs.twimg.com/media/FezaHWkX0AIWvvU?format=jpg",
                                            "https://i.redd.it/qoa6gk4ii8571.jpg",
                                            "https://i.redd.it/8psapajhi8571.jpg"
                                        ).random()
                                    },
                                    url = "https://store.epicgames.com/en-US/p/a-plague-tale-requiem",
                                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                                    linkType = LinkType.SAVED_LINK,
                                    localId = 0L,
                                    note = "",
                                    idOfLinkedFolder = null
                                ),
                                onMoreIconClick = { -> },
                                onLinkClick = { ->
                                    localUriHandler.openUri("https://store.epicgames.com/en-US/p/a-plague-tale-requiem")
                                },
                                onForceOpenInExternalBrowserClicked = { -> },
                                isSelectionModeEnabled = mutableStateOf(false),
                                isItemSelected = mutableStateOf(false),
                                onLongClick = { -> },
                            ), forTitleOnlyView = false,
                            onShare = {
                                LinkoraSDKProvider.getInstance().nativeUtils.onShare(it)
                            }
                        )
                    }

                    1 -> {
                        FolderComponent(
                            FolderComponentParam(
                                folder = Folder(
                                    name = Localization.Key.AppIntroSlide3Folder3_1Name.rememberLocalizedString(),
                                    note = Localization.Key.AppIntroSlide3Folder3_1Note.rememberLocalizedString(),
                                    parentFolderId = null
                                ),
                                onClick = {},
                                onLongClick = {},
                                onMoreIconClick = {},
                                isCurrentlyInDetailsView = rememberSaveable {
                                    mutableStateOf(false)
                                },
                                showMoreIcon = rememberSaveable {
                                    mutableStateOf(true)
                                },
                                isSelectedForSelection = rememberSaveable {
                                    mutableStateOf(false)
                                },
                                showCheckBox = rememberSaveable {
                                    mutableStateOf(false)
                                },
                                onCheckBoxChanged = {})
                        )
                        LinkListItemComposable(
                            linkUIComponentParam = LinkUIComponentParam(
                                link = Link(
                                    title = "7 Features in Kotlin's Standard Library that You Might Have Overlooked",
                                    baseURL = "youtube.com",
                                    imgURL = "https://i.ytimg.com/vi/OFWMtmqocV8/maxresdefault.jpg",
                                    url = "https://www.youtube.com/watch?v=OFWMtmqocV8",
                                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                                    linkType = LinkType.SAVED_LINK,
                                    localId = 0L,
                                    note = "",
                                    idOfLinkedFolder = null
                                ),
                                onMoreIconClick = { -> },
                                onLinkClick = { ->
                                    localUriHandler.openUri("https://www.youtube.com/watch?v=OFWMtmqocV8")
                                },
                                onForceOpenInExternalBrowserClicked = { -> },
                                isSelectionModeEnabled = mutableStateOf(false),
                                isItemSelected = mutableStateOf(false),
                                onLongClick = { -> },
                            ),
                            forTitleOnlyView = false,
                            imageAlignment = Alignment.TopCenter,
                            onShare = {
                                LinkoraSDKProvider.getInstance().nativeUtils.onShare(it)
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        SlideTitle(
            string = Localization.Key.AppIntroSlide3MainLabel.rememberLocalizedString(),
            modifier = Modifier.fillMaxWidth().padding(start = 15.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        SlideDesc(
            modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp),
            string = Localization.Key.AppIntroSlide3MainLabelDesc.rememberLocalizedString()
        )
        Spacer(modifier = Modifier.height(75.dp))
    }
}

@Composable
private fun Slide4() {
    Column(
        modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 75.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        SlideTitle(string = Localization.Key.AppIntroSlide4Label1.rememberLocalizedString())
        Spacer(Modifier.height(5.dp))
        listOf(
            Localization.Key.AppIntroSlide4Label1Desc1.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc2.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc3.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc4.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc5.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc6.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc7.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc8.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc9.getLocalizedString(),
            Localization.Key.AppIntroSlide4Label1Desc10.getLocalizedString()
        ).forEach {
            key(it) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(text = Typography.bullet.toString())
                    Spacer(modifier = Modifier.width(5.dp))
                    SlideDesc(
                        string = it
                    )
                }
            }
        }
        ItemDivider(
            paddingValues = PaddingValues(top = 15.dp, bottom = 15.dp),
            colorOpacity = 0.95f,
            thickness = 2.dp
        )
        SlideTitle(string = Localization.Key.AppIntroSlide4Label2.rememberLocalizedString())
        Spacer(Modifier.height(5.dp))
        SlideDesc(string = Localization.Key.AppIntroSlide4Label2Desc.rememberLocalizedString())
    }
}