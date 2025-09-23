package com.sakethh.linkora.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.Localization
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.linkora.utils.bottomNavPaddingAcrossPlatforms
import com.sakethh.linkora.utils.rememberLocalizedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateATagBtmSheet(
    sheetState: SheetState,
    showBtmSheet: Boolean,
    onCancel: () -> Unit,
    onCreateClick: (tagName: String) -> Unit
) {
    val focusRequester = remember {
        FocusRequester()
    }
    if (showBtmSheet) {
        var newTag by rememberSaveable {
            mutableStateOf("")
        }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        ModalBottomSheet(sheetState = sheetState, onDismissRequest = onCancel) {
            Column {
                Text(
                    text = "Create A Tag",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(start = 15.dp)
                )
                TextField(
                    label = {
                        Text(
                            text = "Tag Name", style = MaterialTheme.typography.titleSmall
                        )
                    },
                    textStyle = MaterialTheme.typography.titleSmall,
                    value = newTag,
                    onValueChange = {
                        newTag = it
                    },
                    modifier = Modifier.fillMaxWidth().padding(15.dp)
                        .focusRequester(focusRequester = focusRequester)
                )
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.pulsateEffect().fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp)
                ) {
                    Text(
                        text = Localization.Key.Cancel.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth().pulsateEffect()
                        .padding(start = 15.dp, end = 15.dp, bottom = 5.dp)
                        .bottomNavPaddingAcrossPlatforms(), onClick = {
                        onCreateClick(newTag)
                    }) {
                    Text(
                        text = "Create",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}