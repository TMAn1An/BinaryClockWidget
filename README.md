# Binary Clock Widget

An Android home-screen widget that shows the time as a BCD binary clock.

```
 H  H   M  M
    ○   ○  ●     8
    ○   ●  ○     4
 ●  ○   ●  ●     2
 ○  ●   ●  ●     1
 2  1   3  7   → 21:37
```

Each column is one digit of `HH:MM`. Add up the values of the lit dots (8, 4, 2, 1) to read it.

## Build & run

1. Open this folder in Android Studio (Ladybug or newer) and let it sync.
2. Run the `app` configuration on a device or emulator (Android 8.0+ / API 26).
3. Long-press the home screen → **Widgets** → drag **Binary Clock** onto it.

From the command line: `./gradlew installDebug`. For unit tests: `./gradlew test`.

## How it works

- `BinaryTime.kt`: pure logic that turns hours/minutes into the lit-dot grid (unit tested).
- `BinaryClockWidgetProvider.kt`: renders the grid into `RemoteViews` and schedules a
  redraw at the start of each minute with `AlarmManager`. It also refreshes on time and
  timezone changes, and follows the phone's 12/24-hour setting.
- `MainActivity.kt`: a tiny launcher screen. On Android 12+ it offers a button to grant the
  *exact alarm* permission. Without that permission Android may deliver the minute tick late.
- Light and dark colours live in `res/values/colors.xml` and `res/values-night/colors.xml`.
