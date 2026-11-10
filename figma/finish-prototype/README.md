# Apply the Seat-Scout redesign

This is a **local Figma development plugin**, not a running app or Google Maps integration.

1. Open the existing SeatScout file in the Figma desktop app.
2. Import this folder's `manifest.json` as a local development plugin.
3. Run **Seat-Scout · Apple redesign and finish**.
4. On the **02 · Seat-Scout / Apple prototype** page, present the **Seat-Scout · Find your focus** starting point.

The plugin updates the existing mobile page, completes the nine Full-seat report combinations, gives the Power shortcut and secondary map pins distinct destinations, and applies the design documented in the project root's `DESIGN.md`. The old green web-reference page is outside its scope. Figma undo can revert the plugin's canvas edits.

No API key, network access, or authentication is used. The map, reports, and location prompts are illustrative. It uses SF Pro if available, otherwise Inter or DM Sans.

This development manifest omits a publishing ID; [Figma assigns plugin IDs](https://developers.figma.com/docs/plugins/manifest/) when creating or publishing a plugin. If your desktop version requires a generated ID for import, create a local plugin in Figma and point its generated manifest at this folder's `code.js` instead of inventing an ID.

Verification: JavaScript syntax and four pure design/motion checks pass. The connector's exhausted Starter quota prevented running the helper in Figma or checking its final rendered layout. Inspect all main screens after running it, especially text wrapping, long report values, and the secondary library details.
