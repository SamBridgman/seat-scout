"""Package only the demo and reviewed project files; verify an extracted copy."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
from uuid import uuid4

root = Path(__file__).resolve().parent.parent
archive = root / 'Seat-Scout-Complete-Handoff.zip'
prefix = 'Seat-Scout/'
files = {
    'OPEN-SEAT-SCOUT.html': root / 'Seat-Scout.html',
}
for name in ['package.json', 'package-lock.json', 'index.html', 'vite.config.js',
             'firebase.json', '.env.example', '.gitignore', 'DESIGN.md']:
    files['Source/' + name] = root / name
for folder in ['src', 'tests', 'scripts', 'figma']:
    for path in sorted((root / folder).rglob('*')):
        if path.is_file() and path.suffix in {'.js', '.jsx', '.css', '.mjs', '.py', '.json', '.md'}:
            files['Source/' + path.relative_to(root).as_posix()] = path
files['FIGMA-HANDOFF.md'] = root / 'docs/seat-scout-new-figma.md'
for path in sorted((root / 'docs/screenshots').glob('*.png')):
    files['Earlier-Web-Demo-Previews/' + path.name] = path
for name in ['BU SeatScout Project Proposal (1).pdf', 'cs501_full_course_notes_bu_app_specification.md']:
    path = Path('D:/DOWNLOADS') / name
    if path.is_file():
        files['Project-References/' + name] = path

start = '''SEAT-SCOUT | Campus study-space demo

LATEST APPLE-INSPIRED DESIGN
Extract this ZIP, then double-click FIGMA-LINKS.html.
Choose Phone, Tablet, or Desktop to open the refreshed prototype in a browser.
The editable Figma design is linked on the same page.
144 editable screens, 816 navigation connections, three device layouts.
An internet connection and Figma viewing permission may be required.
Read FIGMA-HANDOFF.md for the complete scope and verification notes.

OPEN THE EARLIER OFFLINE WEB DEMO
1. Extract the entire ZIP (Windows: right-click > Extract All).
2. Open the Seat-Scout folder.
3. Double-click OPEN-SEAT-SCOUT.html. Use Chrome or Edge.

No installation, account, API key, terminal, or internet is needed for the demo.
If it opens as text, right-click the HTML file > Open with > Google Chrome.
Open the extracted file, rather than the preview inside your ZIP application.

TRY IT
Browse five sample study areas, search and filter, switch to the campus map,
open a space, and report its seating and atmosphere. My reports shows your
observations. The layout adapts to desktop and narrow browser windows.
See DEMO-GUIDE.txt for a short presentation walkthrough.

WHAT YOU ARE VIEWING
This is the working web demonstration with the green visual theme.
Locations, amenities, walking times, and conditions are illustrative.
Reports stay in this browser; they are not shared with other students.
If browser storage is blocked, reports last only for the current session.
Directions opens Google Maps in a new tab and needs internet.

FIGMA AND NEXT STEPS
The Apple-inspired refresh is applied in the new EDU-account Figma file.
The cloud file may require the owner's access permission.
The ZIP includes browser links, design notes and creation/refresh scripts.
It does not contain a native .fig export. Figma remains the editable source.
The offline HTML is the earlier green web demo, not the new Figma design.
Live Maps integration, authentication, Firebase deployment, and Supabase
shared reports are not connected. This package contains no API keys.

FOR THE DEVELOPER
Source/ contains the editable React/Vite project and the Figma update helper.
Read Source/README.md before editing. Its index.html is a development entry;
use OPEN-SEAT-SCOUT.html above to run the ready-made demo directly.
'''
guide = '''SEAT-SCOUT | A 90-second walkthrough

1. THE PROBLEM
Students need a study space that has seating and the right atmosphere.
Seat-Scout brings those observations together in one place.

2. FIND A SPACE
Show the five study areas. Search for Mugar. Clear the search.
Choose Quiet under atmosphere and turn on Power outlets.
Reset filters, then switch to Map and select a library pin.

3. SHOW WHAT IS KNOWN
Close the detail panel. Open Stone Science Library.
On a fresh browser, its sample conditions show Unknown because the sample
observation is stale. Unknown is more honest than an outdated availability claim.

4. CONTRIBUTE
Click Report conditions. Choose Many seats and Quiet, then Submit report.
Open Stone again to see the conditions update. Open My reports to see the
timestamped observation. Reload: the report remains if browser storage is allowed.

5. EXPLAIN THE RULES
Observations expire from current conditions after 30 minutes. Conflicting
recent observations show Mixed. There are no reservations or exact seat counts.

Close with: "This frontend uses sample data. Shared reports and live services
are the next integration step."

To repeat the Unknown-to-reported moment, use a fresh browser profile.
Your own old reports remain in My reports even after their conditions expire.
'''
source_readme = '''# Seat-Scout source

The latest Apple-inspired Figma design is linked in ../FIGMA-LINKS.html.
Read ../FIGMA-HANDOFF.md for its scope and limitations.

For the ready-to-open demo, use `../OPEN-SEAT-SCOUT.html`.
This folder contains the editable React/Vite project. Its `index.html` needs
the development server; it is not the standalone demo.

## Develop

Install Node.js 22.12+ and run these commands in this folder:

    npm ci
    npm run dev

Open the local URL printed by Vite. Dependency installation requires internet.
No cloud configuration is required for sample-data mode.

## Build and check

    npm test
    npm run build
    npm run build:offline

The offline build generates `Seat-Scout.html` in this folder.
`node scripts/check-offline.mjs` tests that file in installed Windows Chrome.
The browser executable path in the test may need changing on another system.
`python scripts/package-share.py` creates a new share ZIP after the offline build.

## Project files

- `src/`: interface, sample catalog, conditions, browser-local reports.
- `tests/`: aggregation, persistence validation, Figma helper logic checks.
- `figma/build-seat-scout.js`: creation recipe for the new EDU-account file.
- `figma/refresh-seat-scout.js`: refresh recipe; not safe to rerun unchanged.
- `figma/finish-prototype/`: historical helper for the previous Figma file only.
- `DESIGN.md`: intended Apple-inspired design direction, separate from this web UI.
- `firebase.json`: undeployed hosting configuration.
- `src/backend.js` and `.env.example`: optional, unwired Supabase scaffolding.

The current Figma design has been built, refreshed and visually checked.
The historical helper targets the old file and has not been executed there.
Creation scripts are records for developers, not a one-click importer or .fig export.
Do not run creation/refresh scripts on the finished canvas without adapting them;
they can duplicate layers. Use the linked design for normal collaboration.
'''
figma_links = '''<!doctype html><html lang="en"><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Seat-Scout | Figma handoff</title>
<style>body{font:17px/1.6 system-ui,sans-serif;max-width:720px;margin:64px auto;padding:0 24px;background:#f5f5f7;color:#1d1d1f}h1{font-size:40px;letter-spacing:-1px}a{color:#9f1834}li{margin:16px 0}.note{background:white;border-radius:20px;padding:24px}</style>
<h1>Seat-Scout</h1><p>Design files and prototype</p><ul>
<li><a href="https://www.figma.com/proto/mr4UoGANhRLj4pZcOpTPDm/Seat-Scout?page-id=0%3A1&amp;node-id=4-3508&amp;scaling=scale-down">Open phone prototype</a></li>
<li><a href="https://www.figma.com/proto/mr4UoGANhRLj4pZcOpTPDm/Seat-Scout?page-id=0%3A1&amp;node-id=4-5394&amp;scaling=scale-down">Open tablet prototype</a></li>
<li><a href="https://www.figma.com/proto/mr4UoGANhRLj4pZcOpTPDm/Seat-Scout?page-id=0%3A1&amp;node-id=4-7273&amp;scaling=scale-down">Open desktop prototype</a></li>
<li><a href="https://www.figma.com/design/mr4UoGANhRLj4pZcOpTPDm/Seat-Scout?node-id=4-3508">Open editable Figma design</a></li>
<li><a href="OPEN-SEAT-SCOUT.html">Open earlier offline web demo</a></li></ul>
<div class="note"><strong>Current design status</strong>
<p>The refreshed Apple-inspired Seat-Scout design is applied: 144 editable screens, three device layouts, 816 connections, tappable map markers and a complete Mugar report walkthrough.</p>
<p>Phone, tablet and desktop are separate presentation flows. Search uses preset results; maps, saved states and submissions are simulated. Live Google Maps, authentication and shared cloud reports are not connected.</p>
<p>Figma links require internet and may require permission from the file owner. No offline .fig file is included. The offline web demo uses the earlier green design and sample data. Source files, creation scripts and handoff notes are included.</p></div></html>'''

with ZipFile(archive, 'w', ZIP_DEFLATED) as bundle:
    for name, path in files.items():
        bundle.write(path, prefix + name)
    for name, content in {'START-HERE.txt': start, 'DEMO-GUIDE.txt': guide,
                          'FIGMA-LINKS.html': figma_links, 'Source/README.md': source_readme}.items():
        bundle.writestr(prefix + name, content)

verification = root / '.tools' / ('share-check-' + uuid4().hex[:8])
with ZipFile(archive) as bundle:
    assert bundle.testzip() is None, 'Corrupt ZIP entry'
    for name in bundle.namelist():
        assert name.startswith(prefix) and '..' not in Path(name).parts
        assert Path(name).name not in {'.env', '.env.local'}
    bundle.extractall(verification)
    html = (verification / prefix / 'OPEN-SEAT-SCOUT.html').read_text(encoding='utf-8')
    assert 'src="/src/main.jsx"' not in html and '<style>' in html and '<script>' in html
    assert 'Seat-Scout' in html
    links = (verification / prefix / 'FIGMA-LINKS.html').read_text(encoding='utf-8')
    assert links.count('mr4UoGANhRLj4pZcOpTPDm') == 4
    assert 'GU8NIjb5MjqueipwZh8Dh4' not in links
    assert (verification / prefix / 'Source/figma/refresh-seat-scout.js').is_file()
    print(f'Archive verified: {archive} ({archive.stat().st_size:,} bytes; {len(bundle.namelist())} files)')
print(f'Browser test target: {verification / prefix / "OPEN-SEAT-SCOUT.html"}')
