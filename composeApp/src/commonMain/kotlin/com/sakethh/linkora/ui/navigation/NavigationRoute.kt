package com.sakethh.linkora.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationRoute {

    @Serializable
    data object HomeScreen : NavigationRoute {
        override fun toString(): String {
            return "Home"
        }
    }

    @Serializable
    data object SearchScreen : NavigationRoute {
        override fun toString(): String {
            return "Search"
        }
    }

    @Serializable
    data object CollectionsScreen : NavigationRoute {
        override fun toString(): String {
            return "Collections"
        }
    }

    @Serializable
    sealed class SettingsScreen : NavigationRoute {
        override fun toString(): String {
            return "Settings"
        }

        data object ThemeSettingsScreen : SettingsScreen() {
            override fun toString(): String {
                return "Theme"
            }
        }

        data object GeneralSettingsScreen : SettingsScreen() {
            override fun toString(): String {
                return "General"
            }
        }

        data object AdvancedSettingsScreen : SettingsScreen() {
            override fun toString(): String {
                return "Advanced"
            }
        }

        data object LayoutSettingsScreen : SettingsScreen() {
            override fun toString(): String {
                return "Layout"
            }
        }

        data object LanguageSettingsScreen : SettingsScreen() {
            override fun toString(): String {
                return "Language"
            }
        }

        data object DataSettingsScreen : SettingsScreen() {
            override fun toString(): String {
                return "Data"
            }
        }

        data object AboutSettingsScreen : SettingsScreen() {
            override fun toString(): String {
                return "About"
            }
        }

        data object AcknowledgementSettingsScreen : SettingsScreen() {
            override fun toString(): String {
                return "Acknowledgement"
            }
        }
    }
}