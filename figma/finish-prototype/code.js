// Local Figma development plugin. No network requests or account credentials.
// The Figma MCP Starter quota prevents applying these edits through the connector.
// Run this inside the existing SeatScout Figma file, not in a new file.

const SeatScoutDesign = {
  palette: { '#FBFAF9': '#F5F5F7', '#24262B': '#1D1D1F', '#7C8088': '#6E6E73', '#E8E8E9': '#DEDEE3', '#FCECED': '#FFF0F0' },
  rgb(hex) { return { r: parseInt(hex.slice(1, 3), 16) / 255, g: parseInt(hex.slice(3, 5), 16) / 255, b: parseInt(hex.slice(5, 7), 16) / 255 }; },
  hex(color) { return '#' + ['r', 'g', 'b'].map(key => Math.round(color[key] * 255).toString(16).padStart(2, '0')).join('').toUpperCase(); },
  textSize(size) { return size <= 10 ? 11 : size <= 16 ? size + 1 : size === 21 ? 22 : size >= 30 && size <= 34 ? 34 : size; },
  transition(sourceName, targetName, controlName) {
    const ease = { type: 'EASE_OUT' };
    if (controlName === 'Action / Report conditions') return { type: 'MOVE_IN', direction: 'BOTTOM', matchLayers: false, easing: ease, duration: 0.32 };
    const isBack = /back/i.test(controlName);
    if (/Report (atmosphere|outlets)|Report \/ (Seating|Atmosphere|Outlets)/.test(sourceName) && /Report (atmosphere|outlets)|Report \/ (Seating|Atmosphere|Outlets)/.test(targetName)) {
      return { type: 'PUSH', direction: isBack ? 'RIGHT' : 'LEFT', matchLayers: false, easing: ease, duration: 0.26 };
    }
    if (sourceName.includes('Report / Seating') && isBack) return { type: 'MOVE_OUT', direction: 'BOTTOM', matchLayers: false, easing: ease, duration: 0.24 };
    return { type: 'DISSOLVE', easing: ease, duration: 0.2 };
  },
};
if (typeof globalThis !== 'undefined') globalThis.SeatScoutDesign = SeatScoutDesign;

async function main() {
  const page = await figma.getNodeByIdAsync('7:2');
  if (!page || page.type !== 'PAGE') {
    throw new Error('Open the existing SeatScout Figma file with its mobile prototype page.');
  }
  await figma.setCurrentPageAsync(page);
  for (const style of ['Regular', 'Medium', 'SemiBold', 'Bold']) {
    await figma.loadFontAsync({ family: 'DM Sans', style });
  }

  const seats = ['Plenty of seats', 'Some seats', 'A few seats', 'Full'];
  const atmospheres = ['Quiet', 'Moderate', 'Noisy'];
  const outlets = ['Yes, accessible', 'No, unavailable', 'Not sure'];
  const fullAtmosphereId = '13:6173';
  const fullOutletIds = ['13:6589', '13:6635', '13:6681'];
  const reviewTemplate = await figma.getNodeByIdAsync('7:10');
  const updatedTemplate = await figma.getNodeByIdAsync('7:12');
  const home = await figma.getNodeByIdAsync('7:3');

  if (!reviewTemplate || !updatedTemplate || !home) {
    throw new Error('Required prototype screens are missing. No edits were made.');
  }

  const find = (frame, name) => {
    const node = frame.findOne(child => child.name === name);
    if (!node) throw new Error(`Missing layer "${name}" in "${frame.name}".`);
    return node;
  };
  const setText = async (node, text) => {
    for (const segment of node.getStyledTextSegments(['fontName'])) {
      await figma.loadFontAsync(segment.fontName);
    }
    node.characters = text;
  };
  const navigate = (node, destinationId) => node.setReactionsAsync([{
    trigger: { type: 'ON_CLICK' },
    actions: [{
      type: 'NODE', destinationId, navigation: 'NAVIGATE',
      transition: { type: 'DISSOLVE', easing: { type: 'EASE_OUT' }, duration: 0.2 },
      resetScrollPosition: true,
    }],
  }]);

  let screenIndex = 68;
  const getOrClone = (template, name) => {
    const slot = screenIndex;
    screenIndex += 1;
    const existing = page.children.find(node => node.type === 'FRAME' && node.name === name);
    if (existing) return existing;
    const frame = template.clone();
    frame.name = name;
    frame.x = 100 + (slot % 7) * 482;
    frame.y = 4500 + Math.floor(slot / 7) * 1034;
    return frame;
  };

  // Complete the nine "Full" combinations. All actions remain Starter-compatible.
  for (let atmosphereIndex = 0; atmosphereIndex < atmospheres.length; atmosphereIndex += 1) {
    const outletQuestion = await figma.getNodeByIdAsync(fullOutletIds[atmosphereIndex]);
    if (!outletQuestion) throw new Error('A Full-seat outlet question is missing.');
    for (let outletIndex = 0; outletIndex < outlets.length; outletIndex += 1) {
      const suffix = `Full / ${atmospheres[atmosphereIndex]} / ${outlets[outletIndex]}`;
      const review = getOrClone(reviewTemplate, `Review / ${suffix}`);
      const updated = getOrClone(updatedTemplate, `Shared observation / ${suffix}`);
      for (const frame of [review, updated]) {
        await setText(find(frame, 'value.Seats'), 'Full');
        await setText(find(frame, 'value.Atmosphere'), atmospheres[atmosphereIndex]);
        await setText(find(frame, 'value.Outlets'), outlets[outletIndex]);
      }
      await navigate(find(review, 'report.submit'), updated.id);
      await navigate(find(review, 'edit.Seats'), '7:7');
      await navigate(find(review, 'edit.Atmosphere'), fullAtmosphereId);
      await navigate(find(review, 'edit.Outlets'), outletQuestion.id);
      await navigate(find(review, 'back / tap'), outletQuestion.id);
      await navigate(find(outletQuestion, `choice.Outlets.${outletIndex}`), review.id);
    }
  }

  // Make the Power shortcut distinct from the Quiet shortcut.
  const quietTemplate = await figma.getNodeByIdAsync('7:16');
  const power = getOrClone(quietTemplate, '15 · Spaces with power outlets');
  const replacements = new Map([
    ['Find your quiet', 'Find a place to plug in'],
    ['A little less noise.', 'Keep your ideas charged.'],
    ['Spaces with recent quiet observations.', 'Study spaces with power-outlet amenities.'],
    ['Reported atmosphere can differ from the official zone.', 'Outlet amenities do not guarantee an available socket.'],
  ]);
  for (const node of power.findAll(node => node.type === 'TEXT')) {
    if (replacements.has(node.characters)) await setText(node, replacements.get(node.characters));
  }
  const selectedFilters = find(power, 'Selected filters');
  const selectedFilterText = selectedFilters.findOne(node => node.type === 'TEXT' && node.characters === 'Quiet');
  if (selectedFilterText) await setText(selectedFilterText, 'Power');
  const filterIcon = selectedFilters.findOne(node => node.name === 'Icon / quiet');
  const sourceZap = home.findOne(node => node.name === 'Icon / zap');
  if (filterIcon && sourceZap) {
    const parent = filterIcon.parent;
    const position = parent.children.indexOf(filterIcon);
    const replacement = sourceZap.clone();
    parent.insertChild(position, replacement);
    filterIcon.remove();
  }
  const homeFilters = find(home, 'Atmosphere filters');
  await navigate(find(homeFilters, 'Power'), power.id);

  // Give the other labeled map pins their own library details.
  const map = await figma.getNodeByIdAsync('7:4');
  const detailsTemplate = await figma.getNodeByIdAsync('7:5');
  const otherLibraries = [
    { pin: 'SEL', name: 'Science & Engineering\nLibrary', title: 'Science & Engineering', area: '2nd floor · Quiet study', seats: 'Full', noise: 'Quiet', address: '38 Cummington Mall', query: '38 Cummington Mall Boston MA' },
    { pin: 'Questrom', name: 'Pardee Management\nLibrary', title: 'Pardee Management', area: '3rd floor · Study area', seats: 'Many seats', noise: 'Moderate', address: '595 Commonwealth Avenue', query: '595 Commonwealth Avenue Boston MA' },
  ];
  for (const library of otherLibraries) {
    const details = getOrClone(detailsTemplate, `Details / ${library.title}`);
    const texts = details.findAll(node => node.type === 'TEXT');
    for (const text of texts) {
      if (text.characters === 'Mugar Memorial\nLibrary') await setText(text, library.name);
      else if (text.characters === '4th floor · Open study') await setText(text, library.area);
      else if (text.characters === 'Official zone: Silent') await setText(text, 'Official zone: Quiet');
      else if (text.characters === '771 Commonwealth Avenue') await setText(text, library.address);
      else if (text.characters === 'Report conditions') await setText(text, 'Back to campus map');
    }
    await setText(find(details, 'value.Seats'), library.seats);
    await setText(find(details, 'value.Atmosphere'), library.noise);
    await navigate(find(details, 'Action / Report conditions'), map.id);
    await navigate(find(details, 'back / tap'), map.id);
    const directions = find(details, 'Directions link');
    await directions.setReactionsAsync([{
      trigger: { type: 'ON_CLICK' },
      actions: [{ type: 'URL', url: `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(library.query)}`, openInNewTab: true }],
    }]);
    await navigate(find(map, `Map pin / ${library.pin}`), details.id);
    if (library.pin === 'SEL') await navigate(find(home, 'Space / Science & Engineering'), details.id);
  }

  page.flowStartingPoints = [
    { nodeId: '7:3', name: 'Seat-Scout · Find your focus' },
    { nodeId: '7:7', name: 'Share an observation' },
    { nodeId: '7:13', name: 'Preview nearby reminders' },
  ];

  // Check every modeled combination has review values and a submission destination.
  const missing = [];
  for (const seating of seats) for (const atmosphere of atmospheres) for (const outlet of outlets) {
    const name = `Review / ${seating} / ${atmosphere} / ${outlet}`;
    const review = page.children.find(node => node.type === 'FRAME' && node.name === name);
    if (!review || find(review, 'report.submit').reactions.length === 0) missing.push(name);
  }
  if (missing.length) throw new Error(`${missing.length} report combinations still need attention.`);

  // One typography pass after branch completion keeps all alternate states consistent.
  const availableFonts = (await figma.listAvailableFontsAsync()).map(entry => entry.fontName);
  const family = preferred => preferred.find(name => availableFonts.some(font => font.family === name));
  const displayFamily = family(['SF Pro Display', 'SF Pro', 'Inter', 'DM Sans']);
  const bodyFamily = family(['SF Pro Text', 'SF Pro', 'Inter', 'DM Sans']);
  if (!displayFamily || !bodyFamily) throw new Error('No supported text family is available.');
  const chooseFont = (fontFamily, currentStyle = 'Regular') => {
    const key = currentStyle.toLowerCase().replace(/[^a-z]/g, '');
    const desired = key.includes('semibold') ? 'semibold' : key.includes('bold') ? 'bold' : key.includes('medium') ? 'medium' : 'regular';
    return availableFonts.find(font => font.family === fontFamily && font.style.toLowerCase().replace(/[^a-z]/g, '') === desired)
      || availableFonts.find(font => font.family === fontFamily && font.style === 'Regular');
  };
  const loadedFonts = new Set();
  const loadFont = async font => {
    const key = JSON.stringify(font);
    if (!loadedFonts.has(key)) { await figma.loadFontAsync(font); loadedFonts.add(key); }
  };
  const recolor = paints => Array.isArray(paints) ? paints.map(paint => {
    if (paint.type !== 'SOLID') return paint;
    const replacement = SeatScoutDesign.palette[SeatScoutDesign.hex(paint.color)];
    if (!replacement) return paint;
    const { boundVariables, ...unboundPaint } = paint;
    return { ...unboundPaint, color: SeatScoutDesign.rgb(replacement) };
  }) : paints;
  const nodes = page.findAll(() => true);
  const alreadyStyled = page.name === '02 · Seat-Scout / Apple prototype';
  for (const node of nodes) {
    if (node.type === 'TEXT') {
      const segments = node.getStyledTextSegments(['fontName']);
      for (const segment of segments) await loadFont(segment.fontName);
      if (/TerrierSpace|SeatScout/.test(node.characters)) node.characters = node.characters.replace(/TerrierSpace|SeatScout/g, 'Seat-Scout');
      if (typeof node.fontSize === 'number') {
        const font = chooseFont(node.fontSize >= 25 ? displayFamily : bodyFamily, segments[0]?.fontName.style);
        await loadFont(font);
        node.fontName = font;
        if (!alreadyStyled) node.fontSize = SeatScoutDesign.textSize(node.fontSize);
        node.letterSpacing = { unit: 'PERCENT', value: node.fontSize >= 25 ? -2 : 0 };
      }
    }
    if ('fills' in node && Array.isArray(node.fills)) node.fills = recolor(node.fills);
    if ('strokes' in node && Array.isArray(node.strokes)) node.strokes = recolor(node.strokes);
    if ('cornerRadius' in node && node.type === 'FRAME' && node.parent?.type === 'PAGE') node.cornerRadius = 34;
    if (/^Action \//.test(node.name) && 'cornerRadius' in node) node.cornerRadius = 27;
    if (node.name === 'Bottom navigation') {
      node.fills = [{ type: 'SOLID', color: SeatScoutDesign.rgb('#FFFFFF'), opacity: 0.9 }];
      node.effects = [{ type: 'BACKGROUND_BLUR', radius: 24, visible: true }];
    }
    if ('reactions' in node && node.reactions.length) {
      let source = node;
      while (source.parent && source.parent.type !== 'PAGE') source = source.parent;
      const reactions = [];
      for (const reaction of node.reactions) {
        const actions = [];
        for (const action of reaction.actions || []) {
          if (action.type === 'NODE' && action.navigation === 'NAVIGATE' && action.destinationId) {
            const destination = await figma.getNodeByIdAsync(action.destinationId);
            actions.push({ ...action, transition: SeatScoutDesign.transition(source.name, destination?.name || '', node.name) });
          } else actions.push(action);
        }
        reactions.push({ ...reaction, actions });
      }
      await node.setReactionsAsync(reactions);
    }
    node.name = node.name.replace(/TerrierSpace/g, 'Seat-Scout');
  }
  page.name = '02 · Seat-Scout / Apple prototype';
  const staleNames = page.findAll(node => node.type === 'TEXT' && /TerrierSpace/.test(node.characters));
  if (staleNames.length) throw new Error('Some obsolete app-name labels remain.');
  figma.viewport.scrollAndZoomIntoView([home]);
  figma.closePlugin('Seat-Scout: Apple-style design, 36 report paths, and 3 presentation starts. Inspect the screens before presenting.');
}

if (typeof figma !== 'undefined') main().catch(error => figma.closePlugin(`Could not finish: ${error.message}`));
