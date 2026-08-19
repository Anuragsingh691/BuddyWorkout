package com.example.buddyworkout.core.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

/** Peak opacity of the black wash over a screen that has been pushed under. */
private const val SCRIM_ALPHA = 0.14f

/**
 * What the current transition is doing, from the point of view of the screen
 * being covered.
 *
 * @param isPop the stack is unwinding, so the screen underneath is being
 *   revealed rather than covered.
 * @param dims this move has a screen underneath at all. False between bottom-nav
 *   tabs, which cross-fade as siblings — a scrim there reads as a dark flash.
 */
@Immutable
data class NavMotion(val isPop: Boolean, val dims: Boolean)

/**
 * Classifies each destination change as a push or a pop.
 *
 * Back-stack *depth* is the tempting signal and it is wrong: sign-in and the
 * winner screen both `navigate(Home) { popUpTo(0) }`, which shrinks the stack
 * while being a push. Entry identity is exact instead — Navigation reuses a
 * [NavBackStackEntry] when unwinding to it and mints a fresh one otherwise, so
 * an id seen before means a pop.
 */
class NavMotionTracker {
    private val seen = LinkedHashSet<String>()
    private var previousRoute: String? = null

    fun onDestination(id: String?, route: String?): NavMotion {
        val isPop = id != null && !seen.add(id)
        if (seen.size > MAX_TRACKED) seen.iterator().let { it.next(); it.remove() }
        val motion = NavMotion(isPop = isPop, dims = !isTabSwitch(previousRoute, route))
        previousRoute = route
        return motion
    }

    private companion object {
        /** Far deeper than any real back stack; only bounds a long session. */
        const val MAX_TRACKED = 64
    }
}

@Composable
fun rememberNavMotion(navController: NavHostController): NavMotion {
    val entry by navController.currentBackStackEntryAsState()
    val tracker = remember { NavMotionTracker() }
    // Keyed on the entry so this resolves during composition — a LaunchedEffect
    // would land after the destinations have already composed and read a stale
    // direction for the transition that is starting.
    return remember(entry?.id) {
        tracker.onDestination(entry?.id, entry?.destination?.route)
    }
}

/**
 * Registers a destination whose content darkens while it sits under another.
 *
 * The scrim cannot come from the [NavTransitions] lambdas: Navigation exposes
 * only `EnterTransition`/`ExitTransition`, which move and fade a screen but
 * cannot paint over it. It has to live inside the destination's own content.
 */
inline fun <reified T : Any> NavGraphBuilder.scrimmed(
    motion: NavMotion,
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) = composable<T>(deepLinks = deepLinks) { entry ->
    NavScrim(motion) { content(entry) }
}

/**
 * Washes [content] with black at whichever end of the transition leaves this
 * screen underneath the one above it.
 *
 * [EnterExitState] alone cannot tell a push from a pop — a screen sliding under
 * and a screen sliding off to the right both run `Visible -> PostExit` — which
 * is why [NavMotion.isPop] has to be threaded in from the nav controller.
 */
@Composable
fun AnimatedVisibilityScope.NavScrim(
    motion: NavMotion,
    content: @Composable () -> Unit,
) {
    if (!motion.dims) {
        content()
        return
    }

    val covered = if (motion.isPop) EnterExitState.PreEnter else EnterExitState.PostExit
    val alpha = transition.animateFloat(
        transitionSpec = { tween(durationMillis = PUSH_MILLIS, easing = PushEasing) },
        label = "navScrim",
    ) { state -> if (state == covered) SCRIM_ALPHA else 0f }

    Box(
        Modifier.drawWithContent {
            drawContent()
            // Read inside the draw lambda so the animation stays a draw-phase
            // change instead of recomposing the screen 60 times a second.
            if (alpha.value > 0f) drawRect(Color.Black, alpha = alpha.value)
        },
    ) {
        content()
    }
}
