---
document_type: project-analysis
project: AnimScale
platform: Android
analysis_date: 2026-08-27
source_revision: 6c71d54
working_tree_updated: 2026-08-28
status: legacy-prototype
companion_document: ANDROID_COMPATIBILITY_AUDIT.md
---

# AnimScale project analysis

## Executive summary

AnimScale is a small, single-screen Android utility originally last changed in June 2022. It displays the device's three global animation-scale settings and can set each scale to `1` (default), `0` (disabled), or a custom non-negative decimal value.

The project is a prototype rather than a production-ready app. Its core write operation depends on `android.permission.WRITE_SECURE_SETTINGS`, which Android reserves from ordinary third-party applications. The documented ADB grant is therefore part of the product's operating model, not an optional convenience. Without special provisioning, the app can read the values but its write buttons fail with a permission-denial toast.

The legacy project assembles debug and minified release APKs, and its placeholder JVM test passes. Android lint now passes with 5 outdated-dependency warnings after the minimum SDK was aligned with the launcher resources. See [ANDROID_COMPATIBILITY_AUDIT.md](ANDROID_COMPATIBILITY_AUDIT.md) for current-platform findings and a prioritized migration plan.

## Analysis scope and method

Reviewed inputs:

- Root and app Gradle configuration.
- Android manifest, Java source, XML resources, ProGuard configuration, and tests.
- Git history and README.
- A clean dependency resolution followed by debug/release assembly, `testDebugUnitTest`, and `lintDebug` on JDK 17.0.12.
- Current official Android and Google Play documentation as of 2026-08-27.

Not performed:

- No comprehensive automated product tests were added; the existing test sources remain templates.
- The special permission flow was not validated on physical OEM devices.
- No production-signed release artifact was created.
- The linked prebuilt release APK was not treated as equivalent to the current source tree.

## Repository map

```text
anim-scale/
|-- build.gradle                         # AGP plugin versions
|-- settings.gradle                      # repositories and :app module
|-- gradle.properties                    # AndroidX and Gradle JVM options
|-- gradle/wrapper/gradle-wrapper.properties
|-- README.md                            # brief ADB provisioning instructions
`-- app/
    |-- build.gradle                     # SDK levels, dependencies, build types
    |-- proguard-rules.pro               # project-specific shrinker rules
    `-- src/
        |-- main/
        |   |-- AndroidManifest.xml
        |   |-- java/.../MainActivity.java
        |   `-- res/
        |       |-- layout/activity_main.xml
        |       |-- values/              # colors, strings, day theme
        |       |-- values-night/        # night theme
        |       `-- mipmap*/drawable*/   # launcher assets
        |-- test/.../ExampleUnitTest.java
        `-- androidTest/.../ExampleInstrumentedTest.java
```

There is one application module, one activity, no fragments, no services, no receivers, no content providers, no database, no network client, and no native code.

## Build and dependency profile

| Item | Current value |
|---|---|
| Build language | Groovy Gradle DSL |
| App language | Java 8 source/target |
| Android Gradle Plugin | 7.1.3 |
| Gradle wrapper | 7.2 |
| `compileSdk` | 32 (Android 12L) |
| `targetSdk` | 32 |
| `minSdk` | 18 |
| Application ID | `com.example.abdul.animscale` |
| Version | code 1, name 1.0 |
| UI toolkit | Views, AppCompat, Material Components, NestedScrollView |
| Release shrinking | R8 enabled via `minifyEnabled true` |

Direct runtime dependencies:

- `androidx.appcompat:appcompat:1.4.2`
- `com.google.android.material:material:1.6.0`
- `androidx.constraintlayout:constraintlayout:2.1.4`

All were already old at audit time. The lint run reported stable updates for these and both Android test libraries.

## Runtime behavior

### Startup flow

1. `MainActivity.onCreate()` inflates `activity_main.xml`.
2. It binds the animation labels, editable fields, scale actions, and two permission-help buttons.
3. It reads and displays each current setting in both its current-value label and editable field:
   - `Settings.Global.ANIMATOR_DURATION_SCALE`
   - `Settings.Global.WINDOW_ANIMATION_SCALE`
   - `Settings.Global.TRANSITION_ANIMATION_SCALE`

The app no longer launches the unrelated system "Modify settings" screen or shows a permission dialog automatically. Permission status and the ADB command are available on demand.

### Button flow

The activity binds all controls through explicit Java listeners. A shared helper maps each input, Apply button, Default button, and Disable button to its corresponding `Settings.Global` key.

Custom values are parsed as non-negative finite floats. Custom and preset actions apply the shared secure-permission guard, call `Settings.Global.putFloat()`, show `Permission Denied` for absent or denied privilege, refresh all displayed values after success, and provide success/error feedback.

Two additional buttons use explicit Java listeners:

- **Check permissions** reports the only relevant capability, `WRITE_SECURE_SETTINGS`.
- **ADB permission hint** displays a command built from the installed app's actual package name.

### State and data

- The app owns no persistent data.
- The only state it reads or attempts to change is device-global system configuration.
- No personal information, analytics, advertising identifier, network data, files, or credentials are used.
- The declared backup flag has no meaningful app data to back up in the current implementation.

## UI description

The redesigned screen uses a `NestedScrollView` with a vertical, card-based hierarchy. Each animation card contains:

- A human-readable setting title and live current value.
- A labelled decimal input initialized from its corresponding current system value.
- An always-enabled **Apply** button.
- Equal-width, subtly tinted **Default (1×)** and **Disable (0×)** quick actions.

A separate permission card shows whether the app is ready to write and provides full-width permission-check and ADB-help actions. The secondary ADB action uses a subtle lavender tint instead of either a low-contrast white outline or a competing dark fill. All visible text is stored in string resources, input fields opt out of irrelevant autofill, controls have descriptive IDs, and click listeners are wired explicitly in Java.

The scrolling layout removes the original fixed text height and fixed-width constraint chains, making the screen substantially safer for small displays and larger font settings. It was visually verified on an emulator. Android 15/16 edge-to-edge insets and a complete multi-form-factor test matrix remain future compatibility work.

## Test coverage

The test sources are untouched Android Studio template tests:

- The JVM test only asserts `2 + 2 == 4`.
- The instrumented test only checks the application package name.

There are no tests for permission state, reading settings, failed/successful writes, lifecycle return from Settings, UI behavior, accessibility, or layout behavior. The core Android provider interaction is tightly coupled to the activity, which makes meaningful unit testing difficult.

## Observed implementation flaws

| ID | Severity | Finding | Evidence / effect |
|---|---|---|---|
| FUNC-01 | Critical | Normal users cannot authorize the permission needed by the core write feature. | `Settings.Global.putFloat()` targets global settings; the manifest requests `WRITE_SECURE_SETTINGS`, documented as unavailable to third-party apps. |
| TEST-01 | High | Tests provide no product confidence. | Only generated arithmetic and package-name assertions exist. |

Resolved on 2026-08-28:

- Corrected `Permission Denial !` to `Permission Denied`.
- Removed the automatic and unrelated `ACTION_MANAGE_WRITE_SETTINGS` flow.
- Added on-demand secure permission status and a package-specific ADB hint.
- Removed the empty `onActivityResult()` override.
- Enabled all three `SET` buttons, connected their input fields, and changed writes from integer to float values.
- Added permission and validation handling for custom scale values.
- Replaced the fixed ConstraintLayout screen with scrollable Material cards, descriptive IDs, labelled fields, explicit listeners, live permission readiness, and success feedback.
- Initialized all custom inputs from their respective live system values and refresh them after writes and lifecycle resume.
- Removed the unused `WRITE_SETTINGS` manifest declaration, status check, and related messaging.
- Replaced low-contrast outlined secondary actions with a subtle day/night tonal button style.
- Removed the obsolete experimental/commented activity code and duplicate action bar.

See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for failure diagnosis.

## Product constraints and viable operating models

An ordinary Play-distributed app cannot transparently provide the current write feature. A maintainer must explicitly choose one of these product models before investing in UI modernization:

1. **ADB-provisioned developer utility:** keep the special-permission dependency, clearly document USB debugging/ADB setup, detect permission state accurately, and assume a technical user.
2. **Managed/system application:** deploy as a privileged/system component in a controlled device image. This is not a general Play Store model.
3. **Read-only companion:** display current values and deep-link users to supported system Developer Options, without claiming direct write support.
4. **Shizuku/root integration:** use an explicitly elevated third-party execution model. This adds a major dependency and trust/distribution implications and was not evaluated here.

The existing code implicitly chooses option 1 but its UI and README do not fully communicate the security, provisioning, and OEM limitations.

## Suggested architecture if development continues

For a project this small, a large framework is unnecessary. A maintainable shape would be:

```text
MainActivity / screen
        |
        v
AnimationScaleController
        |
        +-- reads current float values
        +-- reports capability/provisioning state
        `-- attempts writes and returns typed success/failure results
```

Use view binding and explicit click listeners. Keep Android provider access behind the controller so it can be faked in JVM tests. Continue refreshing labels and inputs in `onResume()`. Render a clear provision-required state while retaining the current intentional enabled-button/`Permission Denied` interaction.

## Build verification record

Command executed on 2026-08-28:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug assembleRelease --no-daemon
```

Environment and result:

- JDK: Oracle Java 17.0.12.
- Gradle 7.2 and required dependencies resolved in an isolated temporary Gradle cache.
- Android SDK Platform 32 was installed by the Android build tooling.
- `testDebugUnitTest`: passed (placeholder test only).
- `assembleDebug`: passed; a debug APK was produced.
- `assembleRelease`: passed; a minified unsigned release APK was produced.
- `lintDebug`: passed with 0 errors and 5 outdated-dependency warnings after raising `minSdk` to 18.
- The old toolchain emitted SDK XML-version/parser warnings while reading the newer installed SDK metadata.

The former launcher-resource mismatch is resolved: lossless/extended WebP requires API 18 and the app now declares `minSdk 18`. API 17 and older are intentionally unsupported.

## Handoff notes for other AI systems

- Treat `ANDROID_COMPATIBILITY_AUDIT.md` as the time-sensitive compatibility source.
- Do not re-add `WRITE_SETTINGS`; it is unrelated to the app's `Settings.Global` writes.
- Do not “fix” the permission problem by adding a runtime permission dialog; `WRITE_SECURE_SETTINGS` is not a dangerous runtime permission.
- Preserve the ADB/manual-provisioning requirement unless the product model is intentionally changed.
- API 17 support was dropped on 2026-08-28 by raising `minSdk` to 18, matching the existing launcher WebP resources.
- Upgrade build tooling in stages and run lint/tests at each stage.
- Add tests before refactoring provider logic so permission-denied behavior remains explicit.
