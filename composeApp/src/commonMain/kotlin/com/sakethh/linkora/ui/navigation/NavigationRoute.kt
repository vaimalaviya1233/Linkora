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
    data object SettingsScreen : NavigationRoute {
        override fun toString(): String {
            return "Settings"
        }
    }
}