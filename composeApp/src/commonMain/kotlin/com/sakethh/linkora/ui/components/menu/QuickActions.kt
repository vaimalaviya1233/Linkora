package com.sakethh.linkora.ui.components.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent

@Composable
fun QuickActions(onForceOpenInExternalBrowserClicked: () -> Unit, webUrl: String) {
    val localURIHandler = LocalUriHandler.current
    val localClipBoardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    Column {
        HorizontalDivider(Modifier.padding(start = 15.dp, end = 15.dp, bottom = 15.dp, top = 5.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilledTonalIconButton(onClick = {
                onForceOpenInExternalBrowserClicked()
            }) {
                Icon(
                    imageVector = Icons.Outlined.OpenInBrowser,
                    contentDescription = null
                )
            }
            FilledTonalIconButton(onClick = {
                localClipBoardManager.setText(
                    AnnotatedString(webUrl)
                )
                coroutineScope.pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.CopiedLinkToClipboard.getLocalizedString()))
            }) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null
                )

            }
            FilledTonalIconButton(onClick = {

            }) {
                Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
            }
        }
        Spacer(Modifier.height(15.dp))
    }
}