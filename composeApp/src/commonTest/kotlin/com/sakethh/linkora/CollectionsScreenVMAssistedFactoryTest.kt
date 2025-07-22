package com.sakethh.linkora

import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavController
import com.sakethh.linkora.di.CollectionScreenVMAssistedFactory
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue

class CollectionsScreenVMAssistedFactoryTest {

    @Test
    fun `createForApp should initialize collectionDetailPaneInfo as null`() {
        val collectionsScreenVM = CollectionScreenVMAssistedFactory.createForApp()
            .create(CollectionsScreenVM::class, CreationExtras.Empty)

        assertTrue(collectionsScreenVM.collectionDetailPaneInfo == null)
    }


    @Test
    fun `createForCollectionDetailPane should initialize collectionDetailPaneInfo with a default value`() {

        val navController = mockk<NavController>()

        val collectionsScreenVM = CollectionScreenVMAssistedFactory.createForCollectionDetailPane(
                Platform.Desktop,
                navController = navController
            ).create(CollectionsScreenVM::class, CreationExtras.Empty)

        assertTrue(collectionsScreenVM.collectionDetailPaneInfo != null)
    }

}