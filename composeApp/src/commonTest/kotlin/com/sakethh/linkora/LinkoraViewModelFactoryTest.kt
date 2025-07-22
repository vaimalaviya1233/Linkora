package com.sakethh.linkora

import androidx.lifecycle.viewmodel.CreationExtras
import com.sakethh.linkora.di.LinkoraViewModelFactory
import com.sakethh.linkora.ui.AppVM
import junit.framework.TestCase.assertTrue
import org.junit.Test

class LinkoraViewModelFactoryTest {

    @Test
    fun `AppVM should be instantiated using LinkoraViewModelFactory`() {
        val appVM = LinkoraViewModelFactory.create(AppVM::class, CreationExtras.Empty)
        assertTrue(appVM is AppVM)
    }

}