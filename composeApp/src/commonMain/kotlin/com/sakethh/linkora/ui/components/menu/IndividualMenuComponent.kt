package com.sakethh.linkora.ui.components.menu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.ui.utils.pulsateEffect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IndividualMenuComponent(
    onOptionClick: () -> Unit,
    elementName: String,
    elementImageVector: ImageVector,
    inPanelsScreen: Boolean = false
) {
    Row(
        modifier = Modifier
            .combinedClickable(
                interactionSource = remember {
                    MutableInteractionSource()
                }, indication = null,
                onClick = {
                    onOptionClick()
                },
                onLongClick = {

                })
            .pulsateEffect()
            .padding(end = 10.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier.padding(10.dp), onClick = { onOptionClick() },
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) {
                Icon(imageVector = elementImageVector, contentDescription = null)
            }
            Text(
                text = elementName,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(if (inPanelsScreen) 0.4f else 1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}