import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

const context = vm.createContext({});
vm.runInContext(readFileSync(new URL('../figma/finish-prototype/code.js', import.meta.url), 'utf8'), context);
const design = context.SeatScoutDesign;

test('palette conversion preserves BU scarlet and round-trips design colors', () => {
  for (const hex of ['#CC0000', '#FFFFFF', '#F5F5F7', '#1D1D1F']) assert.equal(design.hex(design.rgb(hex)), hex);
  assert.equal(design.palette['#FBFAF9'], '#F5F5F7');
  assert.equal(design.palette['#CC0000'], undefined);
});

test('prototype navigation mirrors forward and back directions', () => {
  const forward = design.transition('05 · Report / Seating', 'Report atmosphere / Full', 'choice.Seats.3');
  const backward = design.transition('Report atmosphere / Full', '05 · Report / Seating', 'report.back');
  assert.equal(forward.type, 'PUSH');
  assert.equal(forward.direction, 'LEFT');
  assert.equal(backward.type, 'PUSH');
  assert.equal(backward.direction, 'RIGHT');
});

test('report entry and dismissal use the same bottom edge', () => {
  const entry = design.transition('03 · Mugar / Details', '05 · Report / Seating', 'Action / Report conditions');
  const exit = design.transition('05 · Report / Seating', '03 · Mugar / Details', 'report.back');
  assert.equal(entry.type, 'MOVE_IN');
  assert.equal(exit.type, 'MOVE_OUT');
  assert.equal(entry.direction, exit.direction);
  assert.equal(entry.direction, 'BOTTOM');
});

test('navigation tabs crossfade without unnecessary spatial motion', () => {
  const transition = design.transition('01 · Discover', '04 · Saved spaces', 'Nav item 02');
  assert.equal(transition.type, 'DISSOLVE');
  assert.ok(transition.duration <= 0.25);
});
