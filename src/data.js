export const spaces = [
  { id: 'mugar-4', name: 'Mugar Memorial Library', area: '4th floor · Open study', category: 'Library', zone: 'Silent', power: true, walk: 4, building: '771 Commonwealth Avenue, Boston, MA', description: 'A spot for getting into deep work. Explore individual study desks and find a quieter corner away from the bustle of campus.', amenities: ['Power outlets', 'Individual desks', 'Wi-Fi', 'Accessible building'], art: 'library', color: '#e7ebe3', seats: 'Few seats', atmosphere: 'Quiet', age: 5, reports: 3, x: 43, y: 51 },
  { id: 'pal', name: 'Mugar PAL Lounge', area: '1st floor · Student lounge', category: 'Lounge', zone: 'Collaborative', power: true, walk: 4, building: '771 Commonwealth Avenue, Boston, MA', description: 'A more social place to work through an assignment with a friend. A good option when a little background conversation helps you think.', amenities: ['Power outlets', 'Shared tables', 'Wi-Fi', 'Accessible building'], art: 'lounge', color: '#eee6dc', seats: 'Many seats', atmosphere: 'Noisy', age: 2, reports: 2, x: 47, y: 60 },
  { id: 'stone', name: 'Stone Science Library', area: '2nd floor · Reading room', category: 'Library', zone: 'Quiet', power: false, walk: 7, building: '675 Commonwealth Avenue, Boston, MA', description: 'A smaller reading-room setting for solo study. There are no fresh observations right now; check the space and help the next student with a report.', amenities: ['Reading tables', 'Wi-Fi', 'Natural light'], art: 'stone', color: '#e2e9ed', seats: 'Many seats', atmosphere: 'Quiet', age: 90, reports: 1, x: 74, y: 40 },
  { id: 'pardee', name: 'Pardee Management Library', area: '3rd floor · Study area', category: 'Library', zone: 'Quiet', power: true, walk: 9, building: '595 Commonwealth Avenue, Boston, MA', description: 'Settle into a library study area for reading, notes, and coursework. Seating and noise can vary, so look at the latest observations before heading over.', amenities: ['Power outlets', 'Study tables', 'Wi-Fi', 'Natural light'], art: 'pardee', color: '#e8e4ed', seats: 'Many seats', atmosphere: 'Moderate', age: 8, reports: 2, x: 86, y: 62 },
  { id: 'science', name: 'Science & Engineering Library', area: '2nd floor · Open study', category: 'Library', zone: 'Quiet', power: true, walk: 6, building: '38 Cummington Mall, Boston, MA', description: 'Make room for your next problem set. This illustrative study area offers a mix of study tables and focused workspaces.', amenities: ['Power outlets', 'Study tables', 'Wi-Fi'], art: 'science', color: '#e4eade', seats: 'Full', atmosphere: 'Quiet', age: 12, reports: 2, x: 26, y: 72 },
];

export const SEATS = ['Many seats', 'Few seats', 'Full'];
export const ATMOSPHERES = ['Quiet', 'Moderate', 'Noisy'];
export const FRESH_MS = 30 * 60 * 1000;
export const demoStartedAt = Date.now();
export const seedReports = spaces.flatMap(space => Array.from({ length: space.reports }, (_, i) => ({ id: `sample-${space.id}-${i}`, spaceId: space.id, seats: space.seats, atmosphere: space.atmosphere, createdAt: demoStartedAt - (space.age + i * 2) * 60000, sample: true })));

export function conditions(spaceId, reports, now = Date.now()) {
  const recent = reports.filter(r => r.spaceId === spaceId && r.createdAt <= now && now - r.createdAt < FRESH_MS).sort((a, b) => b.createdAt - a.createdAt);
  const summarize = key => !recent.length ? 'Unknown' : new Set(recent.map(r => r[key])).size > 1 ? 'Mixed' : recent[0][key];
  return { seats: summarize('seats'), atmosphere: summarize('atmosphere'), count: recent.length, newest: recent[0]?.createdAt ?? null };
}

export function ageLabel(timestamp, now = Date.now()) {
  if (!timestamp) return 'No recent reports';
  const mins = Math.max(0, Math.floor((now - timestamp) / 60000));
  return mins === 0 ? 'Just now' : `${mins} min ago`;
}

export function loadReports(storage) {
  try {
    const data = JSON.parse(storage.getItem('seatscout.reports.v1') || '[]');
    if (!Array.isArray(data)) return [];
    return data.filter(r => r && typeof r.id === 'string' && spaces.some(s => s.id === r.spaceId) && SEATS.includes(r.seats) && ATMOSPHERES.includes(r.atmosphere) && Number.isFinite(r.createdAt) && r.createdAt <= Date.now() && r.sample !== true).slice(-100);
  } catch { return []; }
}
