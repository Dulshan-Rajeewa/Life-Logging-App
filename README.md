# Life Logging App

Android application for logging daily activities and reflections.

> Minimal, personal life-logging app built with Jetpack Compose and Kotlin.

---

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup & Build](#setup--build)
- [Project Structure](#project-structure)
- [Important Files & Screens](#important-files--screens)
- [Contributors](#contributors)

---

## Project Overview

The Life Logging App is an Android application designed to let users record short logs for daily activities (study, workout, events, reflections, travel, work, etc.), view a timeline of logs, and track daily progress. The UI uses Jetpack Compose and follows a single-activity / composable screen structure.

This repository contains the Android project (Gradle/Kotlin) and related resources.

## Features
- Add, view, and manage daily logs
- Summary card showing daily progress (animated)
- Timeline / list of today's logs with category badges
- Offline-first behavior with sync indicator
- Simple profile dialog and authentication hooks

## Tech Stack
- Kotlin
- Android (Jetpack Compose)
- Gradle (Kotlin DSL)
- Optional: local data store / Room, remote sync (depending on implementation)

## Prerequisites
- Windows, macOS, or Linux
- Android Studio (recommended) or command-line Android SDK & build tools
- JDK 11 or later
- Gradle (wrapper included — you can use `gradlew`)

## Setup & Build
Open a terminal in the project root (the folder that contains this `README.md`) and use the included Gradle wrapper to build and run the app.

On macOS / Linux / WSL / Git Bash:

```bash
./gradlew clean
./gradlew :app:assembleDebug
./gradlew :app:installDebug  # installs to a connected Android device
```

On Windows (PowerShell or Command Prompt):

```powershell
.\gradlew.bat clean
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

Alternatively, open the project in Android Studio and use the Run/Debug actions (recommended for first-time setup).

## Project Structure

- `app/` — Android application module
  - `src/main/java/...` — Kotlin source files (UI, ViewModels, repositories)
  - `src/main/res/` — resources (layouts, drawables, values)
  - `build.gradle.kts` — module build script
- `build.gradle.kts`, `settings.gradle.kts`, `gradle/` — project-level Gradle configuration

## Important Files & Screens
These are useful to help you locate the key screens and components in the codebase.

- `app/src/main/java/.../ui/screens/HomeScreen.kt` — Home screen composable (greeting, summary card, timeline, FAB)
- `app/src/main/java/.../ui/screens/AddEntryScreen.kt` — Add new log entry
- `app/src/main/java/.../ui/screens/LogDetailScreen.kt` — Detail view for a single log
- `app/src/main/java/.../ui/components/HomeComponents.kt` — Reusable UI components used by the home screen (colors, badges, summary card)
- `app/src/main/java/.../viewmodel/` — ViewModels that provide data to the screens

(If your package path differs, search for files by name in the `app` module.)

## Contributors
- UD Rajeewa — development, UI, features
- MPBR Perera — development, data & sync

---

If you'd like, I can also:
- Add a `CONTRIBUTING.md` and `LICENSE` file
- Create a short cheat-sheet for the viva with the most important file paths and quick edit examples
- Update the README with screenshots or build badges (if you provide them)


