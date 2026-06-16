# BuddyWorkout — architecture docs

System design for the **BuddyWorkout** MVP: a community fitness app where buddies run **group pushup challenges**, record workouts that an on-device AI counts, and compete on a live leaderboard.

This folder goes top-down: high-level design first, then the Firebase backend, then low-level design of the data model, the Android app, and the AI rep counter.

## Reading order

| # | Doc | What it covers |
|---|-----|----------------|
| 1 | [01-hld.md](01-hld.md) | **HLD** — system context, containers, tech stack, app layering, navigation map, NFRs |
| 2 | [02-firebase-design.md](02-firebase-design.md) | **Firebase as the backend** — Auth, Firestore, Storage, invite deep links, Cloud Functions, security rules, plan & cost |
| 3 | [03-data-model.md](03-data-model.md) | **LLD · data** — Firestore ER model, per-collection field tables, indexes, challenge state machine, invariants |
| 4 | [04-lld-android.md](04-lld-android.md) | **LLD · app** — package structure, layers (data/domain/ui), DI, repositories, per-flow sequence diagrams, nav routes, Gradle deps |
| 5 | [05-lld-pushup-counter.md](05-lld-pushup-counter.md) | **LLD · AI** — CameraX + ML Kit pose pipeline, rep-counting state machine, classes |

## Product scope (MVP)

Locked decisions the design assumes (from the design/wireframe phase):

- Exercise = **pushups only**. Challenges are **group** (2–4 members), generically titled "Pushup challenge".
- A user can be in **at most 2 active challenges**; at the limit, creating a new one is blocked.
- Challenge **duration is time-precise** (hours → weeks); it has a start and end timestamp.
- **Auto-complete only**: a challenge runs to its `endAt` and then auto-declares the winner (most reps). No early win. The **creator can cancel** (voids it, no winner, frees a slot).
- Reps are counted **on-device** by ML Kit Pose Detection.
- Invites are **link-based** only (no friend search).
- Out of MVP: push notifications, chat, profile editing, multiple exercise types, anti-cheat.

## Project facts (as scaffolded)

- Package / applicationId: `com.example.buddyworkout`
- minSdk **24**, targetSdk 36, compileSdk 36
- Kotlin 2.0.21, Jetpack Compose (BOM 2024.09.00) + Material 3, AGP 9.0.1
- Single-module (`:app`), Gradle version catalog at `gradle/libs.versions.toml`

> Diagrams are written in [Mermaid](https://mermaid.js.org/) and render on GitHub and most Markdown viewers. In Android Studio, install the *Mermaid* Markdown extension to preview them inline.
