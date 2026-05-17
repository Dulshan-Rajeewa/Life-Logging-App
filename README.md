# Life Logging App

Android application for logging daily activities and reflections.

> Minimal, personal life-logging app built with Jetpack Compose and Kotlin.

---

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Setup & Build (Windows / PowerShell)](#setup--build-windows--powershell)
- [Project Structure](#project-structure)
- [Important Files & Screens](#important-files--screens)
- [Start a fresh Git repository](#start-a-fresh-git-repository)
- [Contributing](#contributing)
- [License](#license)

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

## Setup & Build (Windows / PowerShell)
Open PowerShell and run these commands from the project root (this repository already includes the Gradle wrapper):

```powershell
cd "D:\SEM 05\MC\Life Logging App"
# Clean build
./gradlew clean
# Assemble debug APK
./gradlew :app:assembleDebug
# Install on a connected device (use adb devices to confirm)
./gradlew :app:installDebug
```

Alternatively, open the project in Android Studio and use the Run/Debug actions.

Notes:
- If Gradle or the wrapper uses Unix line endings, on Windows use `.
\gradlew.bat` instead of `./gradlew` in PowerShell:

```powershell
cd "D:\SEM 05\MC\Life Logging App"
.\gradlew.bat clean
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

## Project Structure (high level)

- `app/` — Android application module
  - `src/main/java/...` — Kotlin source files (UI, ViewModels, repositories)
  - `src/main/res/` — resources (layouts, drawables, values)
  - `build.gradle.kts` — module build script
- `build.gradle.kts`, `settings.gradle.kts`, `gradle/` — project-level Gradle configuration

## Important Files & Screens
These are useful to know if you are preparing to navigate the code or present the app in a viva/defense.

- `app/src/main/java/.../ui/screens/HomeScreen.kt` — Home screen composable (greeting, summary card, timeline, FAB)
- `app/src/main/java/.../ui/screens/AddEntryScreen.kt` — Add new log entry
- `app/src/main/java/.../ui/screens/LogDetailScreen.kt` — Detail view for a single log
- `app/src/main/java/.../ui/components/HomeComponents.kt` — Reusable UI components used by the home screen (colors, badges, summary card)
- `app/src/main/java/.../viewmodel/` — ViewModels that provide data to the screens

(If your package path differs, search for files by name in the `app` module.)

## Start a fresh Git repository
If you want to discard the existing Git history and start again (keep files but remove history):

```powershell
cd "D:\SEM 05\MC\Life Logging App"
# Remove existing git history (BE CAREFUL)
Remove-Item -Recurse -Force .git
# Initialize a new repository
git init
git add .
git commit -m "Initial commit"
# (Optional) connect to a new remote and push
git remote add origin https://github.com/your-username/your-new-repo.git
git branch -M main
git push -u origin main
```

If you instead want to remove all project files and start truly from empty, delete files in the folder before reinitializing git. Always ensure you have backups if needed.

## Contributing
If you'd like to contribute or extend the app, please open issues or submit pull requests. Follow existing code style and prefer small, focused commits with descriptive messages.

## Tips for Viva / Demo
- Know the key screens and where they are located (`HomeScreen.kt`, `AddEntryScreen.kt`, `LogDetailScreen.kt`).
- Be ready to point to the FAB (add), summary card (progress), top profile button, and the timeline items.
- Explain data flow: ViewModel -> Composables -> Navigation.
- If asked how to change a color or text, point to the component file (e.g. `HomeComponents.kt`) and show the color variable or string.

## License
This project is provided as-is. Add a license file if you intend to publish it (MIT recommended).

---

If you'd like, I can also:
- Add a `CONTRIBUTING.md` and `LICENSE` file
- Create a short cheat-sheet for the viva with the most important file paths and quick edit examples
- Update the README with screenshots or build badges (if you provide them)


