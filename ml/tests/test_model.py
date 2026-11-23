"""Unit tests for the reference model.  Run: python3 -m unittest discover -s tests"""

import sys
import unittest
from datetime import datetime, timedelta, timezone
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from seatscout_model import Area, Report, estimate  # noqa: E402

UTC = timezone.utc
AREAS = [Area(1, "Silent"), Area(9, "Silent"), Area(2, "Collaborative")]
# Wednesday 2026-11-18 15:00 in Boston (EST, UTC-5).
WED_3PM = datetime(2026, 11, 18, 20, 0, tzinfo=UTC)


def pick(estimates, area_id, dimension):
    return next(e for e in estimates if e.area_id == area_id and e.dimension == dimension)


def weekday_afternoons(area_id, seats, weeks, hour_utc=20):
    """One report per weekday at the same local time for the given number of weeks."""
    out = []
    for d in range(1, weeks * 7 + 1):
        ts = WED_3PM.replace(hour=hour_utc) - timedelta(days=d)
        if ts.isoweekday() <= 5:
            out.append(Report(area_id, seats, "QUIET", ts))
    return out


class ModelTest(unittest.TestCase):
    def test_no_reports_is_uniform_and_insufficient(self):
        for e in estimate(AREAS, [], WED_3PM):
            self.assertEqual(e.basis, "insufficient")
            self.assertAlmostEqual(e.probability, 1 / 3)
            self.assertAlmostEqual(sum(e.probabilities.values()), 1.0, places=3)

    def test_output_covers_every_area_and_dimension(self):
        est = estimate(AREAS, [], WED_3PM)
        self.assertEqual({(e.area_id, e.dimension) for e in est},
                         {(a.area_id, d) for a in AREAS for d in ("seats", "atmosphere")})

    def test_fresh_report_is_live_and_dominates(self):
        est = estimate(AREAS, [Report(1, "FULL", "NOISY", WED_3PM - timedelta(minutes=5))], WED_3PM)
        seats = pick(est, 1, "seats")
        self.assertEqual((seats.basis, seats.label), ("live", "FULL"))
        self.assertGreater(seats.probability, 0.6)
        self.assertEqual(pick(est, 1, "atmosphere").label, "NOISY")
        self.assertEqual(pick(est, 9, "seats").basis, "insufficient")

    def test_live_weight_fades_after_about_30_minutes(self):
        r = [Report(1, "FULL", "NOISY", WED_3PM - timedelta(minutes=45))]
        self.assertNotEqual(pick(estimate(AREAS, r, WED_3PM), 1, "seats").basis, "live")

    def test_history_pattern_on_matching_day_type(self):
        reports = weekday_afternoons(1, "FULL", weeks=3)
        seats = pick(estimate(AREAS, reports, WED_3PM), 1, "seats")
        self.assertEqual((seats.basis, seats.label), ("pattern", "FULL"))
        saturday = WED_3PM + timedelta(days=3)
        self.assertEqual(pick(estimate(AREAS, reports, saturday), 1, "seats").basis, "insufficient")

    def test_history_far_from_target_hour_is_ignored(self):
        reports = weekday_afternoons(1, "FULL", weeks=3)
        morning = WED_3PM - timedelta(hours=6)  # 09:00 local
        self.assertEqual(pick(estimate(AREAS, reports, morning), 1, "seats").history_reports, 0)

    def test_zone_mate_history_shrinks_toward_zone(self):
        reports = weekday_afternoons(1, "FULL", weeks=3)
        est = estimate(AREAS, reports, WED_3PM)
        same_zone, other_zone = pick(est, 9, "seats"), pick(est, 2, "seats")
        self.assertEqual(same_zone.basis, "insufficient")
        self.assertGreater(same_zone.probabilities["FULL"], other_zone.probabilities["FULL"])

    def test_future_reports_are_ignored(self):
        later = [Report(1, "FULL", "NOISY", WED_3PM + timedelta(minutes=1))]
        self.assertEqual(estimate(AREAS, later, WED_3PM), estimate(AREAS, [], WED_3PM))

    def test_local_time_uses_boston_timezone(self):
        # 20:00 UTC is 15:00 in Boston in November. A 15:00 UTC report is 10:00 local.
        utc_afternoon = weekday_afternoons(1, "FULL", weeks=3, hour_utc=15)
        self.assertEqual(pick(estimate(AREAS, utc_afternoon, WED_3PM), 1, "seats").history_reports, 0)

    def test_naive_time_is_rejected(self):
        with self.assertRaises(ValueError):
            estimate(AREAS, [], datetime(2026, 11, 18, 15, 0))


if __name__ == "__main__":
    unittest.main()
