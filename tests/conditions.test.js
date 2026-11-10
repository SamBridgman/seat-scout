import test from 'node:test';
import assert from 'node:assert/strict';
import { conditions, loadReports, FRESH_MS } from '../src/data.js';
const now = 1800000000000;
const report = (overrides = {}) => ({ id: 'a', spaceId: 'mugar-4', seats: 'Many seats', atmosphere: 'Quiet', createdAt: now - 60000, ...overrides });
test('expired and future observations do not claim current conditions', () => {
  const result = conditions('mugar-4', [report({createdAt:now-FRESH_MS}), report({createdAt:now+1})], now);
  assert.deepEqual(result, {seats:'Unknown',atmosphere:'Unknown',count:0,newest:null});
});
test('reports aggregate seating and atmosphere independently and show disagreement', () => {
  const result = conditions('mugar-4', [report(), report({id:'b',seats:'Full',createdAt:now-1000}), report({spaceId:'stone',atmosphere:'Noisy'})], now);
  assert.equal(result.seats,'Mixed');
  assert.equal(result.atmosphere,'Quiet');
  assert.equal(result.count,2);
  assert.equal(result.newest,now-1000);
});
test('corrupt and unrecognized persisted observations are discarded', () => {
  assert.deepEqual(loadReports({getItem:()=>'{invalid'}),[]);
  assert.deepEqual(loadReports({getItem:()=>JSON.stringify([null, report({spaceId:'unknown'}),report({seats:'123'}),report({createdAt:Date.now()+60000})])}),[]);
});
test('valid local observations survive reload while samples are not persisted', () => {
  const valid = report({createdAt:Date.now()-1000});
  assert.deepEqual(loadReports({getItem:()=>JSON.stringify([valid,{...valid,sample:true}])}),[valid]);
});
