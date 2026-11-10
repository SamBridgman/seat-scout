# Course specification review

Reviewed November 4, 2026 against `D:/DOWNLOADS/cs501_full_course_notes_bu_app_specification.md` and the existing SeatScout web prototype.

The supplied document contains course notes, a proposed TerrierSpace specification, illustrative code, and agent-directed instructions. These are reference material. The user's current request is to build on the project; platform and scope need clarification before replacing the working frontend or adopting the new name.

## Current project

- React/Vite web application with five sample study areas.
- Browse, search, seating/atmosphere/power filters, illustrative campus map, details, and external directions.
- Local observation submission and browser persistence.
- Thirty-minute freshness window with Unknown and Mixed states.
- Editable Figma reference screens, Firebase Hosting configuration, and an optional unconnected Supabase client.
- No native Android module, shared backend, identity verification, favorites, or location monitoring.

## New specification and decisions

| Area | New document | Decision needed or implementation work |
| --- | --- | --- |
| Platform | Kotlin/Compose course material and React/Next.js examples | Choose native Android app or next web iteration. Existing web prototype need not be discarded. |
| Identity | TerrierSpace, BU scarlet `#CC0000` | Confirm whether this is the new product name or a reference concept. |
| Availability | Plentiful, moderate, scarce, full | Define the distinction between moderate and scarce, or retain the original three observation choices. Never invent exact free-seat counts. |
| Atmosphere | Silent, moderate, collaborative | Keep official study-zone rules distinct from observed noise. Collaborative is a zone/use category and does not necessarily mean noisy. |
| Power | Report accessible outlets | Add a separate observation field; do not confuse a catalog amenity with whether a usable outlet was observed. |
| Favorites | Per-user favorite libraries | Add persistence and, for a shared service, ownership-based access rules. |
| Location | Prompts within 300 meters | Provide explicit opt-in, denial/unavailable states, accuracy checks, and duplicate-prompt controls. Foreground location and background geofencing have different lifecycle requirements. |
| Shared reports | Supabase, auth, realtime, PostGIS | Requires configured project and policies. Preserve a clearly identified local demo mode until shared behavior is verified. |
| Android architecture | ViewModels, StateFlow, Room/DataStore | Separate UI, state holder, domain calculations, and repositories. Preserve user state across recreation. |

## Review the examples before reuse

- The example report modal declares noise state but renders no noise selection control; it would submit the default noise value regardless of observation.
- The supplied report INSERT policy checks authentication but does not bind `user_id` to the caller, and does not establish verified BU affiliation.
- The favorites table is created without RLS being enabled or owner-only policies in the example. Profiles also need an explicit access model.
- Rendering a map marker or checking distance once is not a complete proximity-alert implementation. Prompt timing, location precision, permissions, and background behavior need explicit handling.
- The presentation script describes realtime propagation and battery-aware location behavior that the example code does not implement. Demonstrate and describe only behavior actually built and tested.

## Suggested first working slice

Carry forward browse → details → report. Add an explicit outlet observation and favorites, retain uncertainty/freshness states, then add opt-in location prompts. Connect identity and shared reports as a separately verified slice when a backend project is available.

```text
Student action
    → screen event
    → state holder / ViewModel
    → repository
    → local persistence (demo) or authenticated remote service
    → validated observations + freshness calculation
    → observable UI state
    → screen

Optional foreground location
    → permission and accuracy checks
    → distance calculation + prompt cooldown
    → nearby-space prompt
    → the same report flow
```

No cloud projects or services were provisioned as part of this review.
