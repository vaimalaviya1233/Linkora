package com.sakethh.linkora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.ui.PageKey
import com.sakethh.linkora.ui.domain.PaginationState
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TagSelectionComponent(
    paddingValues: PaddingValues = PaddingValues(start = 15.dp, end = 25.dp, top = 5.dp),
    allTags: PaginationState<Map<PageKey, List<Tag>>>,
    selectedTags: List<Tag>,
    onTagClick: (tag: Tag) -> Unit,
    onRetrieveNextTagsPage: () -> Unit,
    onFirstVisibleIndexChange: (Int) -> Unit
) {
    val lazyColumnState = rememberLazyListState()
    LaunchedEffect(Unit) {
        launch {
            snapshotFlow {
                lazyColumnState.canScrollForward
            }.debounce(500).distinctUntilChanged().collect {
                if (!it && !allTags.isRetrieving && !allTags.pagesCompleted) {
                    onRetrieveNextTagsPage()
                }
            }
        }

        launch {
            snapshotFlow {
                lazyColumnState.firstVisibleItemIndex
            }.debounce(500).distinctUntilChanged().collect {
                onFirstVisibleIndexChange(it)
            }
        }
    }
    LazyColumn(
        state = lazyColumnState, modifier = Modifier.padding(
            paddingValues
        ).heightIn(max = 300.dp).fillMaxWidth().clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surface).border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(0.15f),
                shape = RoundedCornerShape(25.dp)
            ).padding(start = 15.dp, end = 15.dp)
    ) {
        item {
            Spacer(Modifier.height(10.dp))
        }
        allTags.data.forEach { (pageKey, tags) ->
            items(items = tags, key = {
                "TagSelectionComponent_P$pageKey-${it.localId}"
            }) {
                val isTagSelected by rememberSaveable(selectedTags.contains(it)) {
                    mutableStateOf(selectedTags.contains(it))
                }
                AssistChip(
                    shape = RoundedCornerShape(15.dp),
                    colors = AssistChipDefaults.assistChipColors(containerColor = if (isTagSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = if (isTagSelected) MaterialTheme.colorScheme.secondaryContainer else LocalContentColor.current.copy(
                            0.25f
                        )
                    ),
                    onClick = {
                        onTagClick(it)
                    },
                    label = {
                        Text(
                            text = it.name,
                            color = if (isTagSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.secondary.copy(
                                0.85f
                            ),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isTagSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            tint = if (isTagSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.secondary.copy(
                                0.85f
                            ), imageVector = Icons.Default.Tag, contentDescription = null
                        )
                    },
                    modifier = Modifier.padding(top = 7.5.dp, bottom = 7.5.dp).fillMaxWidth()
                        .height(50.dp).pointerHoverIcon(icon = PointerIcon.Hand)
                )
            }
        }
        if (!allTags.pagesCompleted) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }
            }
        }
        item {
            Spacer(Modifier.height(10.dp))
        }
    }
}