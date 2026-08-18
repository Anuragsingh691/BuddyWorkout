package com.example.buddyworkout.core.navigation

import com.example.buddyworkout.core.ui.component.BwNavItem
import kotlinx.serialization.Serializable

// --- Auth ---------------------------------------------------------------
@Serializable data object Login
@Serializable data object Register

// --- Bottom-nav roots ---------------------------------------------------
@Serializable data object Home
@Serializable data object Challenges
@Serializable data object Profile

// --- Challenge flow -----------------------------------------------------
@Serializable data object Invite
@Serializable data class ChallengeDetail(val id: String)
@Serializable data class ChallengeInvite(val code: String)
@Serializable data class Record(val challengeId: String)
@Serializable data class Winner(val challengeId: String)

// --- Create flow: a nested graph so the three screens can later share one
// ViewModel scoped to CreateGraph. ---------------------------------------
@Serializable data object CreateGraph
@Serializable data object Create
@Serializable data object BuddyPicker
@Serializable data object DateTimePicker

// --- Development only ---------------------------------------------------
@Serializable data object Gallery

/** The destination each bottom-bar tab navigates to. */
fun BwNavItem.route(): Any = when (this) {
    BwNavItem.Home -> Home
    BwNavItem.Challenges -> Challenges
    BwNavItem.Profile -> Profile
}

/**
 * Which tab (if any) the bottom bar should highlight for the current
 * destination — `null` means the bar is hidden entirely.
 *
 * Navigation reports a destination's route as the fully-qualified class name of
 * its `@Serializable` type, with an argument pattern appended for routes that
 * take parameters. Exact equality is therefore enough: the three roots take no
 * parameters.
 */
fun bottomBarTabFor(routeName: String?): BwNavItem? = when (routeName) {
    Home::class.qualifiedName -> BwNavItem.Home
    Challenges::class.qualifiedName -> BwNavItem.Challenges
    Profile::class.qualifiedName -> BwNavItem.Profile
    else -> null
}
