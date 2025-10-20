package com.sakethh.linkora.ui.screens.collections.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.domain.ComposableContent
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.platform.platform
import com.sakethh.linkora.ui.utils.pressScaleEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagRenameComponent(
    showComponent: Boolean,
    sheetState: SheetState,
    existingName: String,
    onHide: () -> Unit,
    onSave: (newName: String) -> Unit
) {
    var newTagName by rememberSaveable(existingName) {
        mutableStateOf(existingName)
    }
    val tagFieldFocusRequester = remember {
        FocusRequester()
    }
    val content: ComposableContent = {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Rename Tag Name",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp,
                modifier = Modifier.padding(start = 15.dp)
            )
            TextField(
                value = newTagName,
                textStyle = MaterialTheme.typography.titleSmall,
                onValueChange = {
                    newTagName = it
                },
                modifier = Modifier.padding(15.dp).fillMaxWidth()
                    .focusRequester(tagFieldFocusRequester),
                label = {
                    Text(text = "New tag name", style = MaterialTheme.typography.titleSmall)
                })
            Button(
                modifier = Modifier.pressScaleEffect().pointerHoverIcon(icon = PointerIcon.Hand).fillMaxWidth().padding(start = 15.dp, end = 15.dp), onClick = {
                    onSave(newTagName)
                }) {
                Text(text = "Update", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    val platform = platform()
    if (showComponent) {
        if (platform is Platform.Android.Mobile) {
            ModalBottomSheet(sheetState = sheetState, onDismissRequest = onHide) {
                content()
            }
        } else {
            AlertDialog(onDismissRequest = onHide) {
                content()
            }
        }
        LaunchedEffect(Unit) {
            tagFieldFocusRequester.requestFocus()
        }
    }
}