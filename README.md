# anim-scale (animation-scale)
#### To change animation-scale in Android.

## [Download Now (APK file)][1]

<br/>


Works by manually providing `android.permission.WRITE_SECURE_SETTINGS`
with the following command after installing the APK:

> adb shell pm grant com.example.abdul.animscale android.permission.WRITE_SECURE_SETTINGS

The app's **Check permissions** button reports the current permission state.
Use **ADB permission hint** to display the command for the installed build's
actual package name. The app declares only the required
`WRITE_SECURE_SETTINGS` permission; it does not request the unrelated normal
**Modify system settings** permission.

Each animation row supports preset **Default** and **0** actions, or a custom
non-negative decimal value through its **SET** button. All write actions show
**Permission Denied** when `WRITE_SECURE_SETTINGS` has not been granted.

## Supported custom scale values

Enter a non-negative decimal in any **Custom scale** field and tap **Apply**.
For example:

| Value | Effect |
|---:|---|
| `0` | Disables that animation type |
| `0.5` | Runs animations at half the normal duration |
| `1` | Android's default animation duration |
| `2` | Runs animations at twice the normal duration |

Other non-negative decimal values are also accepted. Larger values make the
animation take longer; smaller values make it finish faster.

Each custom input is initialized from that animation type's current system
value and is refreshed after a successful change or when the app resumes.

The APK supports Android API level 18 and newer.

The redesigned screen is scrollable and groups each animation type into a
separate card with its current value, custom input, and quick actions. Secondary
actions use a subtle lavender tonal treatment so **Default**, **Disable**, and
**ADB permission hint** remain visually identifiable as buttons.

See [TROUBLESHOOTING.md](TROUBLESHOOTING.md) if the app opens but cannot change
the values, or behaves differently on a particular Android device.

<br/>
Source:
<a href="https://stackoverflow.com/a/27816942" target="_blank"> https://stackoverflow.com/a/27816942
</a>

 [1]: https://github.com/abdulmoizhussain/anim-scale/releases/download/v1.1/AnimScale-v1.1.apk
