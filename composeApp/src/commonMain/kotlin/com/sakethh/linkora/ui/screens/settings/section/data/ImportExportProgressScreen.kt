package com.sakethh.linkora.ui.screens.settings.section.data

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.rememberLocalizedString

@Composable
fun ImportExportProgressScreen(
    isVisible: MutableState<Boolean>,
    dataSettingsScreenVM: DataSettingsScreenVM,
    operationTitle: String
) {
    val logsListState = rememberLazyListState()
    if (isVisible.value) {
        Scaffold(topBar = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(15.dp)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Text(
                    text = operationTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = Localization.Key.ImportExportScreenTopAppBarDesc.rememberLocalizedString(),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    style = MaterialTheme.typography.titleSmall,
                )
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(top = 15.dp, bottom = 15.dp)
                )
            }
        }, bottomBar = {
            BottomAppBar(modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(modifier = Modifier.fillMaxWidth().padding(15.dp), onClick = {
                    dataSettingsScreenVM.cancelImportExportJob()
                }) {
                    Text(
                        text = Localization.Key.Cancel.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }) {
            Box(
                modifier = Modifier.padding(it)
                    .clickable(onClick = {}, indication = null, interactionSource = remember {
                        MutableInteractionSource()
                    }).fillMaxSize()
                    .padding(start = 15.dp, end = 15.dp, bottom = 15.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                LazyColumn(
                    userScrollEnabled = false,
                    modifier = Modifier.fillMaxSize(),
                    state = logsListState
                ) {
                    items(dataSettingsScreenVM.importExportProgressLogs) {
                        Text(text = it, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        LaunchedEffect(logsListState.canScrollForward) {
            if (logsListState.canScrollForward) {
                logsListState.animateScrollToItem(logsListState.layoutInfo.totalItemsCount - 1)
            }
        }
    }
}