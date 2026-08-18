package com.example.buddyworkout.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.example.buddyworkout.BuildConfig
import com.example.buddyworkout.core.ui.component.BwBottomNav
import com.example.buddyworkout.core.ui.gallery.ComponentGallery
import com.example.buddyworkout.core.ui.theme.BwColors

/** Host of the App Links used for challenge invites. */
private const val INVITE_BASE_PATH = "https://commworkout.app/c"

@Composable
fun BuddyWorkoutNavHost(
    startDestination: Any,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = bottomBarTabFor(backStackEntry?.destination?.route)

    Scaffold(
        modifier = modifier,
        containerColor = BwColors.Bg,
        bottomBar = {
            if (selectedTab != null) {
                BwBottomNav(
                    selected = selectedTab,
                    onSelect = { item ->
                        navController.navigate(item.route()) {
                            popUpTo(Home) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            // --- Auth ---------------------------------------------------
            composable<Login> {
                PlaceholderScreen(
                    title = "Sign in",
                    actions = listOf(
                        "Sign in" to { navController.navigate(Home) { popUpTo(0) } },
                        "Create an account" to { navController.navigate(Register) },
                    ),
                )
            }
            composable<Register> {
                PlaceholderScreen(
                    title = "Create account",
                    onBack = { navController.popBackStack() },
                    actions = listOf(
                        "Register" to { navController.navigate(Home) { popUpTo(0) } },
                    ),
                )
            }

            // --- Bottom-nav roots ---------------------------------------
            composable<Home> {
                PlaceholderScreen(
                    title = "Home",
                    actions = listOf(
                        "Create challenge" to { navController.navigate(CreateGraph) },
                        "Invite buddies" to { navController.navigate(Invite) },
                        "Open a challenge" to { navController.navigate(ChallengeDetail("demo")) },
                    ),
                )
            }
            composable<Challenges> {
                PlaceholderScreen(
                    title = "Challenges",
                    actions = listOf(
                        "Open a challenge" to { navController.navigate(ChallengeDetail("demo")) },
                    ),
                )
            }
            composable<Profile> {
                PlaceholderScreen(
                    title = "Profile",
                    actions = buildList {
                        add("Sign out" to { navController.navigate(Login) { popUpTo(0) } })
                        if (BuildConfig.DEBUG) {
                            add("Component gallery" to { navController.navigate(Gallery) })
                        }
                    },
                )
            }

            // --- Challenge flow -----------------------------------------
            composable<Invite> {
                PlaceholderScreen(
                    title = "Invite buddies",
                    onBack = { navController.popBackStack() },
                )
            }
            composable<ChallengeDetail> { entry ->
                val route = entry.toRoute<ChallengeDetail>()
                PlaceholderScreen(
                    title = "Challenge ${route.id}",
                    onBack = { navController.popBackStack() },
                    actions = listOf(
                        "Record workout" to { navController.navigate(Record(route.id)) },
                        "See winner" to { navController.navigate(Winner(route.id)) },
                    ),
                )
            }
            composable<ChallengeInvite>(
                deepLinks = listOf(navDeepLink<ChallengeInvite>(basePath = INVITE_BASE_PATH)),
            ) { entry ->
                val route = entry.toRoute<ChallengeInvite>()
                PlaceholderScreen(
                    title = "Invite ${route.code}",
                    actions = listOf(
                        "Accept & join" to { navController.navigate(ChallengeDetail(route.code)) },
                        "Not now" to { navController.navigate(Home) { popUpTo(0) } },
                    ),
                )
            }
            composable<Record> {
                PlaceholderScreen(
                    title = "Record",
                    onBack = { navController.popBackStack() },
                    actions = listOf("Stop & save" to { navController.popBackStack() }),
                )
            }
            composable<Winner> { entry ->
                val route = entry.toRoute<Winner>()
                PlaceholderScreen(
                    title = "Winner — ${route.challengeId}",
                    actions = listOf(
                        "Back to home" to { navController.navigate(Home) { popUpTo(0) } },
                    ),
                )
            }

            // --- Create flow (nested graph) -----------------------------
            navigation<CreateGraph>(startDestination = Create) {
                composable<Create> {
                    PlaceholderScreen(
                        title = "New challenge",
                        onBack = { navController.popBackStack() },
                        actions = listOf(
                            "Add buddies" to { navController.navigate(BuddyPicker) },
                            "Set start & end" to { navController.navigate(DateTimePicker) },
                            "Create" to {
                                navController.navigate(ChallengeDetail("new")) {
                                    popUpTo(CreateGraph) { inclusive = true }
                                }
                            },
                        ),
                    )
                }
                composable<BuddyPicker> {
                    PlaceholderScreen(
                        title = "Add buddies",
                        onBack = { navController.popBackStack() },
                        actions = listOf("Done" to { navController.popBackStack() }),
                    )
                }
                composable<DateTimePicker> {
                    PlaceholderScreen(
                        title = "Start & end",
                        onBack = { navController.popBackStack() },
                        actions = listOf("Save" to { navController.popBackStack() }),
                    )
                }
            }

            // --- Development only ---------------------------------------
            composable<Gallery> { ComponentGallery() }
        }
    }
}
