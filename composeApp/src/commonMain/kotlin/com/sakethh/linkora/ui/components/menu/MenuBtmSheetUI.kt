package com.sakethh.linkora.ui.components.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PinEnd
import androidx.compose.runtime.Composable


@Composable
fun MenuBtmSheetUI() {

    Column {
        repeat(6) {
            IndividualMenuComponent(
                onOptionClick = { -> },
                elementName = "Gladys",
                elementImageVector = Icons.Default.PinEnd,
                inPanelsScreen = false
            )
        }
    }
}



