# SeatScout for Android

Open this folder in Android Studio, use the bundled JDK for Gradle, and install Android SDK 36 if Studio prompts for it. Run **app** on an emulator or phone running Android 6 or newer.

## Update 1 demo

1. Open a sample study area with Unknown conditions.
2. Tap **Update conditions**. Select a seating value and an atmosphere value, then tap **Save report**.
3. Check the values and report age on the details screen.
4. Force-stop and reopen SeatScout. Open the same location and confirm the report is still there. You can do this in airplane mode.

The catalog is illustrative. Reports stay on the device, and each new report replaces the previous one for that location. After 30 minutes, the screen shows Unknown. The app requests no runtime permissions.

## Code walkthrough

- `MainActivity.kt` defines the three Compose screens and Navigation routes.
- `ScoutViewModel.kt` combines Room's report stream with the current time and tracks save progress and errors.
- `Reports.kt` defines the sample catalog, Room table and DAO, and repository. The repository timestamps the report and the DAO writes it to disk.
- `ReportTest.kt` checks the freshness boundary and future timestamps.

Run `./gradlew.bat :app:assembleDebug :app:testDebugUnitTest` in PowerShell to build and check the unit test.

Cross-device reporting, conflict aggregation, verified campus entries, and filtering are outside this slice.
