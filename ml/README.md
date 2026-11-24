# Seat-Scout condition estimates (ML, plan A)

Most rooms have no fresh report most of the time, so the app shows "unknown". This module estimates seating and atmosphere for every room from the history of student reports, so a room can show "usually busy around now (based on 12 reports)" when no fresh report exists.

It also gives the app a shared view of conditions. Under the current RLS policies each student can read only their own rows in `reports`. `estimate_conditions()` returns aggregates over every student's reports without exposing who reported what.

The model follows the structure Google Maps uses for popular times: a pattern learned from past visits with recent data weighted more, a separate live layer, and no estimate when data is too thin.

## How the model works

For a room at a target time, `estimate_conditions()` combines three layers.

1. **History.** Reports older than 60 minutes on the same day type (weekday or weekend) are counted. Each report is weighted by how close its Boston local time is to the target time (Gaussian, sigma 1 hour, cut off at 3 hours) and by its age (half-life 28 days).
2. **Hierarchical smoothing (Bayesian shrinkage).** Campus-wide counts with a uniform prior give a global distribution. Zone counts (Silent, Collaborative, and so on) are shrunk toward it, and room counts are shrunk toward the zone, each with a prior strength of 4 reports. A room with little data therefore borrows from similar rooms instead of guessing.
3. **Live reports.** Reports from the past 60 minutes are added on top with weight `4 × 0.5^(age / 15 min)`.

For each room, a student's latest live report and strongest matching historical report contribute to their respective layers.

Each room gets one row per dimension (`seats`, `atmosphere`) with the most likely value, all probabilities and a `basis`:

| basis | meaning | how the app should show it |
| --- | --- | --- |
| `live` | a report within about 30 minutes | as current conditions |
| `pattern` | at least 3 effective past reports near this time | "Usually …" with the report count, styled differently from live reports |
| `insufficient` | not enough data | do not show a condition |

The parameters are at the top of [sql/002_estimate_conditions.sql](sql/002_estimate_conditions.sql) and in `Params` in [seatscout_model.py](seatscout_model.py). They are reasoned defaults, not tuned values. Tune them only on real reports.

## Files

| Path | Purpose |
| --- | --- |
| [sql/001_report_history.sql](sql/001_report_history.sql) | Append-only `report_events` table and a trigger that copies every write to `reports` into it |
| [sql/002_estimate_conditions.sql](sql/002_estimate_conditions.sql) | The model as a Postgres function, callable by the app |
| [seatscout_model.py](seatscout_model.py) | The same model in Python for backtests |
| [backtest.py](backtest.py) | Predicts each test report from earlier reports only and compares with baselines |
| [synthetic.py](synthetic.py) | Generates invented reports for testing the pipeline |
| [data/areas.csv](data/areas.csv) | `study_area` IDs and zones, from the Supabase project on 2026-11-22 |
| [tests/](tests/) | Python unit tests and the SQL check against the Python model |

## Apply to Supabase

In the Supabase dashboard, open SQL Editor and run `sql/001_report_history.sql`, then `sql/002_estimate_conditions.sql`. Run them after [android/supabase/reports.sql](../android/supabase/reports.sql). Both can be run again safely.

The app's write path does not change. `reports` keeps one current row per user and area, and an upsert replaces it. The trigger copies each insert or update into `report_events` with the server time, so replaced reports stay in the history. A report for an unknown area is skipped with a warning, and the student's write still succeeds.

Raw history includes reporter IDs, so clients cannot read `report_events`. They can only call the function, which returns aggregates. Deleting an account also deletes its report history.

## Call from the app

```bash
curl -X POST "$SUPABASE_URL/rest/v1/rpc/estimate_conditions" \
  -H "apikey: $SUPABASE_PUBLISHABLE_KEY" -H "Content-Type: application/json" -d '{}'
```

Pass `{"at_time": "2026-11-18T20:00:00Z"}` to estimate another time. `area_id` in the result is `study_area.id` as text, the same value as `StudyArea.id` in the app. Anonymous and signed-in clients can both call it. The Android app does not call it yet. A natural place is a flow beside `ReportRepository.reports`, refreshed every few minutes.

## Run the checks

```bash
cd ml
python3 -m unittest discover -s tests
npm install && node tests/sql_parity.mjs
```

The SQL check runs `android/supabase/reports.sql` and both migrations in an in-memory Postgres (PGlite), with stand-ins for Supabase auth. It runs the app's exact upsert as an authenticated user under RLS and checks that replaced reports stay in the history. It then confirms the function matches the Python model on 432 estimates and checks client permissions.

## Backtest

Export real history from the SQL editor as CSV and run the backtest on it. The export uses a temporary numeric key per reporter so the backtest can group reports without exporting account IDs.

```sql
select e.area_id, e.seats, e.atmosphere, e.reported_at,
       case when e.reporter is not null then dense_rank() over (order by e.reporter) end as reporter
from report_events e
order by e.reported_at, e.id;
```

```bash
python3 backtest.py --reports data/reports_export.csv
```

It reports accuracy and log loss for the full model and for the pattern only. It compares them with three baselines: the last report in the same room, the campus-wide majority value and a uniform guess. It also shows coverage, the share of predictions that are not `insufficient`.

`python3 synthetic.py` writes invented reports to `data/synthetic_reports.csv`. On them the model reaches about 0.65 accuracy for seats and 0.69 for atmosphere, against 0.43 and 0.54 for the best baseline. This only shows that the pipeline runs end to end. The generator invents the daily rhythm the model looks for, and it has no short-term correlation between consecutive reports, so it cannot show what live reports add. Do not quote synthetic numbers as results about BU.

## Next steps

1. Run the migrations and collect seed reports in person at different times of day.
2. Show `pattern` estimates in the room details and the Quiet now filter, visually distinct from live reports.
3. Once a few hundred real reports exist, backtest, tune the parameters and add features such as exam weeks.
4. With many overlapping reports, weight reporters by reliability, for example with Dawid-Skene from Crowd-Kit.
