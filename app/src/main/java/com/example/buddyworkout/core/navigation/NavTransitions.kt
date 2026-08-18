package com.example.buddyworkout.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/** Duration of a push or pop, matching UIKit's navigation controller. */
private const val PUSH_MILLIS = 350

/** Duration of the cross-fade between bottom-nav tabs. */
private const val FADE_MILLIS = 150

/**
 * How far the outgoing screen travels while the incoming one covers the full
 * width. The mismatch is the parallax that makes the stack read as layered
 * rather than as two cards sliding past each other.
 */
private const val PARALLAX = 0.25f

/** UIKit's push curve: eases in gently, decelerates hard at the end. */
private val PushEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private val PushSpec = tween<IntOffset>(
    durationMillis = PUSH_MILLIS,
    easing = PushEasing,
)

private val FadeSpec = tween<Float>(durationMillis = FADE_MILLIS)

/**
 * Whether a move between [from] and [to] is a hop between bottom-nav tabs.
 *
 * The three tab roots are siblings, not a stack, so they cross-fade instead of
 * sliding — a horizontal push there would imply a hierarchy that does not
 * exist. The check has to run on the pop transitions too: the bottom bar
 * navigates with `popUpTo(Home) { saveState = true }`, so returning to an
 * earlier tab is dispatched as a pop and would otherwise slide backwards.
 */
fun isTabSwitch(from: String?, to: String?): Boolean =
    bottomBarTabFor(from) != null && bottomBarTabFor(to) != null

private val AnimatedContentTransitionScope<NavBackStackEntry>.movesBetweenTabs: Boolean
    get() = isTabSwitch(initialState.destination.route, targetState.destination.route)

/**
 * iOS-style navigation transitions for the whole graph.
 *
 * These belong on the [androidx.navigation.compose.NavHost] rather than on
 * individual destinations, because a transition is a *pair* of screens: putting
 * a fade on `Home` would also fade Home out when pushing a detail screen from
 * it. Each lambda instead inspects both ends of the move.
 */
object NavTransitions {

    /** Incoming screen enters from the right edge. */
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (movesBetweenTabs) {
            fadeIn(FadeSpec)
        } else {
            slideInHorizontally(PushSpec) { width -> width }
        }
    }

    /** Outgoing screen slides only part of the way — see [PARALLAX]. */
    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (movesBetweenTabs) {
            fadeOut(FadeSpec)
        } else {
            slideOutHorizontally(PushSpec) { width -> -(width * PARALLAX).toInt() }
        }
    }

    /** The screen underneath returns from its parked parallax offset. */
    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (movesBetweenTabs) {
            fadeIn(FadeSpec)
        } else {
            slideInHorizontally(PushSpec) { width -> -(width * PARALLAX).toInt() }
        }
    }

    /** The top screen slides back off the right edge. */
    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (movesBetweenTabs) {
            fadeOut(FadeSpec)
        } else {
            slideOutHorizontally(PushSpec) { width -> width }
        }
    }
}
