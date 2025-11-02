package com.sakethh.linkora.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sakethh.linkora.domain.model.tag.Tag

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelectionComponent(
    paddingValues: PaddingValues = PaddingValues(start = 15.dp, end = 25.dp, top = 5.dp),
    allTags: List<Tag>,
    selectedTags: List<Tag>,
    onTagClick: (tag: Tag) -> Unit,
    onCreateTagClick: () -> Unit
) {
    FlowRow(
        modifier = Modifier.padding(
            paddingValues
        ).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconButton(
            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
            onClick = onCreateTagClick
        ) {
            Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
        }
        allTags.forEach {
            val isTagSelected by rememberSaveable(selectedTags.contains(it)) {
                mutableStateOf(selectedTags.contains(it))
            }
            key("TAG_ID_${it.localId}") {
                AssistChip(
                    colors = AssistChipDefaults.assistChipColors(containerColor = if (isTagSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = if (isTagSelected) MaterialTheme.colorScheme.secondaryContainer else LocalContentColor.current
                    ),
                    onClick = {
                        onTagClick(it)
                    },
                    label = {
                        Text(
                            text = it.name,
                            color = if (isTagSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isTagSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            tint = if (isTagSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.secondary,
                            imageVector = Icons.Default.Tag,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                )
            }
        }
    }
}