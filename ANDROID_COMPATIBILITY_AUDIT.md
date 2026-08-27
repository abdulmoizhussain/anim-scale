---
document_type: android-compatibility-audit
project: AnimScale
audit_date: 2026-08-27
baseline_target_sdk: 32
current_android_baseline: Android 16 / API 36
distribution_scope: phone-and-tablet
source_of_truth: official-android-and-google-play-documentation
---

# Android 16 compatibility and modernization audit

## Verdict

The current project can produce a debug APK, but it is not ready for current Android development or Google Play submission.

- **Buildability:** pass on the legacy toolchain. Debug/release packaging, the placeholder unit test, and lint complete successfully; lint reports dependency-age warnings only.
- **Core feature on a normal device:** fail by design. Ordinary third-party apps cannot write `Settings.Global`; special ADB/system provisioning is required.
- **Google Play update eligibility:** fail. The app targets API 32. Starting August 31, 2026, phone/tablet new apps and updates must target API 36 or higher.
- **Android 15/16 UI readiness after retargeting:** fail. The screen does not handle mandatory edge-to-edge insets or adaptive layouts.
- **Toolchain currency:** fail. AGP 7.1.3/Gradle 7.2/compileSdk 32 are a 2022 stack and already show parser warnings with current SDK metadata.
- **Minimum OS/resources:** pass. `minSdk 18` now matches the launcher WebP resource requirement.

## Current official baseline

At the audit date:

- Android 16 is API level 36.
- Starting **August 31, 2026**, phone/tablet new apps and app updates submitted to Google Play must target Android 16/API 36 or higher.
- Existing phone/tablet apps must target Android 15/API 35 or higher to remain available to new users on devices running newer Android versions. Google documents a possible extension to November 1, 2026.
- The current Android Gradle Plugin documentation shows AGP 9.2.0 with Gradle 9.4.1 as the minimum matching Gradle version. This is a reference baseline, not a recommendation to jump versions without staged migration.

## Compatibility matrix

| Area | Current state | Current requirement / behavior | Status |
|---|---|---|---|
| Play target | `targetSdk 32` | API 36 for phone/tablet submissions from 2026-08-31 | Blocker |
| Compile SDK | `compileSdk 32` | Compile with API 36 to target Android 16 | Blocker for retargeting |
| Build plugin | AGP 7.1.3 | Current documented line is AGP 9.2; modern AGP requires `namespace` in module build script | Major migration |
| Gradle | 7.2 | AGP 9.2 requires Gradle 9.4.1 | Major migration |
| Namespace | Manifest `package=...`; no Gradle `namespace` | AGP 8+ requires module `namespace` | Upgrade blocker |
| Minimum OS | API 18 | Matches the launcher WebP resource requirement | Consistent |
| Edge-to-edge | No inset handling | Enforced when targeting API 35+ on Android 15+ | UI blocker |
| Large screens | Scrollable card layout; narrow-row and large-font testing remains | API 36-targeted apps must adapt to large-screen/window changes | Medium risk |
| Special settings | Writes `Settings.Global` | Third-party apps may read but may not write without special privilege | Product blocker |
| Tests | Template tests only | Core flows need permission/lifecycle/UI coverage | Quality blocker |

## Findings by priority

### P0 — decide whether the product is viable

#### AND-001: Core write access is not available to ordinary third-party apps

`Settings.Global` documentation states that applications may read global settings but are not allowed to write them. `WRITE_SECURE_SETTINGS` is explicitly documented as not for third-party applications.

Impact:

- A normal install cannot perform the app's advertised function.
- Android's ordinary **Modify system settings** approval does not grant the needed global-settings privilege.
- Play-policy modernization cannot remove this platform security boundary.

Action:

- Choose ADB-provisioned, managed/system, read-only, or another explicitly elevated operating model.
- Make provisioning state and failure causes visible in the UI.
- Do not spend significant effort on a store release until this decision is made.

#### AND-002: Target API 32 is ineligible for current Play updates

Impact:

- After the August 31, 2026 deadline, this phone/tablet app cannot be submitted as a new app or update unless it targets API 36 or higher.
- As an existing listing targeting API 32, availability to new users on newer Android versions is also restricted under the current policy.

Action:

- Move `compileSdk` and `targetSdk` to 36 after updating the build stack.
- Test behavior changes incrementally for APIs 33, 34, 35, and 36 rather than changing every variable at once.

### P1 — unblock a trustworthy modern build

#### AND-003: 2022 build tooling conflicts with the current SDK ecosystem

Observed during the audit:

- AGP 7.1.3 with Gradle 7.2 successfully built on the available JDK 17.0.12.
- It emitted `SDK XML versions up to 3 ... version 4 encountered` and unexpected SDK metadata element warnings.

This is evidence of tooling age even though compilation completed.

Action:

1. Commit or tag the current buildable baseline.
2. Add `namespace 'com.example.abdul.animscale'` to `app/build.gradle` and remove the manifest `package` after the toolchain supports the DSL.
3. Upgrade AGP/Gradle/JDK in supported pairs, preferably with Android Studio's Upgrade Assistant.
4. Upgrade compile/target SDK and AndroidX libraries separately from behavioral refactors.
5. Run unit tests, lint, assemble, and an instrumented smoke test at each step.

#### AND-004: API 17 resource mismatch was resolved on 2026-08-28

The project now declares `minSdk 18`, matching the minimum API required by its lossless/extended WebP launcher resources. The previous 10 `WebpUnsupported` lint errors are resolved without suppressions or a lint baseline.

#### AND-005: Manifest contains legacy/non-production declarations

- `package` is used as the code namespace; modern AGP requires a Gradle `namespace`.
- Unqualified `coreApp="true"` is not needed for this third-party utility and should be removed unless a controlled system-image deployment has a documented reason for it.
- `tools:ignore="ProtectedPermissions"` hides the exact permission constraint that defines the product.
- `android:allowBackup="true"` is not currently dangerous because the app stores no data, but backup/data-extraction policy should be explicit if state is later added.

### P1 — fix behavior before retargeting

#### AND-006: Permission UX was corrected on 2026-08-28

The automatic `Settings.System.canWrite()` / `ACTION_MANAGE_WRITE_SETTINGS` flow was removed because it cannot authorize writes to `Settings.Global`. The unused `WRITE_SETTINGS` declaration and check have also been removed. The screen now checks only `WRITE_SECURE_SETTINGS` on demand and displays a package-specific ADB command.

Current behavior:

- Write controls intentionally remain enabled; an unprovisioned click shows `Permission Denied`.
- Permission readiness and the ADB command remain available on demand.
- Labels and editable values refresh in `onResume()`.

#### AND-007: Float-based custom scales were implemented on 2026-08-28

Android documents animation scales as floats; for example, `0.0f` disables window animations. All three editable fields and `SET` buttons are connected, non-negative finite values are validated, and custom and preset writes use `Settings.Global.putFloat()`. Each input now starts with its corresponding live system value instead of a hardcoded `1` and refreshes with the displayed value.

The remaining product-level restriction is unchanged: writes still require the specially provisioned `WRITE_SECURE_SETTINGS` permission.

### P1 — support Android 15/16 window behavior

#### AND-008: Edge-to-edge will be enforced after the required retarget

When an app targets API 35 or later and runs on Android 15 or later, the window is edge-to-edge. The current root layout applies no system-bar or cutout insets.

Likely effect:

- The root's fixed top padding does not account for the actual status bar or display cutout and may overlap after retargeting.
- Bottom content can conflict with the navigation/gesture area depending on window size and font scale.

Action:

- Enable edge-to-edge consistently and use `WindowInsetsCompat` to apply system-bar/display-cutout padding.
- Give the root a stable ID and test gesture navigation, three-button navigation, display cutouts, light/dark themes, and IME visibility.

#### AND-009: Layout adaptability was substantially improved on 2026-08-28

The fixed ConstraintLayout, fixed text height, generic IDs, and non-scrolling control chains were replaced by a `NestedScrollView` containing Material cards. Fields are labelled and initialized from live values, values and actions are grouped consistently, text comes from resources, and current permission readiness is visible on the screen. Secondary quick actions now use subtle day/night tonal colors for clear but restrained contrast.

Remaining action:

- Allow the custom-input/Apply row to stack vertically at extremely narrow widths.
- Verify 200% font scale and long translated strings.
- Test landscape, split screen, tablet, foldable, and desktop window sizes.
- Complete Android 15/16 edge-to-edge inset handling.

### P2 — quality, accessibility, and maintainability

#### AND-010: Legacy launcher-resource lint failure was resolved

Current lint result after the API 18 correction: **0 errors, 5 warnings**.

Warning category:

- 5 outdated dependency notices.

Action:

- Update dependencies as part of the staged build-system migration.
- Add automated accessibility checks even though the redesigned screen has cleared the previous UI lint findings.

#### AND-011: Release signing and physical-device behavior remain unverified

Release builds enable minification. A debug-signed copy of the minified artifact was installed and smoke-tested, while the distributable release output remains unsigned. XML reflection risk was removed by replacing all `android:onClick` attributes with explicit Java listeners.

Action:

- Add `assembleRelease`/bundle verification using a real signing strategy in CI.
- Install and exercise the minified artifact before publishing.

#### AND-012: Tests do not cover functionality

Action:

- Unit-test formatting, float parsing/validation, and typed permission/write results behind a controller abstraction.
- Instrument startup with and without provisioning.
- Test button-to-key mappings and refreshed values.
- Add accessibility checks and screenshot/layout tests for key widths, themes, and font scales.
- Run at least API 23/24 if retained, plus API 32 and API 35/36; use the true selected `minSdk` rather than an aspirational one.

## Recommended modernization sequence

### Phase 0: product decision

1. Select the privilege/distribution model.
2. Define supported Android versions and whether Play distribution is a goal.
3. If ordinary Play distribution is mandatory, redesign the core feature as read-only or supported-settings navigation.

### Phase 1: preserve and characterize behavior

1. Add tests around setting keys, displayed values, and permission-denied behavior.
2. Remove dead/commented experimental code.
3. Keep `minSdk 18` or convert the launcher assets if API 17 support is ever restored. *(Resolved for the current scope.)*
4. Keep lint passing on the legacy baseline. *(Resolved; dependency warnings remain.)*

### Phase 2: build-system migration

1. Add module namespace and migrate away from manifest namespace inference.
2. Upgrade AGP, Gradle, JDK, AndroidX, Material, and test libraries in supported increments.
3. Set `compileSdk 36`, then `targetSdk 36` after compatibility testing.
4. Produce and install both debug and minified release artifacts.

### Phase 3: app behavior and UI

1. Keep the unrelated `WRITE_SETTINGS` permission out of the manifest and status UI. *(Resolved.)*
2. Keep float custom values and live field initialization covered during future refactors. *(Implemented; tests remain.)*
3. Adopt view binding; explicit listeners are already in place.
4. Handle edge-to-edge insets; the layout is already scrollable.
5. Complete accessibility, localization, and multi-form-factor verification; lifecycle refresh is implemented.

### Phase 4: verification and release

1. Test Android 16 behavior changes that affect all apps and those gated by target API 36.
2. Test multiple form factors, navigation modes, themes, font scales, and relevant OEM devices.
3. Run `test`, `lint`, `assemble`, instrumented tests, and release smoke tests in CI.
4. Confirm Play policy, signing, app bundle, developer verification, privacy, and listing requirements immediately before release because policy can change.

## Suggested acceptance criteria

- `compileSdk` and `targetSdk` are 36 or newer when Play submission is attempted.
- The chosen AGP/Gradle/JDK versions are an officially supported combination.
- Gradle namespace is explicit and manifest namespace inference is removed.
- `lint` completes with zero errors; any baseline entries are individually justified.
- Debug and minified release variants assemble and install.
- The app never claims a write succeeded when the value did not change.
- Permission/provisioning UI accurately describes the non-standard requirement.
- The screen is usable at 200% font scale, in narrow split screen, and with system-bar/cutout insets.
- Automated tests cover unprovisioned and provisioned controller outcomes.
- Physical/emulator testing includes Android 15 and Android 16.

## Official references

- [Android 16 overview](https://developer.android.com/about/versions/16)
- [Set up the Android 16 SDK](https://developer.android.com/about/versions/16/setup-sdk)
- [Android 16 changes for apps targeting API 36](https://developer.android.com/about/versions/16/behavior-changes-16)
- [Android 16 changes affecting all apps](https://developer.android.com/about/versions/16/behavior-changes-all)
- [Google Play target API requirements](https://developer.android.com/google/play/requirements/target-sdk)
- [Google Play target API policy and 2026 deadlines](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en-GB_ALL)
- [Android Gradle Plugin versions and Gradle compatibility](https://developer.android.com/build/releases/about-agp)
- [AGP 8 namespace requirement](https://developer.android.com/build/releases/agp-8-0-0-release-notes)
- [`WRITE_SECURE_SETTINGS` permission reference](https://developer.android.com/reference/android/Manifest.permission#WRITE_SECURE_SETTINGS)
- [`Settings.Global` read/write model](https://developer.android.com/reference/android/provider/Settings.Global)
- [Edge-to-edge guidance for Views](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
- [Window inset guidance](https://developer.android.com/develop/ui/views/layout/insets)

## Time-sensitivity note

Facts about the inspected source tree are stable until the code changes. Android releases, Play deadlines, current library versions, and tooling recommendations are time-sensitive. A future maintainer or AI system should re-check the official references rather than copying version numbers or deadlines blindly.
