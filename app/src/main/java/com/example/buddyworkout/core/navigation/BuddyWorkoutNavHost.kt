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
import com.example.buddyworkout.core.common.CalendarMonth
import com.example.buddyworkout.core.ui.component.BwBottomNav
import com.example.buddyworkout.core.ui.gallery.ComponentGallery
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.feature.auth.LoginRoute
import com.example.buddyworkout.feature.auth.RegisterRoute
import com.example.buddyworkout.feature.challenge.ChallengeDetailRoute
import com.example.buddyworkout.feature.challenge.ChallengesRoute
import com.example.buddyworkout.feature.challenge.create.BuddyPickerScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.example.buddyworkout.feature.challenge.create.CreateChallengeRoute
import com.example.buddyworkout.feature.challenge.create.CreateChallengeViewModel
import com.example.buddyworkout.feature.challenge.create.MAX_BUDDIES
import com.example.buddyworkout.feature.challenge.create.DateTimeMode
import com.example.buddyworkout.feature.challenge.create.DateTimePickerScreen
import com.example.buddyworkout.feature.home.HomeRoute
import com.example.buddyworkout.feature.invite.ChallengeInviteScreen
import com.example.buddyworkout.feature.invite.InviteScreen
import com.example.buddyworkout.feature.profile.ProfileRoute
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
    val motion = rememberNavMotion(navController)

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
            enterTransition = NavTransitions.enter,
            exitTransition = NavTransitions.exit,
            popEnterTransition = NavTransitions.popEnter,
            popExitTransition = NavTransitions.popExit,
        ) {
            // --- Auth ---------------------------------------------------
            scrimmed<Login>(motion) {
                LoginRoute(
                    onSignedIn = { navController.navigate(Home) { popUpTo(0) } },
                    onRegisterClick = { navController.navigate(Register) },
                )
            }
            scrimmed<Register>(motion) {
                RegisterRoute(
                    onRegistered = { navController.navigate(Home) { popUpTo(0) } },
                    onBack = { navController.popBackStack() },
                )
            }

            // --- Bottom-nav roots ---------------------------------------
            scrimmed<Home>(motion) {
                HomeRoute(
                    onCreateChallenge = { navController.navigate(CreateGraph) },
                    onInviteBuddies = { navController.navigate(Invite) },
                    onChallengeClick = { id -> navController.navigate(ChallengeDetail(id)) },
                    onNotifications = {},
                )
            }
            scrimmed<Challenges>(motion) {
                ChallengesRoute(
                    onChallengeClick = { id -> navController.navigate(ChallengeDetail(id)) },
                    onCreateChallenge = { navController.navigate(CreateGraph) },
                )
            }
            scrimmed<Profile>(motion) {
                ProfileRoute(
                    onSignedOut = { navController.navigate(Login) { popUpTo(0) } },
                    onOpenGallery = { navController.navigate(Gallery) },
                )
            }

            // --- Challenge flow -----------------------------------------
            scrimmed<Invite>(motion) {
                InviteScreen(
                    state = PreviewData.invite,
                    onCopyLink = {},
                    onShareLink = {},
                    onChallengeBuddy = { navController.navigate(CreateGraph) },
                    onBack = { navController.popBackStack() },
                )
            }
            scrimmed<ChallengeDetail>(motion) { entry ->
                val route = entry.toRoute<ChallengeDetail>()
                ChallengeDetailRoute(
                    onBack = { navController.popBackStack() },
                    onRecordWorkout = { navController.navigate(Record(route.id)) },
                    onSeeWinner = { navController.navigate(Winner(route.id)) },
                    onCancelled = { navController.popBackStack() },
                )
            }
            scrimmed<ChallengeInvite>(
                motion = motion,
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
            scrimmed<Record>(motion) {
                RecordScreen(
                    state = PreviewData.record,
                    onGrantCameraPermission = {},
                    onFlipCamera = {},
                    onStopAndSave = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            scrimmed<Winner>(motion) {
                WinnerScreen(
                    state = PreviewData.winner,
                    onRematch = { navController.navigate(CreateGraph) },
                    onBack = { navController.navigate(Home) { popUpTo(0) } },
                )
            }

            // --- Create flow (nested graph) -----------------------------
            // Scoped as its own graph so the three screens can later share one
            // CreateChallengeViewModel instead of passing results back.
            navigation<CreateGraph>(startDestination = Create) {
                scrimmed<Create>(motion) { entry ->
                    CreateChallengeRoute(
                        viewModel = createViewModel(navController, entry),
                        onCreated = { id ->
                            navController.navigate(ChallengeDetail(id)) {
                                popUpTo(CreateGraph) { inclusive = true }
                            }
                        },
                        onPickBuddies = { navController.navigate(BuddyPicker) },
                        onPickWindow = { navController.navigate(DateTimePicker) },
                        onBack = { navController.popBackStack() },
                    )
                }
                scrimmed<BuddyPicker>(motion) {
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
                scrimmed<DateTimePicker>(motion) {
                    // Temporary UI-local sheet state, likewise replaced by the
                    // shared CreateChallengeViewModel. The month is held as two
                    // ints so it survives process death without a custom Saver.
                    var mode by rememberSaveable { mutableStateOf(DateTimeMode.Date) }
                    var year by rememberSaveable { mutableIntStateOf(2026) }
                    var month by rememberSaveable { mutableIntStateOf(6) }
                    var day by rememberSaveable { mutableStateOf<Int?>(17) }
                    var hour by rememberSaveable { mutableIntStateOf(6) }
                    var minute by rememberSaveable { mutableIntStateOf(0) }
                    var isPm by rememberSaveable { mutableStateOf(true) }

                    val current = CalendarMonth(year, month)
                    DateTimePickerScreen(
                        state = PreviewData.dateTimePicker.copy(
                            mode = mode,
                            month = current,
                            selectedDay = day,
                            hour = hour,
                            minute = minute,
                            isPm = isPm,
                        ),
                        onSelectMode = { mode = it },
                        onSelectDay = { day = it },
                        onPreviousMonth = {
                            current.previous().let { year = it.year; month = it.month }
                            // The day may not exist in the month stepped into.
                            day = null
                        },
                        onNextMonth = {
                            current.next().let { year = it.year; month = it.month }
                            day = null
                        },
                        onSelectHour = { hour = it },
                        onSelectMinute = { minute = it },
                        onToggleMeridiem = { isPm = !isPm },
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                    )
                }
            }

            // --- Development only ---------------------------------------
            scrimmed<Gallery>(motion) { ComponentGallery() }
        }
    }
}

/**
 * The create flow's shared ViewModel, scoped to the graph rather than to a
 * screen, so the three screens of the flow see the same state — which is why
 * `CreateGraph` is a nested graph at all (spec §2.5).
 */
@Composable
private fun createViewModel(
    navController: NavHostController,
    entry: NavBackStackEntry,
): CreateChallengeViewModel {
    val graphEntry = remember(entry) { navController.getBackStackEntry(CreateGraph) }
    return hiltViewModel(graphEntry)
}
