# AnimScale 1.1 release notes

AnimScale 1.1 modernizes the app's single-screen experience and fixes several permission and control issues while preserving its focused ADB-provisioned workflow.

## Highlights

- Added support for custom non-negative decimal animation scales, including `0`, `0.5`, `1`, and `2`.
- Enabled all **Apply** buttons. Without the required permission, they now remain available and show **Permission Denied** when tapped.
- Custom inputs now start with their corresponding current system values instead of always showing `1`.
- Added on-demand **Check permissions** and **ADB permission hint** actions.
- Removed the unrelated `WRITE_SETTINGS` permission and its misleading system-settings flow. The app now declares and checks only `WRITE_SECURE_SETTINGS`.
- Corrected the permission error message from “Permission Denial” to “Permission Denied.”

## UI and usability

- Rebuilt the screen as a compact, scrollable card layout for shorter displays.
- Reduced outer padding and card spacing.
- Added clear current-value labels for animator duration, window animation, and transition animation.
- Added **Default (1×)** and **Disable (0×)** quick actions for every animation type.
- Applied a subtle lavender tonal style to secondary buttons so they remain visible without competing with the primary actions.
- Added day/night colors for the secondary-button treatment.
- Values refresh after a successful change and whenever the app resumes.

## Compatibility

- Minimum supported Android version is now Android 4.3 / API 18, matching the existing launcher image format.
- Debug and minified release variants build successfully on JDK 17.
- Android lint completes with no errors; remaining warnings concern outdated dependencies in the legacy build stack.

## Required setup

Changing global animation scales requires the protected `WRITE_SECURE_SETTINGS` permission. After installing the APK, connect the device through ADB and run:

```text
adb shell pm grant com.example.abdul.animscale android.permission.WRITE_SECURE_SETTINGS
```

Open AnimScale and use **Check permissions** to confirm that the permission is granted. The **ADB permission hint** button displays the correct command for the installed package.

The permission can be lost when the app is uninstalled or reinstalled. If the controls stop working after reinstalling, run the ADB command again.

## Known limitations

- Android does not grant `WRITE_SECURE_SETTINGS` through a normal runtime permission dialog; ADB, system-app provisioning, or another elevated deployment model is required.
- Behavior may vary on managed devices, restricted profiles, or Android builds whose manufacturer blocks global-setting changes.
- The project still uses the legacy AGP 7.1.3, Gradle 7.2, and target API 32 toolchain. A staged toolchain and target-SDK upgrade remains recommended.
- Automated tests are still Android Studio placeholders; physical-device testing across manufacturers remains recommended.
