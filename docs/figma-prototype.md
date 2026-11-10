# TerrierSpace Figma prototype

**Latest direction:** the user confirmed **Seat-Scout** as the app name and selected a clickable Figma Design prototype only. The Apple-inspired redesign is specified in `DESIGN.md` and prepared in the local finishing plugin. It has not been applied to the canvas because the Figma connector's Starter quota is exhausted. The remaining text below describes the last confirmed canvas state.

Built November 4, 2026 in the existing SeatScout file, on the new page **02 · TerrierSpace / Mobile prototype**. The original green web-reference frames remain on Page 1.

- [Open the new mobile design](https://www.figma.com/design/GU8NIjb5MjqueipwZh8Dh4?node-id=7-3)
- [Open the prototype at Discover](https://www.figma.com/proto/GU8NIjb5MjqueipwZh8Dh4?page-id=7%3A2&node-id=7-3&starting-point-node-id=7%3A3&scaling=scale-down)

## Main screens created

1. Discover
2. Campus map
3. Mugar library details
4. Saved spaces
5. Report seating
6. Report atmosphere
7. Report accessible outlets
8. Review observation
9. Thank-you confirmation
10. Submitted observation on the library detail screen
11. Nearby reminders opt-in concept
12. Nearby-library prompt
13. Community updates and freshness explanation
14. Quiet-space filter result

All main screens are 402 × 874, use editable text/vector layers and auto-layout containers, and draw on Material 3 button/navigation instances. The BU scarlet palette and DM Sans typography distinguish this iteration from the previous web demo.

This is a Figma design prototype. It does not perform authentication, send reports, request GPS permission, or communicate with Firebase/Supabase. Maps, library details, observations, walking estimates, and proximity distances are illustrative.

## Presentation path that is connected

Discover → Mugar Memorial Library → Report conditions → A few seats → Quiet → Yes, accessible → Share report → Done → Back to Discover.

Alternative report states are laid out farther down the same page because prototype navigation must remain on one page. Twenty-seven combinations of seating, atmosphere, and outlet observations were created and connected before Figma stopped further tool calls at the Starter-plan quota. The first three seating choices each have nine complete combinations. The Full seating question reaches its atmosphere/outlet screens, but its nine review/submitted states remain pending.

## Remaining edits and local finishing helper

The finishing plugin is prepared at `figma/finish-prototype/manifest.json`. Import that manifest as a local development plugin in the Figma desktop app, open the existing file, and run **Seat-Scout · Apple redesign and finish**. It requires no network access or credentials. It also applies the Seat-Scout name, Apple-inspired typography/materials, and directional prototype transitions.

It completes the nine Full-seat branches, adds three presentation starting points, makes Power a distinct filter shortcut, and gives the SEL and Questrom pins their own detail screens. Until run, Power opens the Quiet example, and those secondary pins open the Mugar example. The primary Mugar reporting flow is already connected.

The plugin checks that all 36 report combinations have a review state and a submission destination. Re-running it reuses named screens. It has been syntax-checked locally but **has not been executed in Figma**, because the connected tool quota was exhausted. Run it and visually inspect the result before presenting every alternate path.

## Verification recorded

- Inspected the existing file and available typefaces and libraries.
- Created 14 main editable screens; inspected a rendered Discover screenshot.
- Connected the default reporting, map/list, Saved, Updates, and nearby-preview paths.
- Tool results confirm 27 complete report combinations with matching review/submitted text.
- Further canvas inspection, remaining branch edits, and final visual checks were blocked by the Figma Starter tool-call quota.
