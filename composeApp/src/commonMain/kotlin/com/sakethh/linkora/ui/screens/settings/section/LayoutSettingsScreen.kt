package com.sakethh.linkora.ui.screens.settings.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.Link
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.link.GridViewLinkUIComponent
import com.sakethh.linkora.ui.components.link.LinkListItemComposable
import com.sakethh.linkora.ui.domain.Layout
import com.sakethh.linkora.ui.domain.model.LinkPref
import com.sakethh.linkora.ui.domain.model.LinkUIComponentParam
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.utils.genericViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutSettingsScreen() {
    val navController = LocalNavController.current
    val settingsScreenViewModel: SettingsScreenViewModel =
        viewModel(factory = genericViewModelFactory {
            SettingsScreenViewModel(DependencyContainer.preferencesRepo.value)
        })
    val sampleList = remember {
        listOf(
            LinkUIComponentParam(
                link = Link(
                    linkTitle = "Red Dead Redemption 2 - Rockstar Games",
                    baseURL = "rockstargames.com",
                    imgURL = "https://media-rockstargames-com.akamaized.net/rockstargames-newsite/img/global/games/fob/640/reddeadredemption2.jpg",
                    webURL = "https://www.rockstargames.com/reddeadredemption2",
                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                    linkType = LinkType.SAVED_LINK,
                    id = 0L,
                    infoForSaving = "",
                    lastModified = "",
                    isLinkedWithSavedLinks = false,
                    isLinkedWithFolders = false,
                    idOfLinkedFolder = null
                ),
                onMoreIconClick = { -> },
                onLinkClick = { -> },
                onForceOpenInExternalBrowserClicked = { -> },
                isSelectionModeEnabled = mutableStateOf(false),
                isItemSelected = mutableStateOf(false),
                onLongClick = { -> },
            ),
            LinkUIComponentParam(
                link = Link(
                    linkTitle = "A Plague Tale: Requiem | Download and Buy Today - Epic Games Store",
                    baseURL = "store.epicgames.com",
                    imgURL = listOf(
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
                    ).random(),
                    webURL = "https://store.epicgames.com/en-US/p/a-plague-tale-requiem",
                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                    linkType = LinkType.SAVED_LINK,
                    id = 0L,
                    infoForSaving = "",
                    lastModified = "",
                    isLinkedWithSavedLinks = false,
                    isLinkedWithFolders = false,
                    idOfLinkedFolder = null
                ),
                onMoreIconClick = { -> },
                onLinkClick = { -> },
                onForceOpenInExternalBrowserClicked = { -> },
                isSelectionModeEnabled = mutableStateOf(false),
                isItemSelected = mutableStateOf(false),
                onLongClick = { -> },
            ),
            LinkUIComponentParam(
                link = Link(
                    linkTitle = "Nas | Spotify",
                    baseURL = "open.spotify.com",
                    imgURL = "https://i.scdn.co/image/ab6761610000e5eb153198caeef9e3bda92f9285",
                    webURL = "https://open.spotify.com/artist/20qISvAhX20dpIbOOzGK3q",
                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                    linkType = LinkType.SAVED_LINK,
                    id = 0L,
                    infoForSaving = "",
                    lastModified = "",
                    isLinkedWithSavedLinks = false,
                    isLinkedWithFolders = false,
                    idOfLinkedFolder = null
                ),
                onForceOpenInExternalBrowserClicked = { -> },
                isSelectionModeEnabled = mutableStateOf(false),
                isItemSelected = mutableStateOf(false),
                onLongClick = { -> }, onMoreIconClick = { -> }, onLinkClick = { -> },
            ),
            LinkUIComponentParam(
                link = Link(
                    linkTitle = "Photos From 2024",
                    baseURL = "reddit.com",
                    imgURL = "https://i.redd.it/j14an1zv6aae1.jpg",
                    webURL = "https://www.reddit.com/r/nas/comments/1hqsamj/photos_from_2024/",
                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                    linkType = LinkType.SAVED_LINK,
                    id = 0L,
                    infoForSaving = "",
                    lastModified = "",
                    isLinkedWithSavedLinks = false,
                    isLinkedWithFolders = false,
                    idOfLinkedFolder = null
                ),
                onForceOpenInExternalBrowserClicked = { -> },
                isSelectionModeEnabled = mutableStateOf(false),
                isItemSelected = mutableStateOf(false),
                onLongClick = { -> }, onMoreIconClick = { -> }, onLinkClick = { -> },
            ),
            LinkUIComponentParam(
                link = Link(
                    linkTitle = "KanYe West | Spotify",
                    baseURL = "open.spotify.com",
                    imgURL = "https://i.scdn.co/image/b076a71cd18041144e0c5a1f2fc785cc6f6faa37",
                    webURL = "https://open.spotify.com/artist/5K4W6rqBFWDnAN6FQUkS6x",
                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                    linkType = LinkType.SAVED_LINK,
                    id = 0L,
                    infoForSaving = "",
                    lastModified = "",
                    isLinkedWithSavedLinks = false,
                    isLinkedWithFolders = false,
                    idOfLinkedFolder = null
                ),
                onForceOpenInExternalBrowserClicked = { -> },
                isSelectionModeEnabled = mutableStateOf(false),
                isItemSelected = mutableStateOf(false),
                onLongClick = { -> }, onMoreIconClick = { -> }, onLinkClick = { -> },
            ),
            LinkUIComponentParam(
                link = Link(
                    linkTitle = "Hacker (small type)",
                    baseURL = "twitter.com",
                    imgURL = "https://pbs.twimg.com/media/GT7RIrWWwAAjZzg.jpg",
                    webURL = "https://twitter.com/CatWorkers/status/1819121250226127061",
                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                    linkType = LinkType.SAVED_LINK,
                    id = 0L,
                    infoForSaving = "",
                    lastModified = "",
                    isLinkedWithSavedLinks = false,
                    isLinkedWithFolders = false,
                    idOfLinkedFolder = null
                ),
                onForceOpenInExternalBrowserClicked = { -> },
                isSelectionModeEnabled = mutableStateOf(false),
                isItemSelected = mutableStateOf(false),
                onLongClick = { -> }, onMoreIconClick = { -> }, onLinkClick = { -> },
            ),
            LinkUIComponentParam(
                link = Link(
                    linkTitle = "MrMobile [Michael Fisher]",
                    baseURL = "youtube.com",
                    imgURL = "https://yt3.googleusercontent.com/Bf8B_79jyHxP6CVnjV5WKws93l9Vxlk0d7aPmcBygTBDKzgsGrpazdJRFUrfg1sUNlo8YX8rji8",
                    webURL = "https://www.youtube.com/@TheMrMobile",
                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                    linkType = LinkType.SAVED_LINK,
                    id = 0L,
                    infoForSaving = "",
                    lastModified = "",
                    isLinkedWithSavedLinks = false,
                    isLinkedWithFolders = false,
                    idOfLinkedFolder = null
                ),
                onForceOpenInExternalBrowserClicked = { -> },
                isSelectionModeEnabled = mutableStateOf(false),
                isItemSelected = mutableStateOf(false),
                onLongClick = { -> }, onMoreIconClick = { -> }, onLinkClick = { -> },
            ),
            LinkUIComponentParam(
                link = Link(
                    linkTitle = "Philipp Lackner - YouTube",
                    baseURL = "youtube.com",
                    imgURL = "https://yt3.googleusercontent.com/mhup7lzHh_c9b55z0edX65ReN9iJmTF2JU7vMGER9LTOora-NnXtvZdtn_vJmTvW6-y97z0Y=s900-c-k-c0x00ffffff-no-rj",
                    webURL = "https://www.youtube.com/@PhilippLackner",
                    userAgent = AppPreferences.primaryJsoupUserAgent.value,
                    linkType = LinkType.SAVED_LINK,
                    id = 0L,
                    infoForSaving = "",
                    lastModified = "",
                    isLinkedWithSavedLinks = false,
                    isLinkedWithFolders = false,
                    idOfLinkedFolder = null
                ),
                onForceOpenInExternalBrowserClicked = { -> },
                isSelectionModeEnabled = mutableStateOf(false),
                isItemSelected = mutableStateOf(false),
                onLongClick = { -> }, onMoreIconClick = { -> }, onLinkClick = { -> },
            )
        )
    }

    val nonListViewPref = remember {
        listOf(
            LinkPref(
                onClick = {
                    AppPreferences.enableBorderForNonListViews.value =
                        !AppPreferences.enableBorderForNonListViews.value
                    settingsScreenViewModel.changeSettingPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.BORDER_VISIBILITY_FOR_NON_LIST_VIEWS.name),
                        newValue = AppPreferences.enableBorderForNonListViews.value
                    )
                },
                title = Localization.Key.ShowBorderAroundLinks.getLocalizedString(),
                isSwitchChecked = AppPreferences.enableBorderForNonListViews
            ),
            LinkPref(
                onClick = {
                    AppPreferences.enableTitleForNonListViews.value =
                        !AppPreferences.enableTitleForNonListViews.value
                    settingsScreenViewModel.changeSettingPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.TITLE_VISIBILITY_FOR_NON_LIST_VIEWS.name),
                        newValue = AppPreferences.enableTitleForNonListViews.value
                    )
                },
                title = Localization.Key.ShowTitle.getLocalizedString(),
                isSwitchChecked = AppPreferences.enableTitleForNonListViews
            ),
            LinkPref(
                onClick = {
                    AppPreferences.enableBaseURLForNonListViews.value =
                        !AppPreferences.enableBaseURLForNonListViews.value
                    settingsScreenViewModel.changeSettingPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.BASE_URL_VISIBILITY_FOR_NON_LIST_VIEWS.name),
                        newValue = AppPreferences.enableBaseURLForNonListViews.value
                    )
                },
                title = Localization.Key.ShowBaseURL.getLocalizedString(),
                isSwitchChecked = AppPreferences.enableBaseURLForNonListViews
            ),
            LinkPref(
                onClick = {
                    AppPreferences.enableFadedEdgeForNonListViews.value =
                        !AppPreferences.enableFadedEdgeForNonListViews.value
                    settingsScreenViewModel.changeSettingPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.FADED_EDGE_VISIBILITY_FOR_NON_LIST_VIEWS.name),
                        newValue = AppPreferences.enableFadedEdgeForNonListViews.value
                    )
                },
                title = Localization.Key.ShowBottomFadedEdge.getLocalizedString(),
                isSwitchChecked = AppPreferences.enableFadedEdgeForNonListViews
            ),
        )
    }
    SettingsSectionScaffold(
        topAppBarText = Localization.Key.LinkLayoutSettings.rememberLocalizedString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        if (AppPreferences.currentlySelectedLinkLayout.value == Layout.REGULAR_LIST_VIEW.name
            || AppPreferences.currentlySelectedLinkLayout.value == Layout.TITLE_ONLY_LIST_VIEW.name
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                    .navigationBarsPadding()
            ) {
                item {
                    Text(
                        text = Localization.Key.ChooseTheLayoutYouLikeBest.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(15.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(Layout.entries) {
                    LinkViewRadioButtonComponent(
                        it,
                        settingsScreenViewModel,
                        PaddingValues(start = 10.dp)
                    )
                }

                item {
                    HorizontalDivider(
                        Modifier.padding(
                            start = 15.dp, end = 15.dp, top = 15.dp, bottom = 5.dp
                        )
                    )
                }

                item {
                    Text(
                        text = Localization.Key.FeedPreview.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(15.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(sampleList) {
                    LinkListItemComposable(
                        linkUIComponentParam = LinkUIComponentParam(
                        link = Link(
                            linkType = LinkType.SAVED_LINK,
                            id = 0L,
                            linkTitle = it.link.linkTitle,
                            webURL = it.link.webURL,
                            baseURL = it.link.baseURL,
                            imgURL = it.link.imgURL,
                            infoForSaving = it.link.infoForSaving,
                            lastModified = "",
                            isLinkedWithSavedLinks = false,
                            isLinkedWithFolders = false,
                            idOfLinkedFolder = null,
                            userAgent = it.link.userAgent
                        ),
                        isSelectionModeEnabled = remember {
                            mutableStateOf(false)
                        },
                        onMoreIconClick = { -> },
                        onLinkClick = { -> },
                        onForceOpenInExternalBrowserClicked = { -> },
                        isItemSelected = remember {
                            mutableStateOf(false)
                        },
                        onLongClick = { -> }),
                        forTitleOnlyView = AppPreferences.currentlySelectedLinkLayout.value == Layout.TITLE_ONLY_LIST_VIEW.name
                    )
                }
                item {
                    Spacer(Modifier.height(100.dp))
                }
            }
        } else if (AppPreferences.currentlySelectedLinkLayout.value == Layout.GRID_VIEW.name) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .padding(paddingValues)
                    .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                    .navigationBarsPadding()
            ) {
                item(span = {
                    GridItemSpan(maxLineSpan)
                }) {
                    Text(
                        text = Localization.Key.ChooseTheLayoutYouLikeBest.getLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 5.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(Layout.entries, span = {
                    GridItemSpan(maxLineSpan)
                }) {
                    LinkViewRadioButtonComponent(
                        it,
                        settingsScreenViewModel
                    )
                }

                items(nonListViewPref, span = {
                    GridItemSpan(maxLineSpan)
                }) {
                    LinkViewPreferenceSwitch(
                        onClick = it.onClick,
                        title = it.title,
                        isSwitchChecked = it.isSwitchChecked.value
                    )
                }

                item(span = {
                    GridItemSpan(maxLineSpan)
                }) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 15.dp,
                            bottom = 5.dp,
                            start = 5.dp,
                            end = 5.dp
                        ),
                    )
                }

                item(span = {
                    GridItemSpan(maxLineSpan)
                }) {
                    Text(
                        text = Localization.Key.FeedPreview.getLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 10.dp, bottom = 15.dp, start = 5.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(sampleList) {
                    GridViewLinkUIComponent(it, forStaggeredView = false)
                }
                item(span = {
                    GridItemSpan(maxLineSpan)
                }) {
                    Spacer(Modifier.height(100.dp))
                }
            }
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(150.dp),
                modifier = Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .padding(paddingValues)
                    .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                    .navigationBarsPadding()
            ) {
                item(
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Text(
                        text = Localization.Key.ChooseTheLayoutYouLikeBest.getLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 15.dp, bottom = 15.dp, start = 5.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(
                    items = Layout.entries,
                    span = {
                        StaggeredGridItemSpan.FullLine
                    }) {
                    LinkViewRadioButtonComponent(
                        it,
                        settingsScreenViewModel
                    )
                }

                items(items = nonListViewPref, span = { StaggeredGridItemSpan.FullLine }) {
                    LinkViewPreferenceSwitch(
                        onClick = it.onClick,
                        title = it.title,
                        isSwitchChecked = it.isSwitchChecked.value
                    )
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 15.dp,
                            bottom = 5.dp,
                            start = 5.dp,
                            end = 5.dp
                        ),
                    )
                }

                item(span = StaggeredGridItemSpan.FullLine) {
                    Text(
                        text = Localization.Key.FeedPreview.getLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 10.dp, bottom = 15.dp, start = 5.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(sampleList) {
                    GridViewLinkUIComponent(
                        linkUIComponentParam = LinkUIComponentParam(
                            link = Link(
                                linkType = LinkType.SAVED_LINK,
                                id = 0L,
                                linkTitle = it.link.linkTitle,
                                webURL = it.link.webURL,
                                baseURL = it.link.baseURL,
                                imgURL = it.link.imgURL,
                                infoForSaving = it.link.infoForSaving,
                                lastModified = "",
                                isLinkedWithSavedLinks = false,
                                isLinkedWithFolders = false,
                                idOfLinkedFolder = null,
                                userAgent = it.link.userAgent
                            ),
                            isSelectionModeEnabled = remember {
                                mutableStateOf(false)
                            },
                            onMoreIconClick = { -> },
                            onLinkClick = { -> },
                            onForceOpenInExternalBrowserClicked = { -> },
                            isItemSelected = remember {
                                mutableStateOf(false)
                            },
                            onLongClick = { -> }),
                        forStaggeredView = true
                    )
                }
                item(span = StaggeredGridItemSpan.FullLine) {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}


@Composable
private fun LinkViewPreferenceSwitch(
    onClick: () -> Unit,
    title: String,
    isSwitchChecked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onClick()
            }, interactionSource = remember {
                MutableInteractionSource()
            }, indication = null)
            .padding(start = 15.dp, end = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth(0.75f)
        )
        Switch(
            checked = isSwitchChecked,
            onCheckedChange = {
                onClick()
            })
    }
}

@Composable
private fun LinkViewRadioButtonComponent(
    linkLayout: Layout,
    settingsScreenViewModel: SettingsScreenViewModel,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                AppPreferences.currentlySelectedLinkLayout.value = linkLayout.name
                settingsScreenViewModel.changeSettingPreferenceValue(
                    preferenceKey = stringPreferencesKey(AppPreferenceType.CURRENTLY_SELECTED_LINK_VIEW.name),
                    newValue = linkLayout.name
                )
            }
            .padding(paddingValues)
    ) {
        RadioButton(
            selected = AppPreferences.currentlySelectedLinkLayout.value == linkLayout.name,
            onClick = {
                AppPreferences.currentlySelectedLinkLayout.value = linkLayout.name
                settingsScreenViewModel.changeSettingPreferenceValue(
                    preferenceKey = stringPreferencesKey(AppPreferenceType.CURRENTLY_SELECTED_LINK_VIEW.name),
                    newValue = linkLayout.name
                )
            })
        Text(
            text = when (linkLayout) {
                Layout.REGULAR_LIST_VIEW -> Localization.Key.RegularListView.rememberLocalizedString()
                Layout.TITLE_ONLY_LIST_VIEW -> Localization.Key.TitleOnlyListView.rememberLocalizedString()
                Layout.GRID_VIEW -> Localization.Key.GridView.rememberLocalizedString()
                Layout.STAGGERED_VIEW -> Localization.Key.StaggeredView.rememberLocalizedString()
            },
            style = MaterialTheme.typography.titleSmall
        )
    }
}