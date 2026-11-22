# Seat-Scout native interface

Seat-Scout opens with search, current-condition filters and a photo-assisted campus lookup. Students can read building access rules, view the source, save a space and report the seating and atmosphere they observe. Dark appearance follows the system. The Figma prototype records an earlier design iteration.

## Phone

The burgundy accent marks actions and selections on warm neutral surfaces. Serif page titles distinguish screens. System body text, wrapping filters and scalable controls support enlarged text. Wide screens can display two building groups per row.

The navigation dock floats above scrolling content and makes room for the keyboard. Hardware-accelerated Android 12 and newer devices can blur the content below the dock. Android 13 adds edge refraction. Older and software-rendered devices use opaque controls. Content cards have solid backgrounds for reading room names and access information.

Discover opens a real geographic map with published municipal coordinates. Its initial illustration is the entry point. On the Map screen, a selected address reveals its building and available catalog entries. The offline map draws Census street geometry. The phone can also display Google Maps when built with a restricted Maps SDK for Android key. Source and access details remain available through each room.

The photo picker feeds a bundled, on-device text recognizer. Location suggestions show the matched building or room. The student confirms a result before opening the pin. A photograph without a readable campus label produces a recovery prompt. Reports use a separate three-step seating, atmosphere and review flow. Their timestamps and success messages describe storage on the current device.

Navigation uses short native transitions and Material press feedback. Scroll, safe-area and keyboard insets keep interactive controls reachable. See README.md for key configuration, source files and backend boundaries.

## Watch

The Wear OS app uses the shared catalog and scrollable native controls sized for a watch display. Students select a campus, building and space, then read its address and access information, save it locally or open directions. Phone and watch bookmarks currently live on their respective devices.

The design uses Apple-inspired material hierarchy with native Android interactions. Unslop direction adapted from [Damir Sokolovsky](https://heydamir.design/unslop), CC BY 4.0.
