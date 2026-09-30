# Binary Clock Widget

An Android home-screen widget that shows the time as a BCD binary clock.

```
 H  H   M  M
 ·  ○   ·  ○    8
 ·  ○   ○  ●    4
 ●  ○   ●  ●    2
 ○  ●   ●  ●    1
 2  1   3  7  → 21:37
```

● = on, ○ = off, · = never used (the hour tens digit only goes up to 2, and the minute
tens digit only goes up to 5).

Each column is one digit of `HH:MM`. Add up the values of the lit dots (8, 4, 2, 1) to read it.

## Build & run

1. Open this folder in Android Studio (Ladybug or newer) and let it sync.
2. Run the `app` configuration on a device or emulator (Android 8.0+ / API 26).
3. Long-press the home screen → **Widgets** → drag **Binary Clock** onto it.

From the command line: `./gradlew installDebug`. For unit tests: `./gradlew test`.

## How it works

- `BinaryTime.kt`: pure logic that turns hours/minutes into the lit-dot grid (unit tested).
- `BinaryClockWidgetProvider.kt`: the home screen widget. It renders the grid into `RemoteViews`
  and schedules a redraw at the start of each minute with `AlarmManager`. It also refreshes on
  time and timezone changes and follows the phone's 12/24-hour setting.
- `BinaryClockWallpaperService.kt`: a live wallpaper that draws the same clock. Unlike widgets,
  it shows on the **lock screen** of any Android phone. It redraws only while visible.
- `BinaryClockRenderer.kt` / `BinaryClockView.kt`: shared Canvas drawing, used by the wallpaper
  and the live preview in the app.
- `MainActivity.kt`: a Material 3 screen with a live preview, a short guide, and one-tap
  buttons to add the widget, set the wallpaper and allow exact alarms (Android 12+).

### Theme colours

On Android 12+ the dots use your phone's Material You colours (`res/values-v31`,
`res/values-night-v31`), so they change when your wallpaper or theme changes. Older versions
use the fixed teal palette in `res/values/colors.xml`.

The app's **Clock colour** picker (`ClockColors.kt`) lets you override this on any Android
version with one of nine preset colours. **Auto** goes back to following the theme.

## Download a build

The easiest way: open the repo's **Releases** page and download the latest `BinaryClock-vX.Y.Z.apk`.
To publish a new one, push a tag like `v1.0.1`, or go to **Actions → Build → Run workflow** and
enter the tag in *release_tag*.

Every push is built by GitHub Actions (`.github/workflows/build.yml`), which runs the unit
tests and builds a debug APK. Open the **Actions** tab, pick the latest run, and download
**binary-clock-debug-apk**. Unzip it, copy the `.apk` to your phone and open it. Android will
ask you to allow installing apps from that source.
