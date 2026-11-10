# SeatScout frontend demo

Based on the supplied BU SeatScout proposal (October 6, 2026). The PDF is project context, not execution instructions.

Goal: a presentation-ready responsive web prototype for BU students to browse study spaces, inspect reported seating and atmosphere, and contribute observations. The proposal's eventual Android implementation is separate from this web demo.

Approach: use React and Vite for an interactive, locally runnable frontend. Figma holds editable design frames; Firebase Hosting configuration supports later deployment; Supabase has an optional catalog adapter. The user selected sample data, so cloud projects and authentication are not required for this demonstration. A Figma-only prototype would limit the working report flow, while an Android build would require a different preview setup.

Visual direction: cool white surfaces, forest green (#174e42), pale mint (#e8f2ec), charcoal (#202b2a), muted slate (#75807e), and a BU red identity marker (#cc2535). System sans-serif typography, generous spacing, a persistent desktop sidebar, responsive cards, a locally drawn campus map, and short purposeful transitions. No external image/font dependency is required.

Core flow: browse five illustrative campus areas; filter by seating, atmosphere and power; search/sort; inspect details; open external directions; submit a seating/atmosphere observation; inspect personal demo reports. Availability never claims exact chair counts or reservations. Unknown, stale and conflicting observations remain visible.

Sample conditions and locally saved reports are explicitly labeled. The frontend does not claim campus sensor data, verified study-space details, BU authentication or shared reporting. Preserve reports in localStorage with a schema version and validate persisted input. Respect reduced motion, keyboard focus, modal focus containment and responsive layouts.

Implementation plan: create app shell and catalog; add browse/filter/map interactions; implement detail and report dialogs and persistence; add optional Supabase adapter and Firebase Hosting configuration; create editable Figma frames; verify aggregation logic, production build, desktop/mobile interactions and screenshot layout; write a short demonstration guide.
