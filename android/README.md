# Seat-Scout for Android and Wear OS

Seat-Scout helps Boston University students find study spaces and record seating and noise observations. The phone and watch use the same directory of 36 study spaces across Charles River, Fenway and Medical Campus. Each device keeps its own bookmarks. Reports are currently stored on the phone.

## Build and install

Open this directory in Android Studio with the bundled JDK and Android SDK 36 installed. Select the app module for a phone running Android 6 or later. Select wear for a Wear OS watch on API 30 or later. To build without starting an emulator, run these commands from this directory in PowerShell.

```powershell
.\gradlew.bat --max-workers=1 :app:assembleDebug :wear:assembleDebug :app:testDebugUnitTest
adb devices
adb -s PHONE_SERIAL install -r app/build/outputs/apk/debug/app-debug.apk
adb -s WATCH_SERIAL install -r wear/build/outputs/apk/debug/wear-debug.apk
```

Replace the serials with the device IDs from adb. Use the same signing certificate when updating an existing installation to retain local data. Gradle needs network access for dependencies on the first build.

## Map setup

The bundled offline map works immediately. It has 15 distinct street-address pins, selectable rooms, zoom and pan, and 953 segments of public road geometry. The coordinates describe buildings. Two libraries or multiple rooms can share one address. Pins come from the [City of Boston SAM locator](https://geocoder.boston.gov/arcgis/rest/services/SAMLocator/GeocodeServer). Streets come from [U.S. Census TIGERweb](https://tigerweb.geo.census.gov/arcgis/rest/services/TIGERweb/Transportation/MapServer).

To enable Google tiles, enable Maps SDK for Android and billing in the Google Cloud project. Restrict the Android key to Maps SDK for Android, package edu.bu.seatscout, and the SHA-1 of your signing certificate. Run `.\gradlew.bat :app:signingReport` to find the fingerprint. Add this line to android/local.properties and rebuild the phone APK.

```properties
MAPS_API_KEY=YOUR_RESTRICTED_KEY
```

Git ignores local.properties. Keep it out of source archives. The Android SDK reads the key from the built app manifest, so package and certificate restrictions matter. After rebuilding, the Map tab can switch between Google and offline maps. An enabled key, network access and compatible Google Play services are needed to see Google tiles. “Open in maps” on a space opens an installed map application.

## Using the frontend

- **Discover** searches buildings, addresses, campuses and spaces. Power outlets uses documented amenities. Quiet now uses a fresh local observation.
- **Find a place from a photo** reads a campus sign or labeled map using the bundled ML Kit Latin text recognizer. Choose a match to open its pin and inspect its rooms. Blurry signs or photographs without identifying words can return no match. The system photo picker grants access to the selected image. Decoding is size-limited, and the app processes pixels in memory on the device. Seating and noise require student observations.
- **Map** lets students explore buildings, switch campuses and open rooms. **Saved** retains selected rooms on that device.
- **Update conditions** records seating and atmosphere separately. Review both values before saving. Observations expire after 30 minutes. Room access and availability can change independently of published information. Each location links to its BU source.
- **Watch** presents campus, building and room lists, bookmarks and an external map action. Phone-to-watch account or report synchronization belongs to a later backend integration.

## Where to make changes

| Feature | Source under this directory |
| --- | --- |
| Shared room IDs, metadata, source links, catalog | shared/src/main/kotlin/edu/bu/seatscout/ |
| Phone screens and routing | MainActivity.kt, BrowseScreen.kt, SpaceDetails.kt, ReportScreen.kt |
| Geographic pin data and map interaction | CampusCoordinates.kt, CampusMap.kt, OfflineCampusMap.kt, GoogleCampusMap.kt |
| Read a sign and rank catalog matches | PhotoLocationFinder.kt, PhotoMatch.kt |
| Phone state and persistence | ScoutViewModel.kt, Reports.kt |
| Glass dock and shared visual components | Glass.kt, StudyUi.kt |
| Standalone watch UI | wear/src/main/kotlin/edu/bu/seatscout/wear/WatchActivity.kt |

Phone Kotlin files live in app/src/main/kotlin/edu/bu/seatscout/. Update the research inventory and shared catalog together when a room changes. A new street address also needs a geocoded pin and coverage inside the bundled street map.

## Contract for the backend partner

Choose a provider, authentication scheme, write authorization and report aggregation policy before connecting the shared service. Implement ReportRepository in Reports.kt and select it in SeatScoutApp.repository. The existing Room repository is the offline reference.

- `reports: Flow<List<Report>>` emits the latest observation per stable areaId. A read failure surfaces as ScoutState.readFailed. Maintain stable catalog IDs so existing reports and bookmarks still resolve.
- `save(area, seats, atmosphere)` must return after a durable write and throw on failure. The review screen retains the draft on failure. Use server timestamps in Unix epoch milliseconds. Map Seats and Atmosphere explicitly. A future timestamp or an age of 30 minutes or more produces unknown conditions.
- Define how multiple student reports are combined and how conflicting, stale or pending observations appear. Update the local-only wording when shared delivery is implemented. Scope writes and saved data to the signed-in user. Test posting on one device and observing the result on a second device before inviting students.
- Keep the on-device photo processing path independent of account and reporting APIs. The current frontend needs no image storage service.

Published BU pages were reviewed on November 16, 2026. Campus coordinates were matched on November 18, 2026. See the accompanying campus research and validation notes for source coverage and checks performed.
