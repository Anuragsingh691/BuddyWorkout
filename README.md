# BuddyWorkout

A community fitness Android app where buddies run **group pushup challenges**, record workouts that an on-device AI counts, and compete on a live leaderboard.

## Status

MVP in development. Design and system architecture are complete; implementation is next.

- 📐 **Designs:** 12 screens across 5 flows (imported to Figma).
- 🏗️ **System design:** see [`docs/architecture/`](docs/architecture/README.md) — HLD → LLD, with Firebase as the backend.

## Tech stack

- **Kotlin** · **Jetpack Compose** + Material 3 · MVVM + Repository · Hilt · Navigation-Compose
- **Firebase** backend: Authentication (Email + Google), Cloud Firestore, Cloud Storage, Cloud Functions
- **CameraX** + **ML Kit Pose Detection** for on-device pushup rep counting
- minSdk 24 · targetSdk 36 · package `com.example.buddyworkout`

## MVP scope

- Pushups only; **group** challenges (2–4 members), generically titled "Pushup challenge"
- A user can be in **at most 2 active challenges**
- **Time-precise** duration (hours → weeks); **auto-completes** at the deadline and declares the winner by reps (creator can cancel; no early win)
- Link-based invites (Android App Links + Firestore invite codes)

## Architecture docs

| Doc | Contents |
|-----|----------|
| [01-hld.md](docs/architecture/01-hld.md) | System context, containers, tech stack, navigation, NFRs |
| [02-firebase-design.md](docs/architecture/02-firebase-design.md) | Firebase backend: Auth, Firestore, Storage, invites, Functions, rules |
| [03-data-model.md](docs/architecture/03-data-model.md) | Firestore ER model, collections, state machine, indexes |
| [04-lld-android.md](docs/architecture/04-lld-android.md) | App layers, DI, repositories, sequence diagrams, Gradle deps |
| [05-lld-pushup-counter.md](docs/architecture/05-lld-pushup-counter.md) | CameraX + ML Kit rep-counting design |

## Build

Standard Android Studio project. Requires a Firebase project and `app/google-services.json` (not committed) once backend wiring begins.
