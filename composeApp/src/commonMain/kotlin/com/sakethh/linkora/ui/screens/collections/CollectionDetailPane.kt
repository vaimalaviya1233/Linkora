package com.sakethh.linkora.ui.screens.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.ui.components.link.LinkListItemComposable
import com.sakethh.linkora.ui.domain.Layout
import com.sakethh.linkora.ui.domain.model.LinkUIComponentParam

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CollectionDetailPane(
    folder: Folder,
    paneNavigator: ThreePaneScaffoldNavigator<Folder>,
    collectionsScreenVM: CollectionsScreenVM
) {
    val links = collectionsScreenVM.links.collectAsStateWithLifecycle()
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        Column {
            TopAppBar(actions = {}, navigationIcon = {
                IconButton(onClick = {
                    paneNavigator.navigateBack()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }, title = {
                Text(
                    text = folder.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp
                )
            })
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.25f))
        }
    }) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(links.value) {
                LinkListItemComposable(
                    linkUIComponentParam = LinkUIComponentParam(
                        link = it,
                        isSelectionModeEnabled = mutableStateOf(false),
                        onMoreIconClick = {

                        },
                        onLinkClick = {

                        },
                        onForceOpenInExternalBrowserClicked = {

                        },
                        isItemSelected = mutableStateOf(false),
                        onLongClick = {

                        }
                    ),
                    forTitleOnlyView = AppPreferences.currentlySelectedLinkLayout.value == Layout.TITLE_ONLY_LIST_VIEW.name)
            }
        }
    }
}