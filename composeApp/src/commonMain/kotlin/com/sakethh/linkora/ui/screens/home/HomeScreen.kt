package com.sakethh.linkora.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sakethh.linkora.common.Localization

@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column {
            Text(text = Localization.getLocalizedString(Localization.Key.Theme))
            Text(text = Localization.rememberLocalizedString(Localization.Key.Theme))
        }
    }
}