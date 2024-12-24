package com.sakethh.linkora.ui.screens.settings.section.data.sync.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sakethh.linkora.ui.navigation.NavigationRoute
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerManagementScreen(navController: NavController) {
    SettingsSectionScaffold(
        topAppBarText = NavigationRoute.Settings.Data.ServerManagementScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                .navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {

        }
    }
}