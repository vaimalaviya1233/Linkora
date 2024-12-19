package com.sakethh.linkora.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationRoute {

    @Serializable
    sealed interface Root {
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
        data object SettingsScreen : Settings {
            override fun toString(): String = "Settings"
        }
    }



    @Serializable
    sealed interface Settings : NavigationRoute {

        @Serializable
        data object ThemeSettingsScreen : Settings {
            override fun toString(): String = "Theme"
        }

        @Serializable
        data object GeneralSettingsScreen : Settings {
            override fun toString(): String = "General"
        }

        @Serializable
        data object AdvancedSettingsScreen : Settings {
            override fun toString(): String = "Advanced"
        }

        @Serializable
        data object LayoutSettingsScreen : Settings {
            override fun toString(): String = "Layout"
        }

        @Serializable
        data object LanguageSettingsScreen : Settings {
            override fun toString(): String = "Language"
        }

        @Serializable
        data object DataSettingsScreen : Settings {
            override fun toString(): String = "Data"
        }

        @Serializable
        data object AboutSettingsScreen : Settings {
            override fun toString(): String = "About"
        }

        @Serializable
        data object AcknowledgementSettingsScreen : Settings {
            override fun toString(): String = "Acknowledgements"
        }

        sealed interface Data : Settings {
            @Serializable
            data object ServerSetupScreen : Data {
                override fun toString(): String = "Linkora Server Setup"
            }
        }
    }
}