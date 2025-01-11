package com.sakethh.linkora.ui.screens.home.panels

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ViewArray
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.menu.IndividualMenuComponent
import com.sakethh.linkora.ui.screens.home.HomeScreenVM
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.linkora.ui.utils.rememberDeserializableMutableObject
import com.sakethh.platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelsManagerScreen() {
    val navController = LocalNavController.current
    val topAppBarState = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val homeScreenVM: HomeScreenVM = viewModel(factory = genericViewModelFactory {
        HomeScreenVM(
            panelsRepo = DependencyContainer.panelsRepo.value,
            triggerCollectionOfPanels = true,
            triggerCollectionOfPanelFolders = false
        )
    })
    val panels = homeScreenVM.panels.collectAsStateWithLifecycle()
    val selectedPanel = rememberDeserializableMutableObject {
        mutableStateOf(Panel(panelId = -1, panelName = ""))
    }
    val specificPanelManagerScreenVM: SpecificPanelManagerScreenVM =
        viewModel(factory = genericViewModelFactory {
            SpecificPanelManagerScreenVM(
                foldersRepo = DependencyContainer.localFoldersRepo.value,
                panelsRepo = DependencyContainer.panelsRepo.value,
                initData = false
            )
        })
    Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
        BottomAppBar(
            modifier = Modifier.fillMaxWidth(if (platform() is Platform.Android.Mobile) 1f else 0.5f)
        ) {
            Button(
                modifier = Modifier.padding(15.dp).navigationBarsPadding().fillMaxWidth()
                    .pulsateEffect(0.9f), onClick = {

                }) {
                Text(
                    text = Localization.Key.AddANewPanel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
        }
    }, topBar = {
        Column {
            MediumTopAppBar(navigationIcon = {
                IconButton(onClick = {
                    navController.navigateUp()
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                }
            }, scrollBehavior = topAppBarState, title = {
                Text(
                    text = Localization.Key.Panels.rememberLocalizedString(),
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                )
            })
            HorizontalDivider()
        }
    }) {
        Row(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.padding(it)
                    .fillMaxWidth(if (platform() is Platform.Android.Mobile) 1f else 0.5f)
                    .animateContentSize().nestedScroll(topAppBarState.nestedScrollConnection)
            ) {
                items(panels.value.drop(1)) { panel ->
                    IndividualMenuComponent(
                        onOptionClick = { ->
                            SpecificPanelManagerScreenVM.updateSelectedPanelData(panel)
                            specificPanelManagerScreenVM.updateSpecificPanelManagerScreenData()
                            selectedPanel.value = panel
                        },
                        elementName = panel.panelName,
                        elementImageVector = Icons.Default.ViewArray,
                        inPanelsScreen = true,
                        isSelected = selectedPanel.value.panelId == panel.panelId
                    )
                }
            }
            VerticalDivider()
            if (selectedPanel.value.panelId <= 0) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Localization.Key.SelectAPanel.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                SpecificPanelManagerScreen(
                    paddingValues = it,
                    specificPanelManagerScreenVM = specificPanelManagerScreenVM
                )
            }
        }
    }
}