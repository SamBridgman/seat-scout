# Seat-Scout verification

## November 19, 2026 phone and watch frontend

Both debug APKs built from this Android project. Android Build Tools verified both signatures. The phone APK includes the Latin OCR model and native inference libraries for ARM and x86. The bundled catalog covers 36 rooms or study areas at 15 mapped addresses on three campuses.

All 11 tests passed. The Gradle checks used one worker and the Android host test renderer. The current walkthrough did not require an emulator on the laptop.

| Check | Evidence |
| --- | --- |
| Catalog and map | JUnit checks stable IDs, source and access metadata, all three campus names, and a coordinate inside the campus region for every address. |
| Phone navigation | A scroll and search test browses Discover and finds Stone Science Library by name. The reporting test opens Map, filters to Medical, opens L12 Quiet Study, saves it, reports few seats and quiet, waits for the saved conditions to appear, and checks the Saved list. |
| Photo to pin | The Compose test opens the system picker and delivers a test result URI. Only native OCR output is supplied as a fixture. The app ranks the recognized words, displays a suggestion, changes campus and tab, focuses the matching pin, and opens PAL Study Lounge. Separate matcher tests cover addresses, shared addresses, room names and uninformative text. |
| Watch | The native-view test opens Medical, Alumni Medical Library and L12 Quiet Study. It saves the room, retrieves it from Saved, and checks the address in the directions intent. |
| Freshness | Report logic rejects future and expired timestamps. The current condition window is 30 minutes. |
| Static analysis | Android lint completed with zero errors. The phone reported 21 warnings and the watch four. These include dependency updates, EXIF API guidance, pre-Android 12 backup configuration and Wear recents behavior. |
| Privacy | The merged phone manifest requests neither broad image access nor location permission. Both apps disable cleartext traffic and opt out of backup. Modern backup and transfer rules exclude report databases and preferences. The source archive omits credentials and local environment files. |

To reproduce the UI captures, run this command in the Android project with Android SDK 36 installed. A first run downloads the Android test runtime.

```powershell
.\gradlew.bat --max-workers=1 -PdemoCapture :app:testDebugUnitTest :wear:testDebugUnitTest
```

The MP4 uses captures of native screens rendered in host tests. Its glass-dock segment uses a labeled earlier hardware-rendered capture. The host uses the solid dock fallback. A physical phone should still be used to check recognition of real sign photos, Google tiles with the team's restricted key, and glass rendering on its GPU. Watch fit, rotary feel and map-app handoff need a physical Wear device. These builds use local reports and bookmarks.

## Historical phone build — November 16, 2026

### Build checks

- `:app:assembleDebug`, `:app:testDebugUnitTest`, and `:app:lintDebug` succeeded on the November 16 source.
- Two unit tests passed: exact 30-minute freshness/future timestamp behavior, and unique catalog IDs with legacy IDs retained, official HTTPS BU sources, addresses, access descriptions and all three campuses.
- Lint reports zero errors and nine warnings. Warnings include dependency/target update suggestions, backup-rule configuration, an unused historical image, and Kotlin extension suggestions. These are recorded rather than described as a warning-free audit.
- `git diff --check` passed. No commits were created.

### Emulator interactions

Android API 36.1 emulator; existing data was preserved during APK replacement.

- Searched Stone with keyboard open, opened its details, saved the space.
- Completed seating → atmosphere → review → save; observed local success feedback.
- Relaunched and verified saved space and the reported conditions persisted.
- Medical and Fenway filters showed their respective libraries/residences.
- No-result search and Reset filters worked.
- Inspected details, reporting, directory, saved state, light and dark screenshots.
- Narrow screen with 200% font size exposed the Discover dock label wrapping. Changed it to Find at enlarged font sizes, stacked the results count, and shortened the search label. Final captures retained with the handoff.

The emulator initially showed an Android System UI not-responding dialog during boot. Dismissing it allowed the app and subsequent checks to run. The first automation relaunch check observed the previous details view before settling; the check now waits for launch and returns through Back when applicable. The discovery screenshot was recaptured after startup completed.

### Review

Astra supplied the plan and a final read-only code/screenshot review. Its local-attribution finding was fixed. Final verdict: no blockers in the bounded review. EGM performed one fresh-context README review; its two proposed document clarifications are in ../goldfish_review_report.md pending user resolution. No EGM convergence is claimed.

### Limits

No physical-device frame-rate, battery or TalkBack session was measured. Tablet layout is implemented but not device-tested in this pass. Save-failure injection, full RTL/localization and a development worst-case fixture toggle were not run. Network and backend behavior are not implemented. Catalog verification means published-source review, not on-site access verification. No claim of a complete multi-student pilot is made.
