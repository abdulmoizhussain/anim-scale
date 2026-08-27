---
document_type: troubleshooting-guide
project: AnimScale
updated: 2026-08-28
package: com.example.abdul.animscale
---

# AnimScale troubleshooting

## Most likely reason the app does not work

AnimScale can read global animation settings on a normal Android installation, but changing them requires `android.permission.WRITE_SECURE_SETTINGS`. Android does not let an ordinary third-party app request this permission from a runtime dialog or the normal **Modify system settings** screen.

The app must be provisioned through ADB after installation:

```text
adb shell pm grant com.example.abdul.animscale android.permission.WRITE_SECURE_SETTINGS
```

Open AnimScale and tap **Check permissions**. `WRITE_SECURE_SETTINGS` must show **Granted**. This is the only settings-write permission declared and checked by the current app.

## Reporter diagnostic checklist

Ask the reporter for:

- Device manufacturer and model.
- Android version.
- AnimScale version and source of the APK.
- Whether the app opens, crashes, or only fails when a scale button is pressed.
- A screenshot of **Check permissions**.
- The exact ADB command and output, with serial numbers or sensitive identifiers removed.
- Whether the app was uninstalled or reinstalled after granting permission.

## Correct provisioning sequence

1. Install the intended APK.
2. Enable Developer Options and USB debugging.
3. Connect the device and accept its USB-debugging authorization prompt.
4. Confirm that ADB sees the device:

   ```text
   adb devices
   ```

5. Grant the permission to the exact installed package:

   ```text
   adb shell pm grant com.example.abdul.animscale android.permission.WRITE_SECURE_SETTINGS
   ```

6. Open the app and tap **Check permissions**.
7. Try one scale button and verify the displayed value changes.

For a secondary Android user or work profile, the grant might need the correct user ID, for example:

```text
adb shell pm grant --user 0 com.example.abdul.animscale android.permission.WRITE_SECURE_SETTINGS
```

Do not assume user `0` if the app is installed in another Android user or profile.

## Common failure causes

### 1. Permission was never granted

Symptom: the app opens and reads values, but tapping **Default** or **0** shows **Permission Denied**.

Resolution: run the ADB grant and confirm with **Check permissions**.

### 2. The wrong system setting was enabled

Android's **Modify system settings** screen does not authorize `Settings.Global` animation-scale writes. The current app no longer declares or checks `WRITE_SETTINGS`; the required status is `WRITE_SECURE_SETTINGS: Granted`.

### 3. Wrong package name or APK variant

The ADB command must use the installed APK's application ID. The current source uses:

```text
com.example.abdul.animscale
```

If a fork or differently configured build has another application ID, use the command shown by that build's **ADB permission hint** button.

### 4. Permission was cleared by reinstalling

Uninstalling the app removes its package permission state. Some reinstall/update workflows or device-management policies may also clear the grant. Run the ADB grant again after installing the final APK.

### 5. App is installed for a different Android user

The owner user, secondary users, guest users, and work profiles have separate package and permission state. Grant the permission for the user where AnimScale is installed.

Useful diagnostic commands:

```text
adb shell pm list users
adb shell pm list packages
adb shell dumpsys package com.example.abdul.animscale
```

### 6. OEM or device-management restriction

Some managed devices, enterprise policies, restricted profiles, and vendor Android builds can reject the grant or block global-setting changes. Capture the ADB error and Android log output. A permission grant displayed as successful does not guarantee that an OEM applies every animation setting identically.

### 7. Old APK crashes while opening the settings screen

The original code automatically launched `ACTION_MANAGE_WRITE_SETTINGS`, even though that permission does not enable the core feature. An OEM without a compatible settings activity could fail there. The 2026-08-28 working tree removes this automatic launch and provides on-demand status and hint dialogs instead.

### 8. Minimum supported Android version

The project now declares `minSdk 18`, matching its lossless/extended WebP launcher resources. Android API 17 and older are intentionally unsupported.

### 9. Layout unusable on a particular screen

Older APKs use a fixed, non-scrolling layout that can clip controls. The 2026-08-28 redesign is scrollable and removes fixed text heights, but very large fonts, extremely narrow windows, and future edge-to-edge retargeting still require broader device testing.

## Confirm the device setting outside the app

Read the three global values through ADB:

```text
adb shell settings get global animator_duration_scale
adb shell settings get global window_animation_scale
adb shell settings get global transition_animation_scale
```

After pressing a button in AnimScale, run the commands again. If the UI says permission is granted but values do not change, collect logcat while reproducing:

```text
adb logcat
```

Look for `SecurityException`, `Permission Denial`, `SettingsProvider`, or the package name. Do not publish an unredacted full logcat because it can contain unrelated private device information.

## Build verification for the 2026-08-28 UI and permission changes

With Gradle running on JDK 17:

- `assembleDebug`: passed.
- `testDebugUnitTest`: passed; the existing test is only a placeholder.
- `lintDebug`: passes with 5 outdated-dependency warnings and no errors. The API 17/WebP mismatch and prior UI lint findings are resolved.

All three `SET` buttons are enabled. They accept non-negative decimal values such as `0`, `0.5`, `1`, or `2`. Without `WRITE_SECURE_SETTINGS`, custom and preset buttons show **Permission Denied**.

The three custom fields now load their respective current system values instead of a hardcoded `1`. They refresh after a successful write and whenever the activity resumes. The unused `WRITE_SETTINGS` manifest declaration and status check were removed. **Default**, **Disable**, and **ADB permission hint** now use a subtle lavender day/night tonal style instead of a white/transparent outline or a competing dark fill.

The redesigned screen was installed and visually verified on an emulator. Physical-device testing is still required because an emulator cannot prove that elevated permission provisioning works on a reporter's OEM device.
