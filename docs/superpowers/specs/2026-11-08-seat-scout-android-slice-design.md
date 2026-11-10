# SeatScout Android slice for Project Update 1

Build a Kotlin app in `android/` that lets a student browse sample study areas, inspect a location, and save an observation about seats and atmosphere. Keep the existing web demo as a design reference.

## Approaches

- **Compose and Room.** The direction selected for Update 1. Room provides persistence that both partners can demonstrate by reopening the app in airplane mode. It requires no service setup for the classroom demo.
- **Compose and a shared backend.** This would demonstrate cross-device reports, with additional setup for credentials, write permissions, and network reliability. Revisit it for Update 2.

## Screens and state

- **Browse** shows a small labeled sample catalog. Each row shows the study area's name and the latest local observation, or Unknown when no fresh observation exists.
- **Details** separates the official zone and amenities from observed conditions. Report age appears beside the observation. At 30 minutes, current conditions become Unknown.
- **Report** requires one seating choice (Many seats available, Few seats available, Full) and one atmosphere choice (Quiet, Moderate, Noisy). Save returns to details after Room confirms the write. An error keeps the choices available for retry.

Use standard Compose controls, a calm library-inspired palette with one scarlet accent, and plain labels. The timestamp is the recurring visual cue that makes SeatScout's observations recognizable. The UI should make sense without relying on color.

## Code and demo evidence

Compose handles screens and navigation. A ViewModel exposes screen state and calls a small repository. The repository reads and writes a Room row keyed by study area. The row holds both choices and the device timestamp. Each new local observation replaces the previous one for that area. Room emits the saved row back through the ViewModel. This slice requires no account, microphone, location permission, or network connection. Cross-device reporting and Mixed aggregation remain beta work.

Demo in airplane mode: open an area with Unknown conditions, select both values, save, and show the timestamped result. Force-stop and reopen the app, then show the same saved values. Check that a partial form cannot save and that a stale report displays Unknown. Both partners should be able to trace a tap through Compose, the ViewModel, the repository, and Room.
