"""Generate SYNTHETIC Seat-Scout reports for testing the pipeline.

The output is invented data with a plausible daily rhythm. Use it to exercise
the model, the SQL and the backtest. Never present results on it as evidence
about real BU study spaces. Every row has source=synthetic.

    python3 synthetic.py --weeks 6 --per-day 40 --out data/synthetic_reports.csv
"""

from __future__ import annotations

import argparse
import csv
import math
import random
from datetime import datetime, timedelta, timezone
from pathlib import Path
from zoneinfo import ZoneInfo

from seatscout_model import load_areas

TZ = ZoneInfo("America/New_York")
ZONE_NOISE = {"Silent": 0.10, "Quiet": 0.20, "Considerate": 0.35, "Collaborative": 0.60}


def busyness(hour: float, weekend: bool) -> float:
    """Share of seats taken at a local hour, 0 to 1."""
    if weekend:
        peak, width, height = 15.5, 3.5, 0.55
    else:
        peak, width, height = 15.0, 3.0, 0.85
    return 0.12 + height * math.exp(-((hour - peak) ** 2) / (2 * width * width))


def generate(areas, weeks: int, per_day: int, end: datetime, seed: int, flip: float):
    rng = random.Random(seed)
    popularity = {a.area_id: rng.uniform(0.6, 1.25) for a in areas}
    rows = []
    start_day = (end.astimezone(TZ) - timedelta(weeks=weeks)).date()
    for day in range(weeks * 7):
        date = start_day + timedelta(days=day)
        weekend = date.isoweekday() in (6, 7)
        count = per_day // 2 if weekend else per_day
        for _ in range(count):
            # More people around means more reports, so sample hours by busyness.
            while True:
                hour = rng.uniform(8.0, 23.5)
                if rng.random() < busyness(hour, weekend):
                    break
            area = rng.choices(areas, weights=[popularity[a.area_id] for a in areas])[0]
            occ = min(1.0, max(0.0, busyness(hour, weekend) * popularity[area.area_id] + rng.gauss(0, 0.1)))
            seats = "MANY" if occ < 0.45 else "FEW" if occ < 0.8 else "FULL"
            noise = ZONE_NOISE.get(area.zone, 0.45) + 0.3 * occ + rng.gauss(0, 0.1)
            atmosphere = "QUIET" if noise < 0.35 else "MODERATE" if noise < 0.65 else "NOISY"
            if rng.random() < flip:
                seats = rng.choice(["MANY", "FEW", "FULL"])
            if rng.random() < flip:
                atmosphere = rng.choice(["QUIET", "MODERATE", "NOISY"])
            local = datetime(date.year, date.month, date.day, tzinfo=TZ) + timedelta(hours=hour)
            ts = local.astimezone(timezone.utc).replace(microsecond=0)
            if ts <= end:
                rows.append((ts, area, seats, atmosphere))
    rows.sort(key=lambda r: r[0])
    return rows


def main() -> None:
    here = Path(__file__).resolve().parent
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--areas", default=str(here / "data" / "areas.csv"))
    ap.add_argument("--weeks", type=int, default=6)
    ap.add_argument("--per-day", type=int, default=40, help="reports per weekday; weekends get half")
    ap.add_argument("--end", default="2026-11-20T23:59:00-05:00", help="last possible report time, ISO 8601")
    ap.add_argument("--flip", type=float, default=0.1, help="chance a reporter picks a random value")
    ap.add_argument("--seed", type=int, default=7)
    ap.add_argument("--out", default=str(here / "data" / "synthetic_reports.csv"))
    args = ap.parse_args()

    areas = load_areas(args.areas)
    end = datetime.fromisoformat(args.end)
    rows = generate(areas, args.weeks, args.per_day, end, args.seed, args.flip)
    with open(args.out, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["area_id", "zone", "seats", "atmosphere", "reported_at", "source"])
        for ts, area, seats, atmosphere in rows:
            w.writerow([area.area_id, area.zone, seats, atmosphere,
                        ts.isoformat().replace("+00:00", "Z"), "synthetic"])
    print(f"wrote {len(rows)} synthetic reports to {args.out}")


if __name__ == "__main__":
    main()
