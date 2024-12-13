package com.sakethh.linkora.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SettingsInputSvideo
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sakethh.linkora.ui.navigation.NavigationRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val topAppBarScrollState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(topBar = {
        Column {
            LargeTopAppBar(scrollBehavior = topAppBarScrollState, title = {
                Text(
                    text = "Settings",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 24.sp
                )
            })
        }
    }) { it ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .nestedScroll(topAppBarScrollState.nestedScrollConnection)
        ) {
            items(settingsScreenOptions(navController)) {
                SettingSectionComponent(
                    SettingSectionComponentParam(
                        onClick = it.onClick,
                        sectionTitle = it.sectionTitle,
                        sectionIcon = it.sectionIcon,
                        shouldArrowIconAppear = it.shouldArrowIconAppear
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

private fun settingsScreenOptions(navController: NavController): List<SettingSectionComponentParam> {
    return listOf(
        SettingSectionComponentParam(
            onClick = {
                navController.navigate(NavigationRoute.ThemeSettingsScreen)
            },
            sectionTitle = "Theme",
            sectionIcon = Icons.Default.ColorLens
        ),
        SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = "General",
            sectionIcon = Icons.Default.SettingsInputSvideo
        ),
        SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = "Advanced",
            sectionIcon = Icons.Default.Build
        ),
        SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = "Layout",
            sectionIcon = Icons.Default.Dashboard
        ),
        SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = "Language",
            sectionIcon = Icons.Default.Language
        ),
        SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = "Data",
            sectionIcon = Icons.Default.Storage
        ),
        SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = "Privacy",
            sectionIcon = Icons.Default.PrivacyTip
        ),
        SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = "About",
            sectionIcon = Icons.Default.Info
        ),
        SettingSectionComponentParam(
            onClick = {

            },
            sectionTitle = "Acknowledgments",
            sectionIcon = Icons.Default.Group
        ),
    )
}