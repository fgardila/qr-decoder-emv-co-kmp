package dev.code93.emvqr.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Scanner : Route

    @Serializable
    data object CameraScanner : Route

    @Serializable
    data object Generate : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object Licenses : Route
}
