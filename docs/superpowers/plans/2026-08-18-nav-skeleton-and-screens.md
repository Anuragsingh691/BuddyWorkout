# Navigation Skeleton & Stateless Screens — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a runnable BuddyWorkout app where all 13 screens are reachable through a single type-safe `NavHost` and render real UI against hardcoded fake state.

**Architecture:** One `Activity` hosting one `NavHost` with `@Serializable` type-safe routes. Every screen is a stateless `@Composable` taking a flat `UiState` data class plus navigation lambdas — no `NavController`, no Hilt inside screens. Until ViewModels land in a later plan, the `NavHost` feeds screens from `PreviewData`, a `main`-source-set object that `@Preview` functions also use permanently.

**Tech Stack:** Kotlin 2.4.10, Jetpack Compose (BOM 2026.06.01) + Material 3, Navigation Compose 2.9.8 (type-safe routes via kotlinx-serialization), Hilt 2.60.1, Firebase Auth (session gate only), JUnit4 + kotlinx-coroutines-test.

**Spec:** [`docs/superpowers/specs/2026-08-18-app-architecture-design.md`](../specs/2026-08-18-app-architecture-design.md)

## Global Constraints

- **Single Gradle module.** Everything goes in `:app`. Do not create new Gradle modules.
- **Base package:** `com.example.buddyworkout`.
- **`core/ui/` must not import from `feature/`, `data/`, or `domain/`.** The DLS stays domain-ignorant.
- **`ai/` must not import from `feature/`, `data/`, or `domain/`.** (No `ai/` code in this plan; the rule is stated so it is not violated later.)
- **Screens never take a `NavController` and never call `hiltViewModel()`.** Only `*Route.kt` wrappers may — and no `*Route.kt` files exist yet in this plan.
- **UI state is one flat data class per screen** with `isLoading: Boolean` and `error: String?` fields. No sealed `UiState` hierarchies.
- **Events are individual lambdas** (`onCreateChallenge: () -> Unit`), never a sealed `Event` type with one `onEvent` sink.
- **UI models carry preformatted strings.** Composables must never format a duration, a date, or call `.toString()` on a domain type.
- **Use existing DLS components and tokens only.** Colors from `BwColors`, spacing from `BwSpace`, radii from `BwRadius`, sizes from `BwSize`, icons from `BwIcons`. Never hardcode a hex color or a raw `dp` that a token already covers.
- **Light theme only.** `BuddyWorkoutTheme` pins a light scheme; do not add dark-theme branches.
- **minSdk 24**, targetSdk/compileSdk 36.
- **AGP 9 supplies Kotlin support built in** — do not add the `org.jetbrains.kotlin.android` plugin.
- Every task ends with a commit.

## Reference: existing DLS API

Components in `com.example.buddyworkout.core.ui.component` (exact signatures — use them as written):

```kotlin
ActionTile(title: String, description: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier, filled: Boolean = true)
data class AvatarUi(val initials: String, val key: String = initials, val color: Color? = null)
Avatar(avatar: AvatarUi, modifier: Modifier = Modifier, size: Dp = BwSize.Thumb, ring: Boolean = false)
AvatarStack(avatars: List<AvatarUi>, modifier: Modifier = Modifier, size: Dp = 30.dp, max: Int = 4)
enum class BwNavItem(val label: String, val icon: ImageVector) { Home, Challenges, Profile }
BwBottomNav(selected: BwNavItem, onSelect: (BwNavItem) -> Unit, modifier: Modifier = Modifier)
enum class BwButtonVariant { Primary, Outline, Tint, DangerGhost }
BwButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, variant: BwButtonVariant = Primary, icon: ImageVector? = null, enabled: Boolean = true, height: Dp = ...)
BwCard(modifier: Modifier = Modifier, containerColor: Color = BwColors.Surface, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit)
DetailRow(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = BwColors.Ink)
BwSegmentedTabs(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier)
BwTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, label: String? = null, labelHint: String? = null, placeholder: String = "", trailingIcon: ImageVector? = null, onTrailingIconClick: (() -> Unit)? = null, keyboardOptions: KeyboardOptions = KeyboardOptions.Default, isPassword: Boolean = false, enabled: Boolean = true)
BwIconButton(icon: ImageVector, onClick: () -> Unit, contentDescription: String?, modifier: Modifier = Modifier, tinted: Boolean = false, tint: Color = BwColors.Ink)
BwTopBar(title: String, modifier: Modifier = Modifier, overline: String? = null, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {})
CameraOverlay(reps: Int, formLabel: String?, footerLabel: String, footerValue: String, modifier: Modifier = Modifier, showPoseSkeleton: Boolean = true, cameraPreview: @Composable BoxScope.() -> Unit = {})
ChallengeCard(title: String, summary: String, progress: Float, stat: String, onClick: () -> Unit, modifier: Modifier = Modifier, accent: Color = BwColors.Primary, trailing: (@Composable () -> Unit)? = null)
ProgressBar(progress: Float, modifier: Modifier = Modifier, accent: Color = BwColors.Primary)
CountdownCard(remaining: String, members: List<AvatarUi>, modifier: Modifier = Modifier, note: String? = "Auto-declares the winner at the deadline. No early finish.")
enum class RowTone { Plain, Highlight, Gold }
LeaderboardRow(avatar: AvatarUi, name: String, modifier: Modifier = Modifier, rank: Int? = null, subtitle: String? = null, value: String? = null, tone: RowTone = Plain, showDivider: Boolean = true, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null)
NoticeCard(text: String, icon: ImageVector, modifier: Modifier = Modifier, containerColor: Color = BwColors.AmberTint, contentColor: Color = BwColors.AmberInk, cornerRadius: Dp = BwRadius.Card)
enum class PillTone { Green, Amber, Live, Neutral }
Pill(text: String, modifier: Modifier = Modifier, tone: PillTone = Green)
SectionTitle(text: String, modifier: Modifier = Modifier, hint: String? = null, trailing: (@Composable () -> Unit)? = null)
WinnerBanner(winner: String, detail: String, modifier: Modifier = Modifier, overline: String = "🏆 Challenge ended")
fun avatarColorFor(key: String): Color
```

Available icons in `BwIcons`: `Dumbbell`, `DumbbellSmall`, `ArrowRight`, `ChevronLeft`, `ChevronRight`, `Plus`, `Close`, `Check`, `Home`, `List`, `Person`, `PersonAdd`, `Users`, `Bell`, `Eye`, `Clock`, `Info`, `Calendar`, `Lock`, `Video`, `Search`, `Link`, `Share`, `AlertTriangle`, `MoreVertical`, `Stop`, `Google`.

Tokens: `BwColors.{Bg,Surface,Primary,PrimaryDark,PrimaryTint,Ink,Muted,Line,Amber,AmberTint,AmberInk,Danger,DangerTint,DangerInk,Placeholder,LabelInk,LabelInkStrong,CameraBg,PoseJoint,CameraSubtle,Avatar}`, `BwSpace.{Xs,Sm,Md,Lg,Gutter}`, `BwRadius.{IconButton,Thumb,Control,Card,Tile,Pill,Sheet}`, `BwSize.{Button,ButtonCompact,ButtonSmall,Chip,TextField,TopBar,BottomNav,IconButton,Thumb,Border,AvatarOverlap,AvatarRing}`.

## File Structure

**Created by this plan:**

| File | Responsibility |
|------|----------------|
| `core/common/TimeFormat.kt` | Duration → display strings (`"2d 14h left"`). Pure, unit-tested. |
| `core/navigation/Routes.kt` | All `@Serializable` route types + `BwNavItem` ↔ route mapping + bottom-bar visibility rule. |
| `core/navigation/AuthState.kt` | `AuthState` enum + `SessionSource` interface. |
| `core/navigation/AppViewModel.kt` | Exposes `StateFlow<AuthState>` for the gate above the `NavHost`. |
| `core/navigation/BuddyWorkoutNavHost.kt` | The single `NavHost` + `Scaffold` + conditional bottom bar. |
| `data/auth/FirebaseSessionSource.kt` | `SessionSource` impl over `FirebaseAuth`. |
| `core/di/SessionModule.kt` | Binds `FirebaseSessionSource` to `SessionSource`. |
| `core/ui/model/UiModels.kt` | Shared UI models: `ChallengeUi`, `ParticipantUi`, `BuddyUi`. |
| `core/ui/preview/PreviewData.kt` | Fake instances of every `UiState`. Used by `@Preview` and the skeleton `NavHost`. |
| `feature/<name>/<Name>Screen.kt` | One stateless screen each (13 total). |
| `feature/<name>/<Name>UiState.kt` | One flat state class each. |

**Modified:** `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `MainActivity.kt`.

**Deliberately NOT created by this plan:** any `*Route.kt`, any `*ViewModel.kt` other than `AppViewModel`, anything under `domain/usecase/`, `data/challenge/`, `data/workout/`, or `ai/`.

---

### Task 1: Build configuration

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Consumes: nothing.
- Produces: the `kotlinx.serialization` plugin (needed for `@Serializable` routes in Task 3), `collectAsStateWithLifecycle` (Task 5), `BuildConfig.DEBUG` (Task 5), and `runTest` (Task 4).

- [ ] **Step 1: Add versions and libraries to the catalog**

In `gradle/libs.versions.toml`, rename the `coroutinesPlayServices` version key to `coroutines` (it now backs two artifacts) and add three new entries.

Under `[versions]`, replace the line `coroutinesPlayServices = "1.11.0"` with:

```toml
coroutines = "1.11.0"
kotlinxSerialization = "1.9.0"
```

Under `[libraries]`, replace the `kotlinx-coroutines-play-services` line with:

```toml
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
```

Under `[plugins]`, add:

```toml
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

- [ ] **Step 2: Apply the plugin and add dependencies**

In `app/build.gradle.kts`, add to the `plugins { }` block after `alias(libs.plugins.kotlin.compose)`:

```kotlin
    alias(libs.plugins.kotlin.serialization)
```

In the `android { }` block, extend `buildFeatures` so `BuildConfig.DEBUG` is generated (AGP does not generate it by default):

```kotlin
    buildFeatures {
        compose = true
        buildConfig = true
    }
```

In `dependencies { }`, add next to the other Compose entries:

```kotlin
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)
```

and next to `testImplementation(libs.junit)`:

```kotlin
    testImplementation(libs.kotlinx.coroutines.test)
```

- [ ] **Step 3: Verify the build still compiles**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

If `kotlinx-serialization-json:1.9.0` fails to resolve, raise `kotlinxSerialization` to the newest version on Maven Central and re-run. Do not proceed until the build is green.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "build: add kotlinx-serialization, lifecycle-runtime-compose, coroutines-test"
```

---

### Task 2: Duration formatting

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/core/common/TimeFormat.kt`
- Test: `app/src/test/java/com/example/buddyworkout/core/common/TimeFormatTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `fun formatRemaining(millisRemaining: Long): String` — used by every UI model that shows a countdown (Tasks 6, 9, 11).

Why this exists: the Global Constraints forbid composables from formatting durations. This is the one place that logic lives, and it is pure, so it is genuinely unit-testable.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/example/buddyworkout/core/common/TimeFormatTest.kt`:

```kotlin
package com.example.buddyworkout.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

private const val SECOND = 1_000L
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

class TimeFormatTest {

    @Test
    fun `days and hours shown when more than a day remains`() {
        assertEquals("2d 14h left", formatRemaining(2 * DAY + 14 * HOUR))
    }

    @Test
    fun `hours and minutes shown when less than a day remains`() {
        assertEquals("3h 20m left", formatRemaining(3 * HOUR + 20 * MINUTE))
    }

    @Test
    fun `minutes only when less than an hour remains`() {
        assertEquals("45m left", formatRemaining(45 * MINUTE))
    }

    @Test
    fun `under a minute reads as almost over rather than zero`() {
        assertEquals("< 1m left", formatRemaining(30 * SECOND))
    }

    @Test
    fun `zero is ended`() {
        assertEquals("Ended", formatRemaining(0))
    }

    @Test
    fun `negative is ended rather than a negative duration`() {
        assertEquals("Ended", formatRemaining(-5 * HOUR))
    }

    @Test
    fun `whole days do not print a zero hour component`() {
        assertEquals("3d left", formatRemaining(3 * DAY))
    }

    @Test
    fun `whole hours do not print a zero minute component`() {
        assertEquals("5h left", formatRemaining(5 * HOUR))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.buddyworkout.core.common.TimeFormatTest"`
Expected: FAIL — compilation error, `Unresolved reference: formatRemaining`.

- [ ] **Step 3: Write the implementation**

Create `app/src/main/java/com/example/buddyworkout/core/common/TimeFormat.kt`:

```kotlin
package com.example.buddyworkout.core.common

private const val SECOND = 1_000L
private const val MINUTE = 60 * SECOND
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR

/**
 * Renders a remaining duration for challenge countdowns: `"2d 14h left"`,
 * `"45m left"`, `"Ended"`.
 *
 * Composables must never format durations themselves (see the design spec), so
 * every countdown string in the app originates here.
 */
fun formatRemaining(millisRemaining: Long): String {
    if (millisRemaining <= 0) return "Ended"

    val days = millisRemaining / DAY
    val hours = (millisRemaining % DAY) / HOUR
    val minutes = (millisRemaining % HOUR) / MINUTE

    return when {
        days > 0 && hours > 0 -> "${days}d ${hours}h left"
        days > 0 -> "${days}d left"
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m left"
        hours > 0 -> "${hours}h left"
        minutes > 0 -> "${minutes}m left"
        else -> "< 1m left"
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.buddyworkout.core.common.TimeFormatTest"`
Expected: PASS, 8 tests.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/core/common/TimeFormat.kt app/src/test/java/com/example/buddyworkout/core/common/TimeFormatTest.kt
git commit -m "feat: add formatRemaining for challenge countdowns"
```

---

### Task 3: Type-safe routes and bottom-bar rule

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/core/navigation/Routes.kt`
- Test: `app/src/test/java/com/example/buddyworkout/core/navigation/RoutesTest.kt`

**Interfaces:**
- Consumes: `BwNavItem` from `core.ui.component`.
- Produces: route types `Login`, `Register`, `Home`, `Challenges`, `Profile`, `Invite`, `ChallengeDetail(id)`, `ChallengeInvite(code)`, `Record(challengeId)`, `Winner(challengeId)`, `Gallery`, `CreateGraph`, `Create`, `BuddyPicker`, `DateTimePicker`; plus `BwNavItem.route(): Any` and `fun bottomBarTabFor(routeName: String?): BwNavItem?`. Task 5 consumes all of these.

Note on the visibility rule: `NavDestination.route` at runtime is the **fully-qualified class name** of the `@Serializable` type (e.g. `com.example.buddyworkout.core.navigation.Home`), so the mapping is done on that string. Keeping it a pure `String? -> BwNavItem?` function is what makes it testable without a `NavController`.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/example/buddyworkout/core/navigation/RoutesTest.kt`:

```kotlin
package com.example.buddyworkout.core.navigation

import com.example.buddyworkout.core.ui.component.BwNavItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoutesTest {

    @Test
    fun `each bottom nav item maps to its root route`() {
        assertEquals(Home, BwNavItem.Home.route())
        assertEquals(Challenges, BwNavItem.Challenges.route())
        assertEquals(Profile, BwNavItem.Profile.route())
    }

    @Test
    fun `root destinations select their tab`() {
        assertEquals(BwNavItem.Home, bottomBarTabFor(Home::class.qualifiedName))
        assertEquals(BwNavItem.Challenges, bottomBarTabFor(Challenges::class.qualifiedName))
        assertEquals(BwNavItem.Profile, bottomBarTabFor(Profile::class.qualifiedName))
    }

    @Test
    fun `non-root destinations hide the bottom bar`() {
        assertNull(bottomBarTabFor(Login::class.qualifiedName))
        assertNull(bottomBarTabFor(Create::class.qualifiedName))
        assertNull(bottomBarTabFor(ChallengeDetail::class.qualifiedName))
        assertNull(bottomBarTabFor(Record::class.qualifiedName))
    }

    @Test
    fun `parameterised routes match despite their argument suffix`() {
        // Navigation appends an argument pattern to the route string of a
        // route with parameters, e.g. "...ChallengeDetail/{id}". The rule must
        // not accidentally match a prefix of a root route.
        assertNull(bottomBarTabFor("${ChallengeDetail::class.qualifiedName}/{id}"))
    }

    @Test
    fun `null destination hides the bottom bar`() {
        assertNull(bottomBarTabFor(null))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.buddyworkout.core.navigation.RoutesTest"`
Expected: FAIL — compilation error, unresolved references to the route types.

- [ ] **Step 3: Write the implementation**

Create `app/src/main/java/com/example/buddyworkout/core/navigation/Routes.kt`:

```kotlin
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
// ViewModel scoped to CreateGraph (see design spec §2.5). ------------------
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
 * take parameters. Matching on exact equality is therefore both correct and
 * enough: the three roots take no parameters.
 */
fun bottomBarTabFor(routeName: String?): BwNavItem? = when (routeName) {
    Home::class.qualifiedName -> BwNavItem.Home
    Challenges::class.qualifiedName -> BwNavItem.Challenges
    Profile::class.qualifiedName -> BwNavItem.Profile
    else -> null
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.buddyworkout.core.navigation.RoutesTest"`
Expected: PASS, 5 tests.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/core/navigation/Routes.kt app/src/test/java/com/example/buddyworkout/core/navigation/RoutesTest.kt
git commit -m "feat: add type-safe navigation routes and bottom-bar rule"
```

---

### Task 4: Session gate

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/core/navigation/AuthState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/core/navigation/AppViewModel.kt`
- Create: `app/src/main/java/com/example/buddyworkout/data/auth/FirebaseSessionSource.kt`
- Create: `app/src/main/java/com/example/buddyworkout/core/di/SessionModule.kt`
- Test: `app/src/test/java/com/example/buddyworkout/core/navigation/AppViewModelTest.kt`

**Interfaces:**
- Consumes: `FirebaseAuth` (already provided by `core/di/FirebaseModule.kt`).
- Produces: `enum class AuthState { Loading, SignedOut, SignedIn }`, `interface SessionSource { val authState: Flow<AuthState> }`, and `class AppViewModel` exposing `val state: StateFlow<AuthState>`. Task 5 consumes `AppViewModel` and `AuthState`.

Why a real Firebase-backed source now rather than a fake: it is ~25 lines, it makes the gate genuinely work from day one, and the later auth plan builds `AuthRepository` on top rather than replacing this.

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/com/example/buddyworkout/core/navigation/AppViewModelTest.kt`:

```kotlin
package com.example.buddyworkout.core.navigation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class FakeSessionSource(initial: AuthState) : SessionSource {
    private val flow = MutableStateFlow(initial)
    override val authState: Flow<AuthState> = flow
    fun emit(value: AuthState) { flow.value = value }
}

class AppViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts in Loading before the source emits`() = runTest(dispatcher) {
        val vm = AppViewModel(FakeSessionSource(AuthState.SignedIn))
        assertEquals(AuthState.Loading, vm.state.value)
    }

    @Test
    fun `adopts the signed-in state once the source emits`() = runTest(dispatcher) {
        val vm = AppViewModel(FakeSessionSource(AuthState.SignedIn))
        testScheduler.advanceUntilIdle()
        assertEquals(AuthState.SignedIn, vm.state.value)
    }

    @Test
    fun `follows the source when the session ends`() = runTest(dispatcher) {
        val source = FakeSessionSource(AuthState.SignedIn)
        val vm = AppViewModel(source)
        testScheduler.advanceUntilIdle()

        source.emit(AuthState.SignedOut)
        testScheduler.advanceUntilIdle()

        assertEquals(AuthState.SignedOut, vm.state.value)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.buddyworkout.core.navigation.AppViewModelTest"`
Expected: FAIL — compilation error, `Unresolved reference: AuthState`.

- [ ] **Step 3: Write AuthState and SessionSource**

Create `app/src/main/java/com/example/buddyworkout/core/navigation/AuthState.kt`:

```kotlin
package com.example.buddyworkout.core.navigation

import kotlinx.coroutines.flow.Flow

/**
 * Whether a signed-in session exists. [Loading] means Firebase has not yet
 * restored the persisted session — the app shows a blank themed screen rather
 * than guessing, which is what avoids a flash of the login screen on launch.
 */
enum class AuthState { Loading, SignedOut, SignedIn }

/** Single source of truth for the session, observed above the NavHost. */
interface SessionSource {
    val authState: Flow<AuthState>
}
```

- [ ] **Step 4: Write AppViewModel**

Create `app/src/main/java/com/example/buddyworkout/core/navigation/AppViewModel.kt`:

```kotlin
package com.example.buddyworkout.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Holds the session state the navigation gate reads. Deliberately the only
 * ViewModel above the NavHost: screens get their own later.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    sessionSource: SessionSource,
) : ViewModel() {

    val state: StateFlow<AuthState> = sessionSource.authState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AuthState.Loading,
    )
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :app:testDebugUnitTest --tests "com.example.buddyworkout.core.navigation.AppViewModelTest"`
Expected: PASS, 3 tests.

- [ ] **Step 6: Write the Firebase-backed implementation**

Create `app/src/main/java/com/example/buddyworkout/data/auth/FirebaseSessionSource.kt`:

```kotlin
package com.example.buddyworkout.data.auth

import com.example.buddyworkout.core.navigation.AuthState
import com.example.buddyworkout.core.navigation.SessionSource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bridges Firebase's `AuthStateListener` to a Flow. The listener fires once
 * immediately on registration with the restored session, which is what moves
 * the app out of [AuthState.Loading].
 */
@Singleton
class FirebaseSessionSource @Inject constructor(
    private val auth: FirebaseAuth,
) : SessionSource {

    override val authState: Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(
                if (firebaseAuth.currentUser != null) AuthState.SignedIn else AuthState.SignedOut
            )
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()
}
```

- [ ] **Step 7: Bind it in Hilt**

Create `app/src/main/java/com/example/buddyworkout/core/di/SessionModule.kt`:

```kotlin
package com.example.buddyworkout.core.di

import com.example.buddyworkout.core.navigation.SessionSource
import com.example.buddyworkout.data.auth.FirebaseSessionSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {

    @Binds
    @Singleton
    abstract fun sessionSource(impl: FirebaseSessionSource): SessionSource
}
```

- [ ] **Step 8: Verify the whole module builds with Hilt code generation**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. A Hilt binding error here means `FirebaseModule` is not providing `FirebaseAuth` — check `core/di/FirebaseModule.kt` before changing anything else.

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/core/navigation/AuthState.kt \
        app/src/main/java/com/example/buddyworkout/core/navigation/AppViewModel.kt \
        app/src/main/java/com/example/buddyworkout/data/auth/FirebaseSessionSource.kt \
        app/src/main/java/com/example/buddyworkout/core/di/SessionModule.kt \
        app/src/test/java/com/example/buddyworkout/core/navigation/AppViewModelTest.kt
git commit -m "feat: add Firebase-backed session gate for navigation"
```

---

### Task 5: NavHost, placeholders, and the app shell

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/core/navigation/PlaceholderScreen.kt`
- Create: `app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/MainActivity.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: all route types and `bottomBarTabFor` (Task 3); `AppViewModel`, `AuthState` (Task 4); `BwBottomNav`, `BwTopBar`, `BwButton`, `ComponentGallery` (existing).
- Produces: `@Composable fun BuddyWorkoutNavHost(startDestination: Any, navController: NavHostController = rememberNavController())`. Tasks 7–12 replace one `PlaceholderScreen(...)` call inside it per screen.

Design note on the gate: the session state chooses the **entry point only**. Once the `NavHost` is composed, transitions are explicit navigation (sign-in navigates to `Home`, sign-out navigates to `Login` with `popUpTo(0)`). Recomposing a `NavHost` with a changed `startDestination` does not reliably rebuild the graph, so the start destination is captured once, when the session leaves `Loading`.

- [ ] **Step 1: Write the placeholder screen**

Create `app/src/main/java/com/example/buddyworkout/core/navigation/PlaceholderScreen.kt`:

```kotlin
package com.example.buddyworkout.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

/**
 * Temporary stand-in so every route is reachable before its real screen
 * exists. Each entry in [actions] becomes a button, which is how the whole
 * navigation graph gets click-tested on day one.
 *
 * Deleted screen by screen as Tasks 7–12 land; the file goes once the last
 * call site is gone.
 */
@Composable
fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: List<Pair<String, () -> Unit>> = emptyList(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = title, overline = "PLACEHOLDER", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Text(
                text = "This screen is not built yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
            )
            actions.forEach { (label, onClick) ->
                BwButton(
                    text = label,
                    onClick = onClick,
                    variant = BwButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
```

- [ ] **Step 2: Write the NavHost**

Create `app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt`:

```kotlin
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
            composable<Record> { entry ->
                val route = entry.toRoute<Record>()
                PlaceholderScreen(
                    title = "Record",
                    onBack = { navController.popBackStack() },
                    actions = listOf(
                        "Stop & save" to { navController.popBackStack() },
                    ),
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
```

- [ ] **Step 3: Wire MainActivity to the gate**

Replace the whole body of `app/src/main/java/com/example/buddyworkout/MainActivity.kt`:

```kotlin
package com.example.buddyworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.buddyworkout.core.navigation.AppViewModel
import com.example.buddyworkout.core.navigation.AuthState
import com.example.buddyworkout.core.navigation.BuddyWorkoutNavHost
import com.example.buddyworkout.core.navigation.Home
import com.example.buddyworkout.core.navigation.Login
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuddyWorkoutTheme {
                BuddyWorkoutApp()
            }
        }
    }
}

/**
 * Session gate. While the session is [AuthState.Loading] nothing but the theme
 * background is drawn — composing the NavHost early would flash the login
 * screen before Firebase restores a persisted session.
 *
 * The gate picks the entry point once; every later transition (sign-in,
 * sign-out) is explicit navigation inside the graph.
 */
@Composable
private fun BuddyWorkoutApp() {
    val appViewModel: AppViewModel = hiltViewModel()
    val authState by appViewModel.state.collectAsStateWithLifecycle()

    when (authState) {
        AuthState.Loading -> Box(
            Modifier
                .fillMaxSize()
                .background(BwColors.Bg)
        )

        else -> {
            val startDestination = remember {
                if (authState == AuthState.SignedIn) Home else Login
            }
            BuddyWorkoutNavHost(startDestination = startDestination)
        }
    }
}
```

- [ ] **Step 4: Declare the App Link in the manifest**

In `app/src/main/AndroidManifest.xml`, add `android:launchMode="singleTop"` to the `<activity>` tag (so a deep link reuses the running task rather than stacking a second Activity), and add a second `<intent-filter>` inside the same `<activity>`, after the existing LAUNCHER one:

```xml
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:scheme="https"
                    android:host="commworkout.app"
                    android:pathPrefix="/c" />
            </intent-filter>
```

Note: `autoVerify` will not actually verify until an `assetlinks.json` is hosted at that domain — that is part of the later invite work. The filter still routes links opened via `adb`, which is what Step 6 exercises.

- [ ] **Step 5: Build, install, and click through every route**

Run: `./gradlew :app:installDebug`
Expected: `BUILD SUCCESSFUL`, app installed.

Launch the app and verify, by hand:
1. It opens on **Sign in** (no Firebase user yet) with no bottom bar.
2. "Sign in" → **Home**, bottom bar now visible with Home selected.
3. Bottom bar switches between Home / Challenges / Profile, and the bar stays visible on all three.
4. Home → "Create challenge" → **New challenge**, bottom bar hidden. "Add buddies" and "Set start & end" push and pop back correctly. "Create" lands on **Challenge new** with the create graph gone from the back stack (system back goes to Home, not back into the create flow).
5. Challenge detail → "Record workout" → **Record**, back returns to the detail screen.
6. Profile → "Component gallery" opens the existing `ComponentGallery` (debug build only).
7. Profile → "Sign out" returns to **Sign in** with an empty back stack (system back exits the app).

- [ ] **Step 6: Verify the deep link routes correctly**

Run:

```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://commworkout.app/c/abc123" com.example.buddyworkout
```

Expected: the app opens directly on the placeholder titled **Invite abc123**, proving `navDeepLink<ChallengeInvite>` parsed the `code` argument out of the path.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/core/navigation/PlaceholderScreen.kt \
        app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt \
        app/src/main/java/com/example/buddyworkout/MainActivity.kt \
        app/src/main/AndroidManifest.xml
git commit -m "feat: add NavHost with all 13 routes, session gate, and invite deep link"
```

---

### Task 6: Shared UI models and PreviewData

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/core/ui/model/UiModels.kt`
- Create: `app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt`

**Interfaces:**
- Consumes: `AvatarUi`, `avatarColorFor` (existing DLS).
- Produces: `ChallengeUi`, `ParticipantUi`, `BuddyUi`, and `object PreviewData` with `people`, `challenges`, `completedChallenges`, `leaderboard`, `buddies`. Every screen task (7–12) consumes these and appends its own screen state to `PreviewData`.

These models carry **preformatted strings only** — no `Instant`, no `Duration`, no domain types. That is what keeps composables free of formatting logic.

- [ ] **Step 1: Write the shared UI models**

Create `app/src/main/java/com/example/buddyworkout/core/ui/model/UiModels.kt`:

```kotlin
package com.example.buddyworkout.core.ui.model

import com.example.buddyworkout.core.ui.component.AvatarUi

/**
 * A challenge as a screen renders it. Every field is display-ready: the data
 * layer formats, the composable only places.
 */
data class ChallengeUi(
    val id: String,
    val title: String,
    /** e.g. `"4 buddies · 2d 14h left"`. */
    val summary: String,
    /** Elapsed fraction of the challenge window, `0f`–`1f`. */
    val progress: Float,
    /** e.g. `"128 reps"` — the viewer's own total. */
    val stat: String,
    val members: List<AvatarUi>,
    /** e.g. `"2d 14h left"`, or `"Ended"`. */
    val remaining: String,
    val isCompleted: Boolean = false,
)

/** One row of a leaderboard. */
data class ParticipantUi(
    val uid: String,
    val name: String,
    val avatar: AvatarUi,
    val rank: Int,
    /** e.g. `"128 reps"`. */
    val reps: String,
    /** e.g. `"Avg form 92%"`, or null when there is nothing to add. */
    val subtitle: String? = null,
    val isMe: Boolean = false,
)

/** A selectable buddy in the create flow. */
data class BuddyUi(
    val uid: String,
    val name: String,
    val avatar: AvatarUi,
    /** e.g. `"In 1 active challenge"`. */
    val subtitle: String? = null,
    val selected: Boolean = false,
)
```

- [ ] **Step 2: Write the shared fakes**

Create `app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt`:

```kotlin
package com.example.buddyworkout.core.ui.preview

import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.model.BuddyUi
import com.example.buddyworkout.core.ui.model.ChallengeUi
import com.example.buddyworkout.core.ui.model.ParticipantUi
import com.example.buddyworkout.core.ui.theme.avatarColorFor

/**
 * Hardcoded state used by two consumers: `@Preview` functions (permanently) and
 * the NavHost while screens have no ViewModel yet (temporarily).
 *
 * Lives in `main`, not `debug`, because previews compile against `main`.
 */
object PreviewData {

    val me = AvatarUi("AS", color = avatarColorFor("AS"))
    val people = listOf(
        AvatarUi("RK", color = avatarColorFor("RK")),
        AvatarUi("PM", color = avatarColorFor("PM")),
        AvatarUi("SV", color = avatarColorFor("SV")),
        me,
    )

    val challenges = listOf(
        ChallengeUi(
            id = "c1",
            title = "Pushup challenge",
            summary = "4 buddies · 2d 14h left",
            progress = 0.62f,
            stat = "128 reps",
            members = people,
            remaining = "2d 14h left",
        ),
        ChallengeUi(
            id = "c2",
            title = "Pushup challenge",
            summary = "2 buddies · 5h 40m left",
            progress = 0.88f,
            stat = "64 reps",
            members = people.take(2),
            remaining = "5h 40m left",
        ),
    )

    val completedChallenges = listOf(
        ChallengeUi(
            id = "c0",
            title = "Pushup challenge",
            summary = "3 buddies · won by Rahul",
            progress = 1f,
            stat = "204 reps",
            members = people.take(3),
            remaining = "Ended",
            isCompleted = true,
        ),
    )

    val leaderboard = listOf(
        ParticipantUi("u1", "Rahul K.", people[0], rank = 1, reps = "204 reps", subtitle = "Avg form 94%"),
        ParticipantUi("u2", "Priya M.", people[1], rank = 2, reps = "186 reps", subtitle = "Avg form 91%"),
        ParticipantUi("u4", "You", me, rank = 3, reps = "128 reps", subtitle = "Avg form 88%", isMe = true),
        ParticipantUi("u3", "Sameer V.", people[2], rank = 4, reps = "96 reps", subtitle = "Avg form 85%"),
    )

    val buddies = listOf(
        BuddyUi("u1", "Rahul K.", people[0], subtitle = "In 1 active challenge", selected = true),
        BuddyUi("u2", "Priya M.", people[1], subtitle = "Free", selected = true),
        BuddyUi("u3", "Sameer V.", people[2], subtitle = "In 2 active challenges"),
    )
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/core/ui/model/UiModels.kt \
        app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt
git commit -m "feat: add shared UI models and PreviewData fakes"
```

---

### Task 7: Auth screens (Login, Register)

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/feature/auth/LoginUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/auth/LoginScreen.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/auth/RegisterUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/auth/RegisterScreen.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt`

**Interfaces:**
- Consumes: `PreviewData` (Task 6), DLS components.
- Produces: `LoginUiState`, `RegisterUiState`, `LoginScreen(...)`, `RegisterScreen(...)`, `PreviewData.login`, `PreviewData.register`.

- [ ] **Step 1: Write the state classes**

Create `app/src/main/java/com/example/buddyworkout/feature/auth/LoginUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.auth

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val email: String = "",
    val password: String = "",
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}
```

Create `app/src/main/java/com/example/buddyworkout/feature/auth/RegisterUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.auth

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val email: String = "",
    val password: String = "",
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && email.isNotBlank() && password.length >= 6 && !isLoading
}
```

- [ ] **Step 2: Write LoginScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/auth/LoginScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwRadius
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun LoginScreen(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = BwSpace.Gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(72.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(BwColors.PrimaryTint, RoundedCornerShape(BwRadius.Tile)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = BwIcons.Dumbbell,
                contentDescription = null,
                tint = BwColors.Primary,
                modifier = Modifier.size(30.dp),
            )
        }

        Spacer(Modifier.height(BwSpace.Lg))
        Text("BuddyWorkout", style = MaterialTheme.typography.headlineMedium, color = BwColors.Ink)
        Spacer(Modifier.height(BwSpace.Xs))
        Text(
            text = "Push each other. Count every rep.",
            style = MaterialTheme.typography.bodyLarge,
            color = BwColors.Muted,
        )

        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            state.error?.let { message ->
                NoticeCard(
                    text = message,
                    icon = BwIcons.AlertTriangle,
                    containerColor = BwColors.DangerTint,
                    contentColor = BwColors.DangerInk,
                )
            }

            BwTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "you@example.com",
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            BwTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password",
                placeholder = "••••••••",
                isPassword = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            BwButton(
                text = if (state.isLoading) "Signing in…" else "Sign in",
                onClick = onSignIn,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = "or",
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Placeholder,
                modifier = Modifier.fillMaxWidth().padding(vertical = BwSpace.Xs),
            )

            BwButton(
                text = "Continue with Google",
                onClick = onGoogleSignIn,
                variant = BwButtonVariant.Outline,
                icon = BwIcons.Google,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(BwSpace.Lg))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "New here? ",
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Muted,
            )
            Text(
                text = "Create an account",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = BwColors.Primary,
                modifier = Modifier.clickable(enabled = !state.isLoading, onClick = onRegisterClick),
            )
        }

        Spacer(Modifier.height(BwSpace.Gutter))
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun LoginScreenPreview() = BuddyWorkoutTheme {
    LoginScreen(PreviewData.login, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun LoginScreenErrorPreview() = BuddyWorkoutTheme {
    LoginScreen(
        PreviewData.login.copy(error = "That email and password don't match."),
        {}, {}, {}, {}, {},
    )
}
```

- [ ] **Step 3: Write RegisterScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/auth/RegisterScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCreateAccount: () -> Unit,
    onSignInClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Create account", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Text(
                text = "Your name is what buddies see on the leaderboard.",
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
            )

            state.error?.let { message ->
                NoticeCard(
                    text = message,
                    icon = BwIcons.AlertTriangle,
                    containerColor = BwColors.DangerTint,
                    contentColor = BwColors.DangerInk,
                )
            }

            BwTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = "Name",
                placeholder = "Anurag S.",
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            BwTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "you@example.com",
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )
            BwTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Password",
                labelHint = "At least 6 characters",
                placeholder = "••••••••",
                isPassword = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(Modifier.height(BwSpace.Xs))

            BwButton(
                text = if (state.isLoading) "Creating…" else "Create account",
                onClick = onCreateAccount,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BwColors.Muted,
                )
                Text(
                    text = "Sign in",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = BwColors.Primary,
                    modifier = Modifier.clickable(enabled = !state.isLoading, onClick = onSignInClick),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun RegisterScreenPreview() = BuddyWorkoutTheme {
    RegisterScreen(PreviewData.register, {}, {}, {}, {}, {}, {})
}
```

- [ ] **Step 4: Add the fakes**

In `core/ui/preview/PreviewData.kt`, add these imports:

```kotlin
import com.example.buddyworkout.feature.auth.LoginUiState
import com.example.buddyworkout.feature.auth.RegisterUiState
```

and these properties inside `object PreviewData`:

```kotlin
    val login = LoginUiState(email = "anurag@example.com", password = "secret123")

    val register = RegisterUiState(
        name = "Anurag S.",
        email = "anurag@example.com",
        password = "secret123",
    )
```

- [ ] **Step 5: Wire both screens into the NavHost**

In `core/navigation/BuddyWorkoutNavHost.kt`, add the imports:

```kotlin
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.feature.auth.LoginScreen
import com.example.buddyworkout.feature.auth.RegisterScreen
```

Replace the `composable<Login>` and `composable<Register>` blocks with:

```kotlin
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
```

The empty `{}` handlers are correct for this phase — text fields are inert until Task 3 of the auth plan gives them a ViewModel.

- [ ] **Step 6: Build and verify**

Run: `./gradlew :app:installDebug`
Expected: `BUILD SUCCESSFUL`.

Verify by hand: the app opens on the real Login screen with the dumbbell mark, prefilled fake email and password, a Google button, and a "Create an account" link that pushes the real Register screen. Back returns to Login. In Android Studio, open `LoginScreen.kt` and confirm both `@Preview`s render, including the error variant.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/feature/auth/ \
        app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt \
        app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt
git commit -m "feat: add Login and Register screens on fake state"
```

---

### Task 8: Home and Profile screens

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/feature/home/HomeUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/home/HomeScreen.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/profile/ProfileUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/profile/ProfileScreen.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt`

**Interfaces:**
- Consumes: `ChallengeUi` and `PreviewData` (Task 6).
- Produces: `HomeUiState`, `ProfileUiState`, `StatUi`, `HomeScreen(...)`, `ProfileScreen(...)`, `PreviewData.home`, `PreviewData.profile`.

- [ ] **Step 1: Write HomeUiState**

Create `app/src/main/java/com/example/buddyworkout/feature/home/HomeUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.home

import com.example.buddyworkout.core.ui.model.ChallengeUi

data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** e.g. `"Hey, Anurag"`. */
    val greeting: String = "",
    val activeChallenges: List<ChallengeUi> = emptyList(),
    /** True at the 2-active-challenge cap; creating another is blocked. */
    val atChallengeLimit: Boolean = false,
) {
    /** e.g. `"1 of 2"` — shown next to the section title. */
    val slotsLabel: String get() = "${activeChallenges.size} of 2"
}
```

- [ ] **Step 2: Write HomeScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/home/HomeScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.ActionTile
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.BwIconButton
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.ChallengeCard
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun HomeScreen(
    state: HomeUiState,
    onCreateChallenge: () -> Unit,
    onInviteBuddies: () -> Unit,
    onChallengeClick: (String) -> Unit,
    onNotifications: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(
            title = "BuddyWorkout",
            actions = {
                BwIconButton(
                    icon = BwIcons.Bell,
                    onClick = onNotifications,
                    contentDescription = "Notifications",
                )
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            item {
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = BwColors.Ink,
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(BwSpace.Md)) {
                    ActionTile(
                        title = "Create challenge",
                        description = "Pick buddies and a deadline",
                        icon = BwIcons.Plus,
                        onClick = onCreateChallenge,
                        modifier = Modifier.weight(1f),
                    )
                    ActionTile(
                        title = "Invite buddies",
                        description = "Share your invite link",
                        icon = BwIcons.PersonAdd,
                        onClick = onInviteBuddies,
                        filled = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            if (state.atChallengeLimit) {
                item {
                    NoticeCard(
                        text = "You're in 2 active challenges — the maximum. Finish or cancel one to start another.",
                        icon = BwIcons.Info,
                    )
                }
            }

            item {
                SectionTitle(text = "Active challenges", hint = state.slotsLabel)
            }

            if (state.activeChallenges.isEmpty()) {
                item {
                    Text(
                        text = "No active challenges yet. Create one and pull a buddy in.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BwColors.Muted,
                    )
                }
            } else {
                items(state.activeChallenges.size) { index ->
                    val challenge = state.activeChallenges[index]
                    ChallengeCard(
                        title = challenge.title,
                        summary = challenge.summary,
                        progress = challenge.progress,
                        stat = challenge.stat,
                        onClick = { onChallengeClick(challenge.id) },
                        trailing = { AvatarStack(avatars = challenge.members) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun HomeScreenPreview() = BuddyWorkoutTheme {
    HomeScreen(PreviewData.home, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun HomeScreenEmptyPreview() = BuddyWorkoutTheme {
    HomeScreen(PreviewData.home.copy(activeChallenges = emptyList()), {}, {}, {}, {})
}
```

- [ ] **Step 3: Write ProfileUiState**

Create `app/src/main/java/com/example/buddyworkout/feature/profile/ProfileUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.profile

import com.example.buddyworkout.core.ui.component.AvatarUi

/** One label/value pair in the profile stats card. Display-ready strings only. */
data class StatUi(val label: String, val value: String)

data class ProfileUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val name: String = "",
    val email: String = "",
    val avatar: AvatarUi = AvatarUi(""),
    val stats: List<StatUi> = emptyList(),
    /** Debug builds surface the component gallery from here. */
    val showGallery: Boolean = false,
)
```

- [ ] **Step 4: Write ProfileScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/profile/ProfileScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.Avatar
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onSignOut: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Profile")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Spacer(Modifier.height(BwSpace.Sm))
            Avatar(avatar = state.avatar, size = 84.dp, ring = true)

            Text(
                text = state.name,
                style = MaterialTheme.typography.headlineSmall,
                color = BwColors.Ink,
            )
            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                color = BwColors.Muted,
            )

            Spacer(Modifier.height(BwSpace.Sm))

            BwCard(modifier = Modifier.fillMaxWidth()) {
                state.stats.forEachIndexed { index, stat ->
                    DetailRow(label = stat.label, value = stat.value)
                    if (index != state.stats.lastIndex) Spacer(Modifier.height(BwSpace.Sm))
                }
            }

            Spacer(Modifier.height(BwSpace.Sm))

            BwButton(
                text = "Sign out",
                onClick = onSignOut,
                variant = BwButtonVariant.DangerGhost,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.showGallery) {
                BwButton(
                    text = "Component gallery (debug)",
                    onClick = onOpenGallery,
                    variant = BwButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ProfileScreenPreview() = BuddyWorkoutTheme {
    ProfileScreen(PreviewData.profile, {}, {})
}
```

- [ ] **Step 5: Add the fakes**

In `core/ui/preview/PreviewData.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.home.HomeUiState
import com.example.buddyworkout.feature.profile.ProfileUiState
import com.example.buddyworkout.feature.profile.StatUi
```

and properties inside `object PreviewData`:

```kotlin
    val home = HomeUiState(
        greeting = "Hey, Anurag",
        activeChallenges = challenges,
        atChallengeLimit = true,
    )

    val profile = ProfileUiState(
        name = "Anurag S.",
        email = "anurag@example.com",
        avatar = me,
        stats = listOf(
            StatUi("Challenges won", "3"),
            StatUi("Total reps", "1,482"),
            StatUi("Best session", "62 reps"),
        ),
        showGallery = true,
    )
```

- [ ] **Step 6: Wire both into the NavHost**

In `core/navigation/BuddyWorkoutNavHost.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.home.HomeScreen
import com.example.buddyworkout.feature.profile.ProfileScreen
```

Replace the `composable<Home>` and `composable<Profile>` blocks with:

```kotlin
            composable<Home> {
                HomeScreen(
                    state = PreviewData.home,
                    onCreateChallenge = { navController.navigate(CreateGraph) },
                    onInviteBuddies = { navController.navigate(Invite) },
                    onChallengeClick = { id -> navController.navigate(ChallengeDetail(id)) },
                    onNotifications = {},
                )
            }
            composable<Profile> {
                ProfileScreen(
                    state = PreviewData.profile.copy(showGallery = BuildConfig.DEBUG),
                    onSignOut = { navController.navigate(Login) { popUpTo(0) } },
                    onOpenGallery = { navController.navigate(Gallery) },
                )
            }
```

- [ ] **Step 7: Build and verify**

Run: `./gradlew :app:installDebug`
Expected: `BUILD SUCCESSFUL`.

Verify by hand: Home shows the greeting, two action tiles side by side, the amber limit notice, "Active challenges 2 of 2", and two challenge cards with avatar stacks. Tapping a card opens the challenge placeholder with that id in the title. The Profile tab shows the avatar, stats card, red-ghost Sign out, and the gallery button. The bottom bar stays visible on both.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/feature/home/ \
        app/src/main/java/com/example/buddyworkout/feature/profile/ \
        app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt \
        app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt
git commit -m "feat: add Home and Profile screens on fake state"
```

---

### Task 9: Challenges list and Challenge detail

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/ChallengesUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/ChallengesScreen.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/ChallengeDetailUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/ChallengeDetailScreen.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt`

**Interfaces:**
- Consumes: `ChallengeUi`, `ParticipantUi`, `PreviewData` (Task 6).
- Produces: `ChallengesUiState`, `ChallengeDetailUiState`, `ChallengesScreen(...)`, `ChallengeDetailScreen(...)`, `PreviewData.challengesTab`, `PreviewData.challengeDetail`.

- [ ] **Step 1: Write ChallengesUiState**

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/ChallengesUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge

import com.example.buddyworkout.core.ui.model.ChallengeUi

data class ChallengesUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** 0 = Active, 1 = Completed. */
    val selectedTab: Int = 0,
    val active: List<ChallengeUi> = emptyList(),
    val completed: List<ChallengeUi> = emptyList(),
) {
    val visible: List<ChallengeUi> get() = if (selectedTab == 0) active else completed

    val emptyMessage: String
        get() = if (selectedTab == 0) {
            "No active challenges. Create one from Home."
        } else {
            "Nothing finished yet — your completed challenges will land here."
        }
}
```

- [ ] **Step 2: Write ChallengesScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/ChallengesScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.BwSegmentedTabs
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.ChallengeCard
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.PillTone
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ChallengesScreen(
    state: ChallengesUiState,
    onTabSelect: (Int) -> Unit,
    onChallengeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Challenges")

        BwSegmentedTabs(
            options = listOf("Active", "Completed"),
            selectedIndex = state.selectedTab,
            onSelect = onTabSelect,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = BwSpace.Gutter, vertical = BwSpace.Sm),
        )

        if (state.visible.isEmpty()) {
            Text(
                text = state.emptyMessage,
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
                modifier = Modifier.padding(BwSpace.Gutter),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(BwSpace.Gutter),
                verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
            ) {
                items(state.visible.size) { index ->
                    val challenge = state.visible[index]
                    ChallengeCard(
                        title = challenge.title,
                        summary = challenge.summary,
                        progress = challenge.progress,
                        stat = challenge.stat,
                        onClick = { onChallengeClick(challenge.id) },
                        accent = if (challenge.isCompleted) BwColors.Muted else BwColors.Primary,
                        trailing = {
                            if (challenge.isCompleted) {
                                Pill(text = "Ended", tone = PillTone.Neutral)
                            } else {
                                AvatarStack(avatars = challenge.members)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengesActivePreview() = BuddyWorkoutTheme {
    ChallengesScreen(PreviewData.challengesTab, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengesCompletedPreview() = BuddyWorkoutTheme {
    ChallengesScreen(PreviewData.challengesTab.copy(selectedTab = 1), {}, {})
}
```

- [ ] **Step 3: Write ChallengeDetailUiState**

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/ChallengeDetailUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge

import com.example.buddyworkout.core.ui.component.AvatarUi
import com.example.buddyworkout.core.ui.model.ParticipantUi

data class ChallengeDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "Pushup challenge",
    /** e.g. `"2d 14h left"` or `"Ended"` — produced by `formatRemaining`. */
    val remaining: String = "",
    /** e.g. `"Mon 18 Aug, 06:00"`. */
    val startsAt: String = "",
    val endsAt: String = "",
    val members: List<AvatarUi> = emptyList(),
    val leaderboard: List<ParticipantUi> = emptyList(),
    val isCreator: Boolean = false,
    val isCompleted: Boolean = false,
)
```

- [ ] **Step 4: Write ChallengeDetailScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/ChallengeDetailScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.CountdownCard
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.Pill
import com.example.buddyworkout.core.ui.component.PillTone
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ChallengeDetailScreen(
    state: ChallengeDetailUiState,
    onBack: () -> Unit,
    onRecordWorkout: () -> Unit,
    onSeeWinner: () -> Unit,
    onCancelChallenge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(
            title = state.title,
            overline = "CHALLENGE",
            onBack = onBack,
            actions = {
                Pill(
                    text = if (state.isCompleted) "Ended" else "Live",
                    tone = if (state.isCompleted) PillTone.Neutral else PillTone.Live,
                )
            },
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            item {
                CountdownCard(remaining = state.remaining, members = state.members)
            }

            item {
                BwCard(modifier = Modifier.fillMaxWidth()) {
                    DetailRow(label = "Starts", value = state.startsAt)
                    DetailRow(label = "Ends", value = state.endsAt)
                    DetailRow(label = "Exercise", value = "Pushups")
                }
            }

            item { SectionTitle(text = "Leaderboard", hint = "Live") }

            item {
                BwCard(modifier = Modifier.fillMaxWidth()) {
                    state.leaderboard.forEachIndexed { index, participant ->
                        LeaderboardRow(
                            avatar = participant.avatar,
                            name = participant.name,
                            rank = participant.rank,
                            subtitle = participant.subtitle,
                            value = participant.reps,
                            tone = when {
                                state.isCompleted && participant.rank == 1 -> RowTone.Gold
                                participant.isMe -> RowTone.Highlight
                                else -> RowTone.Plain
                            },
                            showDivider = index != state.leaderboard.lastIndex,
                        )
                    }
                }
            }

            item {
                if (state.isCompleted) {
                    BwButton(
                        text = "See the winner",
                        onClick = onSeeWinner,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    BwButton(
                        text = "Record workout",
                        onClick = onRecordWorkout,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            if (state.isCreator && !state.isCompleted) {
                item {
                    BwButton(
                        text = "Cancel challenge",
                        onClick = onCancelChallenge,
                        variant = BwButtonVariant.DangerGhost,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
private fun ChallengeDetailPreview() = BuddyWorkoutTheme {
    ChallengeDetailScreen(PreviewData.challengeDetail, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 900)
@Composable
private fun ChallengeDetailCompletedPreview() = BuddyWorkoutTheme {
    ChallengeDetailScreen(
        PreviewData.challengeDetail.copy(isCompleted = true, remaining = "Ended"),
        {}, {}, {}, {},
    )
}
```

- [ ] **Step 5: Add the fakes**

In `core/ui/preview/PreviewData.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.challenge.ChallengeDetailUiState
import com.example.buddyworkout.feature.challenge.ChallengesUiState
```

and properties inside `object PreviewData`:

```kotlin
    val challengesTab = ChallengesUiState(
        active = challenges,
        completed = completedChallenges,
    )

    val challengeDetail = ChallengeDetailUiState(
        remaining = "2d 14h left",
        startsAt = "Mon 18 Aug, 06:00",
        endsAt = "Wed 20 Aug, 06:00",
        members = people,
        leaderboard = leaderboard,
        isCreator = true,
    )
```

- [ ] **Step 6: Wire both into the NavHost**

In `core/navigation/BuddyWorkoutNavHost.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.challenge.ChallengeDetailScreen
import com.example.buddyworkout.feature.challenge.ChallengesScreen
```

Replace the `composable<Challenges>` and `composable<ChallengeDetail>` blocks with:

```kotlin
            composable<Challenges> {
                ChallengesScreen(
                    state = PreviewData.challengesTab,
                    onTabSelect = {},
                    onChallengeClick = { id -> navController.navigate(ChallengeDetail(id)) },
                )
            }
            composable<ChallengeDetail> { entry ->
                val route = entry.toRoute<ChallengeDetail>()
                ChallengeDetailScreen(
                    state = PreviewData.challengeDetail,
                    onBack = { navController.popBackStack() },
                    onRecordWorkout = { navController.navigate(Record(route.id)) },
                    onSeeWinner = { navController.navigate(Winner(route.id)) },
                    onCancelChallenge = { navController.popBackStack() },
                )
            }
```

The tab selection is inert here — `onTabSelect = {}` — because the tab index lives in state that only a ViewModel can change. Verify both tabs through the `@Preview`s instead.

- [ ] **Step 7: Build and verify**

Run: `./gradlew :app:installDebug`
Expected: `BUILD SUCCESSFUL`.

Verify by hand: the Challenges tab shows the segmented control and two active cards. Opening a challenge shows the countdown card with the avatar stack, the starts/ends card, a four-row leaderboard with your own row highlighted, a "Record workout" button, and a red-ghost "Cancel challenge". Back returns to the list. In Android Studio, confirm all four `@Preview`s across the two files render, including the completed variants.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/feature/challenge/ \
        app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt \
        app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt
git commit -m "feat: add Challenges list and Challenge detail screens on fake state"
```

---

### Task 10: Create flow (Create, BuddyPicker, DateTimePicker)

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/create/CreateChallengeUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/create/CreateChallengeScreen.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/create/BuddyPickerUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/create/BuddyPickerScreen.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/create/DateTimePickerUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/challenge/create/DateTimePickerScreen.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt`

**Interfaces:**
- Consumes: `BuddyUi`, `PreviewData` (Task 6).
- Produces: `CreateChallengeUiState`, `BuddyPickerUiState`, `DateTimePickerUiState`, the three screens, and `PreviewData.createChallenge`, `PreviewData.buddyPicker`, `PreviewData.dateTimePicker`.

Reminder: these three screens will later share one `CreateChallengeViewModel` scoped to the `CreateGraph` nested graph. That wiring is **not** part of this plan — here they are three independent stateless screens fed separate fakes.

- [ ] **Step 1: Write the three state classes**

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/create/CreateChallengeUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.core.ui.model.BuddyUi

data class CreateChallengeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedBuddies: List<BuddyUi> = emptyList(),
    /** e.g. `"Now"` or `"Mon 18 Aug, 06:00"`. */
    val startLabel: String = "Now",
    /** Empty until the user picks an end. */
    val endLabel: String = "",
    /** e.g. `"3 days"`. */
    val durationLabel: String = "",
) {
    val buddiesLabel: String
        get() = when (selectedBuddies.size) {
            0 -> "None yet"
            1 -> "1 buddy"
            else -> "${selectedBuddies.size} buddies"
        }

    val canCreate: Boolean
        get() = selectedBuddies.isNotEmpty() && endLabel.isNotBlank() && !isLoading
}
```

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/create/BuddyPickerUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge.create

import com.example.buddyworkout.core.ui.model.BuddyUi

/** A challenge holds 2–4 members including the creator, so at most 3 buddies. */
const val MAX_BUDDIES = 3

data class BuddyPickerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val buddies: List<BuddyUi> = emptyList(),
) {
    val selectedCount: Int get() = buddies.count { it.selected }
    val atLimit: Boolean get() = selectedCount >= MAX_BUDDIES
    val doneLabel: String get() = if (selectedCount == 0) "Done" else "Done ($selectedCount)"
}
```

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/create/DateTimePickerUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge.create

data class DateTimePickerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Preset durations, e.g. `"6 hours"`, `"1 day"`, `"1 week"`. */
    val presets: List<String> = emptyList(),
    /** -1 when nothing is chosen yet. */
    val selectedPresetIndex: Int = -1,
    /** e.g. `"Now (Mon 18 Aug, 06:00)"`. */
    val startLabel: String = "",
    /** Empty until a preset is chosen. */
    val endLabel: String = "",
) {
    val canSave: Boolean get() = selectedPresetIndex >= 0 && !isLoading
}
```

- [ ] **Step 2: Write CreateChallengeScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/create/CreateChallengeScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.AvatarStack
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun CreateChallengeScreen(
    state: CreateChallengeUiState,
    onBack: () -> Unit,
    onPickBuddies: () -> Unit,
    onPickWindow: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "New challenge", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            NoticeCard(
                text = "Pushups only for now. Everyone in the challenge gets the same window, and the winner is whoever logs the most reps.",
                icon = BwIcons.Info,
            )

            state.error?.let { message ->
                NoticeCard(
                    text = message,
                    icon = BwIcons.AlertTriangle,
                    containerColor = BwColors.DangerTint,
                    contentColor = BwColors.DangerInk,
                )
            }

            PickerRow(
                icon = BwIcons.Users,
                label = "Buddies",
                value = state.buddiesLabel,
                onClick = onPickBuddies,
                trailing = {
                    if (state.selectedBuddies.isNotEmpty()) {
                        AvatarStack(avatars = state.selectedBuddies.map { it.avatar })
                    }
                },
            )

            PickerRow(
                icon = BwIcons.Calendar,
                label = "Starts",
                value = state.startLabel,
                onClick = onPickWindow,
            )

            PickerRow(
                icon = BwIcons.Clock,
                label = "Ends",
                value = state.endLabel.ifBlank { "Not set" },
                onClick = onPickWindow,
            )

            if (state.durationLabel.isNotBlank()) {
                Text(
                    text = "Runs for ${state.durationLabel}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BwColors.Muted,
                )
            }

            BwButton(
                text = if (state.isLoading) "Creating…" else "Create challenge",
                onClick = onCreate,
                enabled = state.canCreate,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PickerRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    BwCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BwColors.Primary,
                modifier = Modifier.size(20.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = BwSpace.Md),
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = BwColors.Ink)
                Text(value, style = MaterialTheme.typography.bodyMedium, color = BwColors.Muted)
            }
            trailing?.invoke()
            Icon(
                imageVector = BwIcons.ChevronRight,
                contentDescription = null,
                tint = BwColors.Placeholder,
                modifier = Modifier
                    .padding(start = BwSpace.Sm)
                    .size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun CreateChallengeFilledPreview() = BuddyWorkoutTheme {
    CreateChallengeScreen(PreviewData.createChallenge, {}, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun CreateChallengeEmptyPreview() = BuddyWorkoutTheme {
    CreateChallengeScreen(CreateChallengeUiState(), {}, {}, {}, {})
}
```

- [ ] **Step 3: Write BuddyPickerScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/create/BuddyPickerScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTextField
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun BuddyPickerScreen(
    state: BuddyPickerUiState,
    onQueryChange: (String) -> Unit,
    onToggleBuddy: (String) -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Add buddies", onBack = onBack)

        BwTextField(
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = "Search your buddies",
            trailingIcon = BwIcons.Search,
            modifier = Modifier.padding(horizontal = BwSpace.Gutter, vertical = BwSpace.Sm),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            if (state.atLimit) {
                item {
                    NoticeCard(
                        text = "That's $MAX_BUDDIES buddies — the most a challenge holds. Deselect someone to swap.",
                        icon = BwIcons.Info,
                    )
                }
            }

            item {
                BwCard(modifier = Modifier.fillMaxWidth()) {
                    state.buddies.forEachIndexed { index, buddy ->
                        LeaderboardRow(
                            avatar = buddy.avatar,
                            name = buddy.name,
                            subtitle = buddy.subtitle,
                            tone = if (buddy.selected) RowTone.Highlight else RowTone.Plain,
                            showDivider = index != state.buddies.lastIndex,
                            onClick = { onToggleBuddy(buddy.uid) },
                            trailing = {
                                if (buddy.selected) {
                                    Icon(
                                        imageVector = BwIcons.Check,
                                        contentDescription = "Selected",
                                        tint = BwColors.Primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }

        BwButton(
            text = state.doneLabel,
            onClick = onDone,
            enabled = state.selectedCount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(BwSpace.Gutter),
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun BuddyPickerPreview() = BuddyWorkoutTheme {
    BuddyPickerScreen(PreviewData.buddyPicker, {}, {}, {}, {})
}
```

- [ ] **Step 4: Write DateTimePickerScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/challenge/create/DateTimePickerScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.challenge.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun DateTimePickerScreen(
    state: DateTimePickerUiState,
    onSelectPreset: (Int) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Start & end", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Text(
                text = "The challenge starts as soon as you create it and closes automatically at the deadline.",
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
            )

            SectionTitle(text = "How long should it run?")

            BwCard(modifier = Modifier.fillMaxWidth()) {
                state.presets.forEachIndexed { index, preset ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = BwSpace.Sm),
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (index == state.selectedPresetIndex) BwColors.Primary else BwColors.Ink,
                            modifier = Modifier.weight(1f),
                        )
                        if (index == state.selectedPresetIndex) {
                            Icon(
                                imageVector = BwIcons.Check,
                                contentDescription = "Selected",
                                tint = BwColors.Primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            BwCard(modifier = Modifier.fillMaxWidth()) {
                DetailRow(label = "Starts", value = state.startLabel)
                DetailRow(
                    label = "Ends",
                    value = state.endLabel.ifBlank { "Pick a duration" },
                    valueColor = if (state.endLabel.isBlank()) BwColors.Placeholder else BwColors.Ink,
                )
            }

            BwButton(
                text = "Save",
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun DateTimePickerPreview() = BuddyWorkoutTheme {
    DateTimePickerScreen(PreviewData.dateTimePicker, {}, {}, {})
}
```

- [ ] **Step 5: Add the fakes**

In `core/ui/preview/PreviewData.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.challenge.create.BuddyPickerUiState
import com.example.buddyworkout.feature.challenge.create.CreateChallengeUiState
import com.example.buddyworkout.feature.challenge.create.DateTimePickerUiState
```

and properties inside `object PreviewData`:

```kotlin
    val createChallenge = CreateChallengeUiState(
        selectedBuddies = buddies.filter { it.selected },
        startLabel = "Now",
        endLabel = "Wed 20 Aug, 06:00",
        durationLabel = "2 days",
    )

    val buddyPicker = BuddyPickerUiState(buddies = buddies)

    val dateTimePicker = DateTimePickerUiState(
        presets = listOf("6 hours", "12 hours", "1 day", "3 days", "1 week", "2 weeks"),
        selectedPresetIndex = 3,
        startLabel = "Now (Mon 18 Aug, 06:00)",
        endLabel = "Thu 21 Aug, 06:00",
    )
```

- [ ] **Step 6: Wire the create graph into the NavHost**

In `core/navigation/BuddyWorkoutNavHost.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.challenge.create.BuddyPickerScreen
import com.example.buddyworkout.feature.challenge.create.CreateChallengeScreen
import com.example.buddyworkout.feature.challenge.create.DateTimePickerScreen
```

Replace the whole `navigation<CreateGraph>` block with:

```kotlin
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
                    BuddyPickerScreen(
                        state = PreviewData.buddyPicker,
                        onQueryChange = {},
                        onToggleBuddy = {},
                        onDone = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<DateTimePicker> {
                    DateTimePickerScreen(
                        state = PreviewData.dateTimePicker,
                        onSelectPreset = {},
                        onSave = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
```

- [ ] **Step 7: Build and verify**

Run: `./gradlew :app:installDebug`
Expected: `BUILD SUCCESSFUL`.

Verify by hand: Home → "Create challenge" opens the real create screen with the info notice, three picker rows, an avatar stack on the Buddies row, and an enabled "Create challenge" button. "Buddies" pushes the picker with two rows selected and a "Done (2)" button; "Ends" pushes the duration screen with "3 days" ticked. Both pop back to Create. "Create challenge" lands on the challenge detail with the whole create graph popped — system back goes to Home.

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/feature/challenge/create/ \
        app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt \
        app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt
git commit -m "feat: add create-challenge flow screens on fake state"
```

---

### Task 11: Invite screens

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/feature/invite/InviteUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/invite/InviteScreen.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/invite/ChallengeInviteUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/invite/ChallengeInviteScreen.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt`

**Interfaces:**
- Consumes: `AvatarUi`, `PreviewData` (Task 6).
- Produces: `InviteUiState`, `ChallengeInviteUiState`, `InviteScreen(...)`, `ChallengeInviteScreen(...)`, `PreviewData.invite`, `PreviewData.challengeInvite`.

- [ ] **Step 1: Write the state classes**

Create `app/src/main/java/com/example/buddyworkout/feature/invite/InviteUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.invite

data class InviteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** The full shareable URL, e.g. `"https://commworkout.app/i/9F3KQ2"`. */
    val link: String = "",
    /** Just the code, shown large so it can be read aloud. */
    val code: String = "",
    val copied: Boolean = false,
)
```

Create `app/src/main/java/com/example/buddyworkout/feature/invite/ChallengeInviteUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.invite

import com.example.buddyworkout.core.ui.component.AvatarUi

data class ChallengeInviteUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /** e.g. `"Rahul K. invited you"`. */
    val headline: String = "",
    val title: String = "Pushup challenge",
    val members: List<AvatarUi> = emptyList(),
    /** e.g. `"2d 14h left"`. */
    val remaining: String = "",
    val startsAt: String = "",
    val endsAt: String = "",
    /** True when the viewer is already at the 2-active-challenge cap. */
    val blockedByLimit: Boolean = false,
) {
    val canAccept: Boolean get() = !blockedByLimit && !isLoading && error == null
}
```

- [ ] **Step 2: Write InviteScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/invite/InviteScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwIconButton
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun InviteScreen(
    state: InviteUiState,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Invite buddies", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            NoticeCard(
                text = "Anyone with this link can join you. It never expires — share it in any chat.",
                icon = BwIcons.Info,
            )

            SectionTitle(text = "Your invite code")

            BwCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = state.code,
                    style = MaterialTheme.typography.headlineMedium,
                    color = BwColors.Primary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = BwSpace.Sm),
                ) {
                    Text(
                        text = state.link,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BwColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    BwIconButton(
                        icon = BwIcons.Link,
                        onClick = onCopyLink,
                        contentDescription = "Copy link",
                        tinted = true,
                    )
                }
            }

            if (state.copied) {
                Text(
                    text = "Copied to clipboard",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BwColors.Primary,
                )
            }

            BwButton(
                text = "Share link",
                onClick = onShareLink,
                icon = BwIcons.Share,
                modifier = Modifier.fillMaxWidth(),
            )
            BwButton(
                text = "Copy link",
                onClick = onCopyLink,
                variant = BwButtonVariant.Outline,
                icon = BwIcons.Link,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun InviteScreenPreview() = BuddyWorkoutTheme {
    InviteScreen(PreviewData.invite, {}, {}, {})
}
```

- [ ] **Step 3: Write ChallengeInviteScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/invite/ChallengeInviteScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.CountdownCard
import com.example.buddyworkout.core.ui.component.DetailRow
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun ChallengeInviteScreen(
    state: ChallengeInviteUiState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Challenge invite")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            Text(
                text = state.headline,
                style = MaterialTheme.typography.headlineSmall,
                color = BwColors.Ink,
            )
            Text(
                text = "Join the challenge and every rep you log counts towards the leaderboard.",
                style = MaterialTheme.typography.bodyLarge,
                color = BwColors.Muted,
            )

            state.error?.let { message ->
                NoticeCard(
                    text = message,
                    icon = BwIcons.AlertTriangle,
                    containerColor = BwColors.DangerTint,
                    contentColor = BwColors.DangerInk,
                )
            }

            if (state.blockedByLimit) {
                NoticeCard(
                    text = "You're already in 2 active challenges. Finish or cancel one before joining this.",
                    icon = BwIcons.Info,
                )
            }

            CountdownCard(remaining = state.remaining, members = state.members)

            BwCard(modifier = Modifier.fillMaxWidth()) {
                DetailRow(label = "Exercise", value = "Pushups")
                DetailRow(label = "Starts", value = state.startsAt)
                DetailRow(label = "Ends", value = state.endsAt)
            }

            Spacer(Modifier.height(BwSpace.Xs))

            BwButton(
                text = "Accept & join",
                onClick = onAccept,
                enabled = state.canAccept,
                modifier = Modifier.fillMaxWidth(),
            )
            BwButton(
                text = "Not now",
                onClick = onDecline,
                variant = BwButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengeInvitePreview() = BuddyWorkoutTheme {
    ChallengeInviteScreen(PreviewData.challengeInvite, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun ChallengeInviteBlockedPreview() = BuddyWorkoutTheme {
    ChallengeInviteScreen(PreviewData.challengeInvite.copy(blockedByLimit = true), {}, {})
}
```

- [ ] **Step 4: Add the fakes**

In `core/ui/preview/PreviewData.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.invite.ChallengeInviteUiState
import com.example.buddyworkout.feature.invite.InviteUiState
```

and properties inside `object PreviewData`:

```kotlin
    val invite = InviteUiState(
        link = "https://commworkout.app/i/9F3KQ2",
        code = "9F3KQ2",
    )

    val challengeInvite = ChallengeInviteUiState(
        headline = "Rahul K. invited you",
        members = people.take(3),
        remaining = "2d 14h left",
        startsAt = "Mon 18 Aug, 06:00",
        endsAt = "Wed 20 Aug, 06:00",
    )
```

- [ ] **Step 5: Wire both into the NavHost**

In `core/navigation/BuddyWorkoutNavHost.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.invite.ChallengeInviteScreen
import com.example.buddyworkout.feature.invite.InviteScreen
```

Replace the `composable<Invite>` and `composable<ChallengeInvite>` blocks with:

```kotlin
            composable<Invite> {
                InviteScreen(
                    state = PreviewData.invite,
                    onCopyLink = {},
                    onShareLink = {},
                    onBack = { navController.popBackStack() },
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
```

- [ ] **Step 6: Build and verify**

Run: `./gradlew :app:installDebug`
Expected: `BUILD SUCCESSFUL`.

Then re-run the deep link check from Task 5:

```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://commworkout.app/c/abc123" com.example.buddyworkout
```

Expected: the app opens on the real **Challenge invite** screen with the countdown card. "Accept & join" lands on the challenge detail for `abc123`, and system back exits rather than returning to the invite. Also check Home → "Invite buddies" shows the code `9F3KQ2` with the link row and both buttons.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/feature/invite/ \
        app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt \
        app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt
git commit -m "feat: add invite and challenge-invite screens on fake state"
```

---

### Task 12: Record and Winner screens, and placeholder removal

**Files:**
- Create: `app/src/main/java/com/example/buddyworkout/feature/record/RecordUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/record/RecordScreen.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/result/WinnerUiState.kt`
- Create: `app/src/main/java/com/example/buddyworkout/feature/result/WinnerScreen.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt`
- Modify: `app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt`
- Delete: `app/src/main/java/com/example/buddyworkout/core/navigation/PlaceholderScreen.kt`

**Interfaces:**
- Consumes: `ParticipantUi`, `PreviewData` (Task 6), `CameraOverlay`, `WinnerBanner` (existing DLS).
- Produces: `RecordUiState`, `WinnerUiState`, `RecordScreen(...)`, `WinnerScreen(...)`, `PreviewData.record`, `PreviewData.winner`.

`RecordScreen` renders the overlay only — no CameraX, no ML Kit. The `cameraPreview` slot stays empty, showing the dark `BwColors.CameraBg` surface. The camera pipeline is step 6 of the spec's build order and belongs to a later plan.

- [ ] **Step 1: Write the state classes**

Create `app/src/main/java/com/example/buddyworkout/feature/record/RecordUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.record

data class RecordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val reps: Int = 0,
    /** e.g. `"Good form"`, or null when nothing is being judged. */
    val formLabel: String? = null,
    /** e.g. `"04:12"`. */
    val elapsedLabel: String = "00:00",
    val isRunning: Boolean = false,
    val hasCameraPermission: Boolean = false,
    /** e.g. `"Position yourself in frame"`, shown when the pose is not readable. */
    val guidance: String? = null,
)
```

Create `app/src/main/java/com/example/buddyworkout/feature/result/WinnerUiState.kt`:

```kotlin
package com.example.buddyworkout.feature.result

import com.example.buddyworkout.core.ui.model.ParticipantUi

data class WinnerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val winnerName: String = "",
    /** e.g. `"204 reps over 3 days"`. */
    val detail: String = "",
    val leaderboard: List<ParticipantUi> = emptyList(),
)
```

- [ ] **Step 2: Write RecordScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/record/RecordScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwButtonVariant
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.CameraOverlay
import com.example.buddyworkout.core.ui.component.NoticeCard
import com.example.buddyworkout.core.ui.icon.BwIcons
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun RecordScreen(
    state: RecordUiState,
    onGrantCameraPermission: () -> Unit,
    onStopAndSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Record workout", overline = "PUSHUPS", onBack = onBack)

        CameraOverlay(
            reps = state.reps,
            formLabel = state.guidance ?: state.formLabel,
            footerLabel = "Elapsed",
            footerValue = state.elapsedLabel,
            showPoseSkeleton = state.isRunning,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = BwSpace.Gutter, vertical = BwSpace.Md),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            if (!state.hasCameraPermission) {
                NoticeCard(
                    text = "Camera access is needed to count your reps. Frames never leave your phone — only the count is saved.",
                    icon = BwIcons.Video,
                )
                BwButton(
                    text = "Allow camera",
                    onClick = onGrantCameraPermission,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                BwButton(
                    text = "Stop & save",
                    onClick = onStopAndSave,
                    icon = BwIcons.Stop,
                    variant = BwButtonVariant.DangerGhost,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun RecordRunningPreview() = BuddyWorkoutTheme {
    RecordScreen(PreviewData.record, {}, {}, {})
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun RecordNeedsPermissionPreview() = BuddyWorkoutTheme {
    RecordScreen(
        PreviewData.record.copy(hasCameraPermission = false, isRunning = false, reps = 0),
        {}, {}, {},
    )
}
```

- [ ] **Step 3: Write WinnerScreen**

Create `app/src/main/java/com/example/buddyworkout/feature/result/WinnerScreen.kt`:

```kotlin
package com.example.buddyworkout.feature.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.buddyworkout.core.ui.component.BwButton
import com.example.buddyworkout.core.ui.component.BwCard
import com.example.buddyworkout.core.ui.component.BwTopBar
import com.example.buddyworkout.core.ui.component.LeaderboardRow
import com.example.buddyworkout.core.ui.component.RowTone
import com.example.buddyworkout.core.ui.component.SectionTitle
import com.example.buddyworkout.core.ui.component.WinnerBanner
import com.example.buddyworkout.core.ui.preview.PreviewData
import com.example.buddyworkout.core.ui.theme.BuddyWorkoutTheme
import com.example.buddyworkout.core.ui.theme.BwColors
import com.example.buddyworkout.core.ui.theme.BwSpace

@Composable
fun WinnerScreen(
    state: WinnerUiState,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BwColors.Bg),
    ) {
        BwTopBar(title = "Results", overline = "PUSHUP CHALLENGE")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(BwSpace.Gutter),
            verticalArrangement = Arrangement.spacedBy(BwSpace.Md),
        ) {
            WinnerBanner(winner = state.winnerName, detail = state.detail)

            SectionTitle(text = "Final leaderboard")

            BwCard(modifier = Modifier.fillMaxWidth()) {
                state.leaderboard.forEachIndexed { index, participant ->
                    LeaderboardRow(
                        avatar = participant.avatar,
                        name = participant.name,
                        rank = participant.rank,
                        subtitle = participant.subtitle,
                        value = participant.reps,
                        tone = when {
                            participant.rank == 1 -> RowTone.Gold
                            participant.isMe -> RowTone.Highlight
                            else -> RowTone.Plain
                        },
                        showDivider = index != state.leaderboard.lastIndex,
                    )
                }
            }

            BwButton(
                text = "Back to home",
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun WinnerScreenPreview() = BuddyWorkoutTheme {
    WinnerScreen(PreviewData.winner, {})
}
```

- [ ] **Step 4: Add the fakes**

In `core/ui/preview/PreviewData.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.record.RecordUiState
import com.example.buddyworkout.feature.result.WinnerUiState
```

and properties inside `object PreviewData`:

```kotlin
    val record = RecordUiState(
        reps = 17,
        formLabel = "Good form",
        elapsedLabel = "04:12",
        isRunning = true,
        hasCameraPermission = true,
    )

    val winner = WinnerUiState(
        winnerName = "Rahul K.",
        detail = "204 reps over 3 days",
        leaderboard = leaderboard,
    )
```

- [ ] **Step 5: Wire both into the NavHost and drop the placeholders**

In `core/navigation/BuddyWorkoutNavHost.kt`, add imports:

```kotlin
import com.example.buddyworkout.feature.record.RecordScreen
import com.example.buddyworkout.feature.result.WinnerScreen
```

Replace the `composable<Record>` and `composable<Winner>` blocks with:

```kotlin
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
```

Then remove the now-unused `import com.example.buddyworkout.core.navigation.PlaceholderScreen` reference — `PlaceholderScreen` is in the same package, so there is no import to delete, but confirm no call sites remain:

Run: `grep -rn "PlaceholderScreen" app/src/main/java`
Expected: only the declaration in `PlaceholderScreen.kt`. If any call site remains, that screen was missed — go back and wire it before continuing.

Delete the file:

```bash
rm app/src/main/java/com/example/buddyworkout/core/navigation/PlaceholderScreen.kt
```

Also note that `entry.toRoute<Record>()` and `entry.toRoute<Winner>()` are no longer used in these two blocks. Leave the `toRoute` import in place — `composable<ChallengeDetail>` and `composable<ChallengeInvite>` still use it.

- [ ] **Step 6: Full build and end-to-end click-through**

Run: `./gradlew :app:testDebugUnitTest :app:installDebug`
Expected: all unit tests PASS and `BUILD SUCCESSFUL`.

Walk the whole app and confirm every one of the 13 screens is real — no "PLACEHOLDER" overline anywhere:

1. Login → Register → back → Login → sign in → Home
2. Home → Create challenge → Add buddies → back → Ends → back → Create → Challenge detail
3. Challenge detail → Record workout → Stop & save → back on detail
4. Challenge detail → See the winner (open a completed challenge, or temporarily flip `PreviewData.challengeDetail.isCompleted` to check) → Winner → Back to home
5. Bottom bar: Home / Challenges / Profile, bar hidden on every non-root screen
6. Home → Invite buddies → Invite screen
7. `adb` deep link → Challenge invite → Accept → Challenge detail
8. Profile → Component gallery → back; Profile → Sign out → Login

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/example/buddyworkout/feature/record/ \
        app/src/main/java/com/example/buddyworkout/feature/result/ \
        app/src/main/java/com/example/buddyworkout/core/ui/preview/PreviewData.kt \
        app/src/main/java/com/example/buddyworkout/core/navigation/BuddyWorkoutNavHost.kt
git rm app/src/main/java/com/example/buddyworkout/core/navigation/PlaceholderScreen.kt
git commit -m "feat: add Record and Winner screens, remove navigation placeholders"
```

---

## Done when

- `./gradlew :app:testDebugUnitTest` passes (16 tests across `TimeFormatTest`, `RoutesTest`, `AppViewModelTest`).
- `./gradlew :app:installDebug` builds, and all 13 screens render real UI reachable from the navigation graph.
- `grep -rn "PlaceholderScreen" app/src/main/java` returns nothing.
- No `*Route.kt` or feature `*ViewModel.kt` exists — that is the next plan's work, which flips each `PreviewData.x` call site in `BuddyWorkoutNavHost.kt` to a `XRoute(...)` wrapper, one screen at a time.
