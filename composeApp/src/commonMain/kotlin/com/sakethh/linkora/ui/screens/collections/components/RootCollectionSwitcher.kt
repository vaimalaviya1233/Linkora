package com.sakethh.linkora.ui.screens.collections.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sakethh.linkora.preferences.AppPreferences
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RootCollectionSwitcher(
    isRootContentSwitcherBtmSheetVisible: Boolean,
    rootContentSwitcherBtmSheetState: SheetState,
    onHide: () -> Unit,
    onSourceClick: (id: Int) -> Unit
) {
    if (isRootContentSwitcherBtmSheetVisible) {
        ModalBottomSheet(onDismissRequest = onHide, sheetState = rootContentSwitcherBtmSheetState) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select a collection source",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(15.dp)
                )
                remember {
                    listOf(0 to "Folders", 1 to "Tags")
                }.forEach { contentType ->
                    Row(modifier = Modifier.fillMaxWidth().clickable {
                        onSourceClick(contentType.first)
                    }.padding(5.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = contentType.first == AppPreferences.selectedCollectionSourceId,
                            onClick = {
                                onSourceClick(contentType.first)
                            })
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = contentType.second,
                            style = if (contentType.first == AppPreferences.selectedCollectionSourceId) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
                            color = if (contentType.first == AppPreferences.selectedCollectionSourceId) LocalContentColor.current else LocalContentColor.current.copy(
                                0.85f
                            )
                        )
                    }
                }
            }
        }
    }
}