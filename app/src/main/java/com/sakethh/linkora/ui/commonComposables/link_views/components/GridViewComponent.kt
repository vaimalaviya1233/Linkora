package com.sakethh.linkora.ui.commonComposables.link_views.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.ui.commonComposables.link_views.LinkUIComponentParam
import com.sakethh.linkora.ui.commonComposables.pulsateEffect
import com.sakethh.linkora.ui.screens.CoilImage
import com.sakethh.linkora.ui.screens.settings.SettingsPreference
import com.sakethh.linkora.utils.fadedEdges

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridViewComponent(linkUIComponentParam: LinkUIComponentParam, forStaggeredView: Boolean) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        Modifier
            .fillMaxWidth()
            .then(if (!forStaggeredView) Modifier.wrapContentHeight() else Modifier)
            .combinedClickable(onClick = {
                linkUIComponentParam.onLinkClick()
            }, interactionSource = remember {
                MutableInteractionSource()
            }, indication = null, onLongClick = {
                linkUIComponentParam.onLongClick()
            })
            .pulsateEffect()
            .padding(4.dp)
            .clip(RoundedCornerShape(5.dp))
            .then(
                if (SettingsPreference.enableBordersForNonListViews.value) Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(0.5f),
                    shape = RoundedCornerShape(5.dp)
                )
                else Modifier
            )
            .then(
                if (linkUIComponentParam.isSelectionModeEnabled.value) Modifier.background(
                    colorScheme.primaryContainer
                ) else Modifier
            )
            .animateContentSize()
        ) {
            if (linkUIComponentParam.isItemSelected.value) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = colorScheme.onPrimary)
                }
            } else if (linkUIComponentParam.imgURL.trim().isNotBlank()) {
                Box(modifier = if (forStaggeredView) Modifier.fillMaxSize() else Modifier.height(150.dp)) {
                    CoilImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .fadedEdges(colorScheme),
                        imgURL = linkUIComponentParam.imgURL,
                        contentScale = if (linkUIComponentParam.imgURL.startsWith("https://pbs.twimg.com/profile_images/") || !SettingsPreference.isShelfMinimizedInHomeScreen.value || !forStaggeredView) ContentScale.Crop else ContentScale.Fit
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = linkUIComponentParam.title,
                        modifier = Modifier.padding(
                            start = 10.dp,
                            top = 10.dp,
                            end = 10.dp,
                            bottom = if (linkUIComponentParam.isSelectionModeEnabled.value) 10.dp else 0.dp
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        overflow = TextOverflow.Ellipsis, fontSize = 12.sp
                    )
                }
            }
        if (!linkUIComponentParam.isSelectionModeEnabled.value) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 5.dp)
            ) {
                Box(Modifier.fillMaxWidth(0.75f)) {
                    Text(
                        text = linkUIComponentParam.webBaseURL.replace("www.", "")
                            .replace("http://", "").replace("https://", ""),
                        modifier = Modifier
                            .padding(
                                start = 10.dp
                            )
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(0.25f),
                                shape = RoundedCornerShape(5.dp)
                            )
                            .padding(5.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = {
                    linkUIComponentParam.onMoreIconClick()
                }) {
                    Icon(Icons.Default.MoreVert, null)
                }
            }
        }
        }
}