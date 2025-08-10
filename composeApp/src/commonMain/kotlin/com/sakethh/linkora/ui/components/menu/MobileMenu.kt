package com.sakethh.linkora.ui.components.menu

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.Localization
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.utils.rememberLocalizedString
import com.sakethh.linkora.domain.ComposableContent
import com.sakethh.linkora.ui.components.CoilImage
import com.sakethh.linkora.ui.screens.collections.ItemDivider
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.fadedEdges
import com.sakethh.linkora.ui.utils.pulsateEffect

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MobileMenu(
    menuBtmSheetParam: MenuBtmSheetParam,
    isNoteBtnSelected: MutableState<Boolean>,
    commonMenuContent: ComposableContent
) {
    val isImageAssociatedWithTheLinkIsExpanded = rememberSaveable {
        mutableStateOf(false)
    }

    val localClipBoardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier.navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        if (menuBtmSheetLinkEntries().contains(
                menuBtmSheetParam.menuBtmSheetFor
            ) && menuBtmSheetParam.link!!.value.imgURL.isNotEmpty() && AppPreferences.showAssociatedImageInLinkMenu.value
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight()
            ) {
                CoilImage(
                    modifier = Modifier.animateContentSize().fillMaxWidth().then(
                        if (isImageAssociatedWithTheLinkIsExpanded.value) Modifier.wrapContentHeight() else Modifier.heightIn(
                            max = 150.dp
                        )
                    ).fadedEdges(MaterialTheme.colorScheme),
                    imgURL = menuBtmSheetParam.link.value.imgURL,
                    userAgent = menuBtmSheetParam.link.value.userAgent
                        ?: AppPreferences.primaryJsoupUserAgent.value
                )
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart)
                        .padding(end = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            isImageAssociatedWithTheLinkIsExpanded.value =
                                !isImageAssociatedWithTheLinkIsExpanded.value
                        }, modifier = Modifier.alpha(0.75f).padding(5.dp)
                    ) {
                        Icon(
                            imageVector = if (!isImageAssociatedWithTheLinkIsExpanded.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = ""
                        )
                    }
                    Text(
                        text = menuBtmSheetParam.link.value.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 16.sp,
                        maxLines = 2,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(0.1f)
            )
            Spacer(Modifier.height(5.dp))
        }
        if (menuBtmSheetLinkEntries().contains(
                menuBtmSheetParam.menuBtmSheetFor
            ) && menuBtmSheetParam.link!!.value.imgURL.isEmpty()
        ) {
            MenuNonImageHeader(
                onClick = {
                    localClipBoardManager.setText(AnnotatedString(menuBtmSheetParam.link.value.title))
                    coroutineScope.pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.Key.CopiedTitleToTheClipboard.getLocalizedString()
                        )
                    )
                },
                menuBtmSheetType = MenuBtmSheetType.Link.FolderLink,
                text = menuBtmSheetParam.link.value.title.toString()
            )
            ItemDivider(
                colorOpacity = 0.25f, paddingValues = PaddingValues(start = 15.dp, end = 15.dp)
            )
        }

        if (menuBtmSheetParam.menuBtmSheetFor == MenuBtmSheetType.Folder.RegularFolder) {
            MenuNonImageHeader(
                onClick = {
                    localClipBoardManager.setText(AnnotatedString(menuBtmSheetParam.folder.value.name))
                    coroutineScope.pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.Key.CopiedTitleToTheClipboard.getLocalizedString()
                        )
                    )
                },
                menuBtmSheetType = MenuBtmSheetType.Folder.RegularFolder,
                text = menuBtmSheetParam.folder!!.value.name.toString()
            )
            ItemDivider(
                colorOpacity = 0.25f, paddingValues = PaddingValues(start = 25.dp, end = 25.dp)
            )
            Spacer(Modifier.height(5.dp))
        }

        if (!isNoteBtnSelected.value) {
            commonMenuContent()
        } else {
            val note =
                if (menuBtmSheetLinkEntries().contains(menuBtmSheetParam.menuBtmSheetFor)) menuBtmSheetParam.link!!.value.note else menuBtmSheetParam.folder!!.value.note
            if (note.isNotEmpty()) {
                Text(
                    text = Localization.Key.SavedNote.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(20.dp)
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                        .combinedClickable(onClick = {}, onLongClick = {
                            localClipBoardManager.setText(
                                AnnotatedString(
                                    note
                                )
                            )
                            coroutineScope.pushUIEvent(
                                UIEvent.Type.ShowSnackbar(
                                    Localization.Key.CopiedNoteToTheClipboard.getLocalizedString()
                                )
                            )
                        }).padding(
                            start = 20.dp, end = 25.dp
                        ),
                    textAlign = TextAlign.Start,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Localization.Key.NoNoteAdded.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Start,
                        lineHeight = 24.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun MenuNonImageHeader(onClick: () -> Unit, menuBtmSheetType: MenuBtmSheetType, text: String) {
    Row(
        modifier = Modifier.combinedClickable(interactionSource = remember {
            MutableInteractionSource()
        }, indication = null, onClick = onClick).pulsateEffect().fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (menuBtmSheetType == MenuBtmSheetType.Folder.RegularFolder) Icons.Outlined.Folder else Icons.Outlined.Link,
            null,
            modifier = Modifier.padding(20.dp).size(28.dp)
        )

        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 16.sp,
            modifier = Modifier.padding(
                end = 20.dp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp
        )
    }
}