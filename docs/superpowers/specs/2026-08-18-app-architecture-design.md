# App architecture — navigation, screens, and module structure

**Date:** 2026-08-18
**Status:** Approved
**Scope:** How the BuddyWorkout MVP is structured as an Android app — module layout, navigation, and the screen/state contract. Complements [`docs/architecture/04-lld-android.md`](../../architecture/04-lld-android.md), which this document refines and in two places deliberately deviates from.

## 1. Context

The DLS is done: `core/ui/{theme,component,icon}` holds ~16 components plus a `ComponentGallery`, Hilt is wired (`BuddyWorkoutApp`, `@AndroidEntryPoint MainActivity`, `core/di/FirebaseModule`), and Firebase Auth/Firestore/Storage/Functions dependencies are in place. `MainActivity` currently renders `ComponentGallery()` directly — there is no `NavHost` yet.

The goal driving every decision below: **ship the MVP fast, solo.** Where a "more correct" pattern costs setup time without paying back at this size, it is explicitly skipped and the skip is recorded.

## 2. Decisions

### 2.1 Single Gradle module

The MVP stays a single `:app` module, as [`01-hld.md`](../../architecture/01-hld.md) §3 already states. No `:core` / `:feature-*` / `:ai` split.

Rationale: a solo developer gets no boundary enforcement benefit worth the Gradle plumbing, and the cross-module DI and navigation glue would slow down the first screens. Revisit when a second developer joins or build times become painful.

Two conventions make a later split a folder move rather than a refactor:

1. `ai/` never imports from `feature/`, `data/`, or `domain/`. It exposes `RepCounter` (a pure, Android-free state machine) and `PoseAnalyzer` as interfaces.
2. `core/ui/` never imports from `feature/`, `data/`, or `domain/`. Screens map domain models to UI models; the DLS stays domain-ignorant.

These are conventions, not enforced rules. Kotlin's `internal` is module-wide and cannot express them, and a Konsist/lint rule is not worth adding for a solo project.

### 2.2 One Activity, one NavHost, no fragments

`MainActivity` keeps `@AndroidEntryPoint` and hosts a single `NavHost`. There are no fragments and no second Activity. `ComponentGallery` moves behind a debug-only route so it stays reachable during development.

### 2.3 Type-safe routes

Navigation Compose is at 2.9.8 with Kotlin 2.4.10, so routes are `@Serializable` objects and data classes rather than string paths:

```kotlin
// core/navigation/Routes.kt
@Serializable data object Login
@Serializable data object Register
@Serializable data object Home
@Serializable data object Challenges
@Serializable data object Profile
@Serializable data object Invite
@Serializable data class  ChallengeDetail(val id: String)
@Serializable data class  ChallengeInvite(val code: String)   // deep link target
@Serializable data class  Record(val challengeId: String)
@Serializable data class  Winner(val challengeId: String)
@Serializable data object CreateGraph        // nested graph
@Serializable data object Create             //   ├ start destination
@Serializable data object BuddyPicker        //   ├
@Serializable data object DateTimePicker     //   └
```

This replaces the string-route sketch in `04-lld-android.md` §4. Requires the `kotlin("plugin.serialization")` plugin and `kotlinx-serialization-json`.

### 2.4 Bottom navigation: one NavHost, conditional bottom bar

`Home`, `Challenges`, and `Profile` (the three entries of `BwNavItem` in `core/ui/component/BwBottomNav.kt`) are ordinary destinations in the single graph. A `Scaffold` renders `BwBottomNav` only when the current destination is one of those three; `Record`, `Create`, `ChallengeDetail`, and the rest render full-bleed.

**Skipped:** a nested `NavHost` per tab for independent per-tab back stacks. It is the more correct pattern but buys nothing when tabs are one level deep, and costs real complexity.

### 2.5 The Create flow shares one ViewModel via a nested graph

`Create`, `BuddyPicker`, and `DateTimePicker` collaborate on a single challenge draft. Rather than passing results back through `savedStateHandle`, `CreateChallengeViewModel` is scoped to the `CreateGraph` nested graph, and all three screens obtain it with `hiltViewModel(parentGraphBackStackEntry)`.

Picking buddies or a date mutates the shared draft in place; `Create` re-reads it. The draft is garbage-collected when the graph is popped.

### 2.6 Auth gate sits above the NavHost

A top-level `AppViewModel` exposes `authState: StateFlow<AuthState>` with `Loading`, `SignedOut`, and `SignedIn`. While `Loading`, `MainActivity` renders only the theme background. Once resolved, the `NavHost` is composed with `startDestination = if (signedIn) Home else Login`.

Sign-out navigates to `Login` with `popUpTo(0)`.

Gating above the `NavHost` rather than inside it avoids the flash of the login screen while Firebase restores the session.

### 2.7 Deep links stay declarative

`ChallengeInvite` declares `navDeepLink<ChallengeInvite>(basePath = "https://commworkout.app/c")` on its composable, paired with the manifest intent filter. No manual `onNewIntent` intent parsing.

### 2.8 Every screen is two composables

```kotlin
// feature/home/HomeScreen.kt — stateless: no Hilt, no NavController, previewable
@Composable fun HomeScreen(
    state: HomeUiState,
    onCreateChallenge: () -> Unit,
    onChallengeClick: (String) -> Unit,
)

// feature/home/HomeRoute.kt — the only Hilt-aware part
@Composable fun HomeRoute(onCreateChallenge: () -> Unit, onChallengeClick: (String) -> Unit) {
    val vm: HomeViewModel = hiltViewModel()
    HomeScreen(vm.uiState.collectAsStateWithLifecycle().value, onCreateChallenge, onChallengeClick)
}
```

Screens never receive a `NavController` or resolve a ViewModel; the `NavHost` supplies navigation lambdas. This separation is what makes the fake-state phase (§2.10) possible.

### 2.9 UI state is one data class per screen

```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val activeChallenges: List<ChallengeUi> = emptyList(),
)
```

**Deviation from `04-lld-android.md` §6**, which sketches `sealed interface ChallengeDetailUiState { Loading | Data | Error }`. Most MVP screens are forms — Login, Register, Create, BuddyPicker — and a sealed hierarchy discards typed-in form state on every transition to `Loading`. A flat data class with `isLoading` and `error` fields keeps one shape across all screens.

`ChallengeUi` and its siblings are **UI models**, not domain models: they carry preformatted strings such as `"2d 14h left"`, so composables never format durations or stringify domain types.

Events are individual lambdas (`onCreateChallenge: () -> Unit`), not a sealed `Event` type with a single `onEvent` sink. Revisit only if a screen exceeds roughly eight callbacks.

### 2.10 Build breadth-first on fake state, then plumb per feature

Phase 1 writes `XUiState` + stateless `XScreen` + fakes; the `NavHost` calls `HomeScreen(PreviewData.home, ...)` directly, with no ViewModel in existence. Phase 2 adds `HomeViewModel` + `HomeRoute` and flips a single call site in the `NavHost`. One feature at a time; no big-bang rewrite.

Fakes live in `core/ui/preview/PreviewData.kt` in the `main` source set — not `debug` — because `@Preview` functions depend on them permanently. The skeleton `NavHost` borrows the same objects, so the only code deleted in phase 2 is one call site per screen.

### 2.11 Thirteen screens, not twelve

The navigation map in `01-hld.md` §5 lists 12 screens, but `BwNavItem` defines a **Profile** tab with no designed screen behind it, and profile editing is out of MVP scope. `ProfileScreen` is therefore built as read-only: avatar, display name, and sign-out. This keeps the third tab from being dead without expanding scope.

## 3. Package structure

Additions to the layout in `04-lld-android.md` §1 are marked `+`.

```
com.example.buddyworkout
├─ MainActivity.kt · BuddyWorkoutApp.kt
├─ core/
│  ├─ navigation/        + Routes.kt, BuddyWorkoutNavHost.kt, AppViewModel.kt
│  ├─ ui/{theme,component,icon}          (exists)
│  ├─ ui/preview/        + PreviewData.kt
│  ├─ ui/gallery/                        (exists — moves behind a debug route)
│  ├─ di/                                (exists)
│  └─ common/            + Result, dispatchers, time formatting
├─ feature/{auth,home,invite,challenge,record,result,profile}/
│     each: XScreen.kt (stateless) · XRoute.kt · XViewModel.kt · XUiState.kt
├─ domain/{model,usecase}
├─ data/{auth,user,challenge,workout,model}
└─ ai/                   CameraX + ML Kit; added only at the Record phase
```

## 4. Testing

Narrow by intent:

- **Pure JVM unit tests** for the `RepCounter` state machine — synthetic pose sequences in, expected rep counts out, no camera required. This is why `ai/` exposes interfaces (§2.1).
- **ViewModel unit tests** against fake repositories. Repositories are already interfaces, so fakes are trivial.
- **No new instrumentation tests** for the MVP. Compose UI tests would largely re-assert what `@Preview` already demonstrates.

## 5. Build order

Each step leaves the app runnable.

| # | Step | Outcome |
|---|------|---------|
| 1 | Serialization plugin, `Routes.kt`, NavHost + auth gate, 13 placeholder screens | App boots; every route reachable; gallery on a debug route |
| 2 | All 13 stateless screens + `UiState` types + `PreviewData` | Full click-through demo on fake data |
| 3 | Auth vertical slice (repo → VM → Login/Register/Profile) | Real sign-in; auth gate goes live |
| 4 | Challenge read path (Home, Challenges, Detail live leaderboard) | Real Firestore data |
| 5 | Challenge write path (Create flow, invite, join deep link) | End-to-end challenge lifecycle |
| 6 | `ai/` — CameraX + ML Kit + Record screen | Reps counted on-device |
| 7 | Winner screen + Cloud Function deadline close | MVP complete |

## 6. Gradle changes

Only one, needed by §2.3:

- Add the `org.jetbrains.kotlin.plugin.serialization` plugin (version aligned to Kotlin 2.4.10) and the `kotlinx-serialization-json` dependency.

CameraX and ML Kit dependencies are deferred to step 6 rather than added up front.

## 7. Open items

Deferred by design, each with a named trigger for revisiting:

| Item | Decision | Revisit when |
|------|----------|--------------|
| Multi-module split | Not now | A second developer joins, or build times hurt |
| Per-tab back stacks | Not now | A tab grows a multi-level flow of its own |
| Boundary enforcement (Konsist/lint) | Not now | The `ai/` or `core/ui/` conventions get violated |
| Rep-counting model design | Separate design session | Before step 6; see `05-lld-pushup-counter.md` |
