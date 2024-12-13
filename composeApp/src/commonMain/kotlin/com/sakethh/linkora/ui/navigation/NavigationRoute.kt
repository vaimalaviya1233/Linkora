package com.sakethh.linkora.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationRoute {

    @Serializable
    data object HomeScreen : NavigationRoute {
        override fun toString(): String = "Home"
    }

    @Serializable
    data object SearchScreen : NavigationRoute {
        override fun toString(): String = "Search"
    }

    @Serializable
    data object CollectionsScreen : NavigationRoute {
        override fun toString(): String = "Collections"
    }

    @Serializable
    data object SettingsScreen : NavigationRoute {
        override fun toString(): String = "Settings"
    }

    @Serializable
    data object ThemeSettingsScreen : NavigationRoute {
        override fun toString(): String = "Theme"
    }

    @Serializable
    data object GeneralSettingsScreen : NavigationRoute {
        override fun toString(): String = "General"
    }

    @Serializable
    data object AdvancedSettingsScreen : NavigationRoute {
        override fun toString(): String = "Advanced"
    }

    @Serializable
    data object LayoutSettingsScreen : NavigationRoute {
        override fun toString(): String = "Layout"
    }

    @Serializable
    data object LanguageSettingsScreen : NavigationRoute {
        override fun toString(): String = "Language"
    }

    @Serializable
    data object DataSettingsScreen : NavigationRoute {
        override fun toString(): String = "Data"
    }

    @Serializable
    data object AboutSettingsScreen : NavigationRoute {
        override fun toString(): String = "About"
    }

    @Serializable
    data object AcknowledgementSettingsScreen : NavigationRoute {
        override fun toString(): String = "Acknowledgement"
    }
}