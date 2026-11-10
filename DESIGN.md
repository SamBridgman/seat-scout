# Seat-Scout · Apple-inspired mobile prototype

The user confirmed the app name **Seat-Scout** and chose a **clickable Figma Design prototype only**. This is the current deliverable; runtime application work, API integration, and cloud deployment are outside this iteration. A Google Maps key is not needed for the illustrative map or external directions links.

## References and interpretation

- [Apple design analysis in the requested design-md collection](https://getdesign.md/apple/design-md): white space, the `#F5F5F7` surface, clear type hierarchy, restrained elevation, and capsule actions. This is an independent design analysis; it is inspiration rather than Apple branding or a literal mobile HIG specification.
- The requested apple-design skill informs spatially consistent motion, immediate feedback, clear navigation, material depth, and readable type.
- The requested Figma motion skill informs easing and timing. This iteration uses prototype transitions rather than timeline animation.

## Visual system

| Role | Value |
| --- | --- |
| App name | Seat-Scout |
| Accent | BU scarlet `#CC0000` |
| Canvas | `#F5F5F7` |
| Surface | `#FFFFFF` |
| Primary text | `#1D1D1F` |
| Secondary text | `#6E6E73` |
| Hairline | `#DEDEE3` |
| Accent wash | `#FFF0F0` |
| Typeface | Available SF Pro family; Inter fallback; DM Sans final fallback |
| Shapes | Rounded cards, capsule actions, circular controls |
| Materials | Light translucent navigation with background blur |

Retain seating and atmosphere as separate observations. Keep unknown and conflicting reports explicit. Use only illustrative campus information until verified. Preserve editable text, vector illustrations, and auto-layout groups.

## Prototype motion

- Report sheet: enter and dismiss through the bottom edge.
- Question navigation: push forward left, back right; approximately 260 ms.
- Tab changes: brief 200 ms crossfade.
- Use a restrained ease-out; avoid idle loops, arbitrary bounce, or decorative text reveals.

The local finishing helper applies this design to the existing mobile prototype, completes its Full-seat combinations, and sets presentation starting points. It does not modify the old green web-reference page.

**Application status:** prepared locally, not applied to the Figma canvas. The Figma connector's Starter quota blocked reads/edits on this turn. Final canvas inspection and motion playback remain unverified.
