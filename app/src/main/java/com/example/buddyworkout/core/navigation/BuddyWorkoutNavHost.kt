package com.example.buddyworkout.core.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.feature.auth.LoginScreen
import com.example.buddyworkout.feature.auth.RegisterScreen
import com.example.buddyworkout.feature.challenge.ChallengeDetailScreen
import com.example.buddyworkout.feature.challenge.ChallengesScreen
import com.example.buddyworkout.feature.challenge.create.BuddyPickerScreen
import com.example.buddyworkout.feature.challenge.create.CreateChallengeScreen
import com.example.buddyworkout.feature.challenge.create.MAX_BUDDIES
import com.example.buddyworkout.feature.challenge.create.DateTimePickerScreen
import com.example.buddyworkout.feature.home.HomeScreen
import com.example.buddyworkout.feature.invite.ChallengeInviteScreen
import com.example.buddyworkout.feature.invite.InviteScreen
import com.example.buddyworkout.feature.profile.ProfileScreen
import com.example.buddyworkout.feature.record.RecordScreen
import com.example.buddyworkout.feature.result.WinnerScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Host of the App Links used for challenge invites. */
private const val INVITE_BASE_PATH = "https://commworkout.app/c"

/**
 * The app's single navigation graph.
 *
 * Screens are fed from [PreviewData] because no ViewModels exist yet. Each
 * `PreviewData.x` below is one call site to flip to an `XRoute(...)` wrapper
 * when its feature gets wired to Firebase — the screens themselves do not
 * change.
 */
@Composable
fun BuddyWorkoutNavHost(
    startDestination: Any,
    modifier: Modifier = Modifier,
    newIntents: Flow<Intent> = emptyFlow(),
    navController: NavHostController = rememberNavController(),
) {
    // Deep links delivered to the running Activity (see MainActivity.onNewIntent).
    LaunchedEffect(navController) {
        newIntents.collect { navController.handleDeepLink(it) }
    }

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
                LoginScreen(
                    state = PreviewData.login,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignIn = { navController.navigate(Home) { popUpTo(0) } },
                    onGoogleSignIn = { navController.navigate(Home) { popUpTo(0) } },
                    onRegisterClick = { navController.navigate(Register) },
                )
            }
            composable<Register> {
                RegisterScreen(
                    state = PreviewData.register,
                    onNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onCreateAccount = { navController.navigate(Home) { popUpTo(0) } },
                    onSignInClick = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }

            // --- Bottom-nav roots ---------------------------------------
            composable<Home> {
                HomeScreen(
                    state = PreviewData.home,
                    onCreateChallenge = { navController.navigate(CreateGraph) },
                    onInviteBuddies = { navController.navigate(Invite) },
                    onChallengeClick = { id -> navController.navigate(ChallengeDetail(id)) },
                    onNotifications = {},
                )
            }
            composable<Challenges> {
                // Temporary UI-local state so the tabs actually switch before a
                // ViewModel exists. The ViewModel will own this.
                var tab by rememberSaveable { mutableIntStateOf(0) }
                ChallengesScreen(
                    state = PreviewData.challengesTab.copy(selectedTab = tab),
                    onTabSelect = { tab = it },
                    onChallengeClick = { id -> navController.navigate(ChallengeDetail(id)) },
                )
            }
            composable<Profile> {
                ProfileScreen(
                    state = PreviewData.profile.copy(showGallery = BuildConfig.DEBUG),
                    onSignOut = { navController.navigate(Login) { popUpTo(0) } },
                    onOpenGallery = { navController.navigate(Gallery) },
                )
            }

            // --- Challenge flow -----------------------------------------
            composable<Invite> {
                InviteScreen(
                    state = PreviewData.invite,
                    onCopyLink = {},
                    onShareLink = {},
                    onBack = { navController.popBackStack() },
                )
            }
            composable<ChallengeDetail> { entry ->
                val route = entry.toRoute<ChallengeDetail>()
                // The completed fake ("c0", reachable from the Completed tab)
                // renders the ended variant, which is the only route to Winner.
                val isCompleted = route.id == "c0"
                ChallengeDetailScreen(
                    state = PreviewData.challengeDetail.copy(
                        isCompleted = isCompleted,
                        remaining = if (isCompleted) "Ended" else PreviewData.challengeDetail.remaining,
                    ),
                    onBack = { navController.popBackStack() },
                    onRecordWorkout = { navController.navigate(Record(route.id)) },
                    onSeeWinner = { navController.navigate(Winner(route.id)) },
                    onCancelChallenge = { navController.popBackStack() },
                )
            }
            composable<ChallengeInvite>(
                deepLinks = listOf(navDeepLink<ChallengeInvite>(basePath = INVITE_BASE_PATH)),
            ) { entry ->
                val route = entry.toRoute<ChallengeInvite>()
                ChallengeInviteScreen(
                    state = PreviewData.challengeInvite,
                    onAccept = {
                        navController.navigate(ChallengeDetail(route.code)) { popUpTo(0) }
                    },
                    onDecline = { navController.navigate(Home) { popUpTo(0) } },
                )
            }
            composable<Record> {
                RecordScreen(
                    state = PreviewData.record,
                    onGrantCameraPermission = {},
                    onStopAndSave = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Winner> {
                WinnerScreen(
                    state = PreviewData.winner,
                    onBackToHome = { navController.navigate(Home) { popUpTo(0) } },
                )
            }

            // --- Create flow (nested graph) -----------------------------
            // Scoped as its own graph so the three screens can later share one
            // CreateChallengeViewModel instead of passing results back.
            navigation<CreateGraph>(startDestination = Create) {
                composable<Create> {
                    CreateChallengeScreen(
                        state = PreviewData.createChallenge,
                        onBack = { navController.popBackStack() },
                        onPickBuddies = { navController.navigate(BuddyPicker) },
                        onPickWindow = { navController.navigate(DateTimePicker) },
                        onCreate = {
                            navController.navigate(ChallengeDetail("new")) {
                                popUpTo(CreateGraph) { inclusive = true }
                            }
                        },
                    )
                }
                composable<BuddyPicker> {
                    // Temporary UI-local selection, replaced by the shared
                    // CreateChallengeViewModel once the create flow is wired.
                    var buddies by remember { mutableStateOf(PreviewData.buddies) }
                    var query by rememberSaveable { mutableStateOf("") }
                    BuddyPickerScreen(
                        state = PreviewData.buddyPicker.copy(buddies = buddies, query = query),
                        onQueryChange = { query = it },
                        onToggleBuddy = { uid ->
                            val selectedCount = buddies.count { it.selected }
                            buddies = buddies.map { buddy ->
                                when {
                                    buddy.uid != uid -> buddy
                                    buddy.selected -> buddy.copy(selected = false)
                                    selectedCount < MAX_BUDDIES -> buddy.copy(selected = true)
                                    else -> buddy
                                }
                            }
                        },
                        onDone = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<DateTimePicker> {
                    var preset by rememberSaveable { mutableIntStateOf(3) }
                    DateTimePickerScreen(
                        state = PreviewData.dateTimePicker.copy(selectedPresetIndex = preset),
                        onSelectPreset = { preset = it },
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                    )
                }
            }

            // --- Development only ---------------------------------------
            composable<Gallery> { ComponentGallery() }
        }
    }
}
