# GymSesh

A local-first Android workout tracker built with Kotlin and Jetpack Compose. The app connects training splits to prefilled workout logging and persistent workout history.

## Features

- Exercise catalogue with search and filtering.
- Six seeded training splits with an active-day rotation.
- Prefilled workout logging, sets/reps/weight entry and a rest timer.
- Room persistence, session history and a workout calendar.
- Streaks, estimated one-rep-max records and muscle-group summaries.
- Home-screen widget and theme settings.

## Build

Requires Android SDK 36 and a compatible JDK (Android Studio’s bundled JDK 21 was used during development). Minimum device API is 30.

```sh
# Configure ANDROID_HOME or a local.properties file with sdk.dir first.
./gradlew :app:assembleDebug --no-daemon --max-workers=1 -Dorg.gradle.vfs.watch=false
```

Install `app/build/outputs/apk/debug/app-debug.apk` on an emulator or test device. No release signing key or prebuilt APK is included.

## Architecture

`app/src/main/` contains the Compose screens, navigation, Room data layer, domain calculations and Glance widget. Seed JSON is under `assets/seed/`. Hilt provides dependency injection; DataStore stores preferences.

Workout sessions persist locally. Aggregate saves use a transaction, and streak calculations share a common domain implementation.

## Status and limitations

Personal MVP. Health Connect remains deferred. Estimated strength statistics are training aids, not measured physiological results. No cloud account or backup service is included. Device-level UI behaviour needs checking on the target Android version.

## Assets and privacy

The original local build used wger exercise imagery. Images are omitted from this public snapshot because complete per-image author/licence attribution was not available. The catalogue uses existing placeholder support instead. No workout database, device configuration or signing material is published. Third-party dependencies retain their respective licences.
