# Seat-Scout browser prototype — November 4, 2026

## Applied refresh

The latest refresh adds a compact burgundy feature card, a campus vignette on wide layouts, distinct full-width space illustrations, semantic seating colors (Unknown gray, A few seats amber, Full red), a search affordance, translucent navigation with icons and selected states, and 15 tappable map markers across the three layouts. Report entry moves from the bottom; question navigation mirrors forward and back directions. Other navigation uses a short crossfade.

After the refresh, the structural audit found 144 frames, 816 navigation connections, no missing destinations and no text nodes with empty widths. Phone, tablet, desktop and map compositions were visually reviewed. Wide feature cards were corrected after auto-layout conversion and desktop was rechecked. The previously documented simulated-data and preset-search limitations still apply.

Created in the user-provided EDU-account file `mr4UoGANhRLj4pZcOpTPDm`.

- [Editable design](https://www.figma.com/design/mr4UoGANhRLj4pZcOpTPDm/Seat-Scout?node-id=4-3508)
- [Phone prototype](https://www.figma.com/proto/mr4UoGANhRLj4pZcOpTPDm/Seat-Scout?page-id=0%3A1&node-id=4-3508&scaling=scale-down)
- [Tablet prototype](https://www.figma.com/proto/mr4UoGANhRLj4pZcOpTPDm/Seat-Scout?page-id=0%3A1&node-id=4-5394&scaling=scale-down)
- [Desktop prototype](https://www.figma.com/proto/mr4UoGANhRLj4pZcOpTPDm/Seat-Scout?page-id=0%3A1&node-id=4-7273&scaling=scale-down)

## Applied design

Apple-inspired white and soft gray surfaces, dark typography, rounded cards and capsule actions, BU scarlet primary actions. SF Pro appeared in font discovery but rendered empty glyphs; Inter is the visually verified fallback. The Apple component library rejected imports, so the screens use editable native text, vector illustrations and auto-layout containers.

Three independently connected layouts: phone 402 × 874, tablet 834 × 1112, desktop 1440 × 1000. These are separate Figma presentation flows, not a runtime page that automatically switches breakpoints. Catalogs use one, two and three columns respectively. Content scrolls within each screen with navigation below it.

## Included interactions

48 screens per layout (144 total), 801 navigation connections. Discover, preset search, quiet and power filters, five space detail pages, illustrative map, save/remove states, directions previews, and a Mugar reporting walkthrough. All nine combinations of three seating observations and three noise observations reach a matching review and confirmation. Other spaces explicitly direct reporting into the Mugar walkthrough.

Search uses preset results. Favorites are simulated states rather than persistent data. Reports are simulated, maps are illustrative, and directions previews do not calculate routes. No API keys, live Google Maps, authentication, Firebase deployment or Supabase shared reports are connected.

## Verification

Structural audit confirmed 144 top-level frames, three flow starts and no missing navigation destinations. Screenshots reviewed phone discovery/detail, tablet and desktop discovery, and phone Full/Noisy review. The SF Pro rendering failure was fixed and phone discovery was rechecked successfully. Prototype playback, every screen's visual appearance, and another user's viewing permission have not been independently verified.

The file owner controls sharing. If a friend cannot view the links, grant them viewing access through Figma's Share control.

`figma/build-seat-scout.js` records the creation recipe. It is intended for a blank file, is not safe to rerun against this finished canvas, and requires the appropriate SIZE parameters for each layout.
