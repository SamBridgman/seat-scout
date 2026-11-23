"""Seat-Scout condition estimates, reference implementation of plan A.

This module mirrors public.estimate_conditions() in
sql/002_estimate_conditions.sql. The SQL function serves the app. This module
is used for offline backtests and for checking the SQL (tests/sql_parity.mjs).
Change both together.

Standard library only, Python 3.9 or later.
"""

from __future__ import annotations

import math
from collections import defaultdict
from dataclasses import dataclass, field
from datetime import datetime
from typing import Dict, Iterable, List, Optional, Sequence
from zoneinfo import ZoneInfo

LABELS: Dict[str, Sequence[str]] = {
    "seats": ("MANY", "FEW", "FULL"),
    "atmosphere": ("QUIET", "MODERATE", "NOISY"),
}
DIMENSIONS = ("atmosphere", "seats")  # SQL output order


@dataclass(frozen=True)
class Params:
    tz: str = "America/New_York"
    hour_sigma: float = 1.0
    hour_window: float = 3.0
    history_half_life_days: float = 28.0
    live_window_min: float = 60.0
    live_half_life_min: float = 15.0
    live_weight: float = 4.0
    zone_strength: float = 4.0
    area_strength: float = 4.0
    pattern_strength: float = 2.0
    min_pattern_weight: float = 3.0
    min_live_weight: float = 1.0


@dataclass(frozen=True)
class Area:
    area_id: int
    zone: str


@dataclass(frozen=True)
class Report:
    area_id: int
    seats: str
    atmosphere: str
    reported_at: datetime  # timezone-aware

    def label(self, dimension: str) -> str:
        return self.seats if dimension == "seats" else self.atmosphere


@dataclass(frozen=True)
class Estimate:
    area_id: int
    dimension: str
    label: str
    probability: float
    probabilities: Dict[str, float] = field(compare=False)
    basis: str  # 'live', 'pattern' or 'insufficient'
    history_weight: float
    history_reports: int
    live_reports: int


def _local_features(ts: datetime, tz: ZoneInfo):
    local = ts.astimezone(tz)
    weekend = local.isoweekday() in (6, 7)
    hour = local.hour + local.minute / 60.0
    return weekend, hour


def estimate(
    areas: Sequence[Area],
    reports: Iterable[Report],
    at: datetime,
    params: Params = Params(),
    only_areas: Optional[Iterable[int]] = None,
) -> List[Estimate]:
    """Estimate conditions at time `at` using only reports made at or before it."""
    if at.tzinfo is None:
        raise ValueError("at must be timezone-aware")
    tz = ZoneInfo(params.tz)
    t_weekend, t_hour = _local_features(at, tz)
    zone_of = {a.area_id: a.zone for a in areas}

    g = defaultdict(float)          # (dim, label)
    z = defaultdict(float)          # (zone, dim, label)
    a_w = defaultdict(float)        # (area, dim, label)
    a_n = defaultdict(int)
    l_w = defaultdict(float)
    l_n = defaultdict(int)

    for r in reports:
        if r.area_id not in zone_of or r.reported_at > at:
            continue
        age_s = (at - r.reported_at).total_seconds()
        if age_s < params.live_window_min * 60.0:
            w = params.live_weight * 0.5 ** (age_s / 60.0 / params.live_half_life_min)
            for dim in LABELS:
                key = (r.area_id, dim, r.label(dim))
                l_w[key] += w
                l_n[key] += 1
            continue
        weekend, hour = _local_features(r.reported_at, tz)
        if weekend != t_weekend:
            continue
        hd = min(abs(hour - t_hour), 24.0 - abs(hour - t_hour))
        if hd > params.hour_window:
            continue
        w = math.exp(-(hd * hd) / (2.0 * params.hour_sigma ** 2)) * 0.5 ** (
            age_s / 86400.0 / params.history_half_life_days
        )
        zone = zone_of[r.area_id]
        for dim in LABELS:
            lab = r.label(dim)
            g[(dim, lab)] += w
            z[(zone, dim, lab)] += w
            a_w[(r.area_id, dim, lab)] += w
            a_n[(r.area_id, dim, lab)] += 1

    wanted = set(only_areas) if only_areas is not None else None
    out: List[Estimate] = []
    for area in sorted(areas, key=lambda a: a.area_id):
        if wanted is not None and area.area_id not in wanted:
            continue
        for dim in DIMENSIONS:
            labels = LABELS[dim]
            k = len(labels)
            g_tot = sum(g[(dim, lab)] for lab in labels)
            z_tot = sum(z[(area.zone, dim, lab)] for lab in labels)
            a_tot = sum(a_w[(area.area_id, dim, lab)] for lab in labels)
            l_tot = sum(l_w[(area.area_id, dim, lab)] for lab in labels)
            post = {}
            for lab in labels:
                pg = (g[(dim, lab)] + 1.0) / (g_tot + k)
                pz = (z[(area.zone, dim, lab)] + params.zone_strength * pg) / (z_tot + params.zone_strength)
                pa = (a_w[(area.area_id, dim, lab)] + params.area_strength * pz) / (a_tot + params.area_strength)
                post[lab] = (l_w[(area.area_id, dim, lab)] + params.pattern_strength * pa) / (
                    l_tot + params.pattern_strength
                )
            best = max(labels, key=lambda lab: (post[lab], -labels.index(lab)))
            if l_tot >= params.min_live_weight:
                basis = "live"
            elif a_tot >= params.min_pattern_weight:
                basis = "pattern"
            else:
                basis = "insufficient"
            out.append(
                Estimate(
                    area_id=area.area_id,
                    dimension=dim,
                    label=best,
                    probability=post[best],
                    probabilities={lab: round(post[lab], 4) for lab in labels},
                    basis=basis,
                    history_weight=a_tot,
                    history_reports=sum(a_n[(area.area_id, dim, lab)] for lab in labels),
                    live_reports=sum(l_n[(area.area_id, dim, lab)] for lab in labels),
                )
            )
    return out


def load_areas(path: str) -> List[Area]:
    import csv

    with open(path, newline="", encoding="utf-8") as f:
        return [Area(int(r["area_id"]), r["zone"]) for r in csv.DictReader(f)]


def load_reports(path: str) -> List[Report]:
    """Read a CSV with area_id, seats, atmosphere, reported_at (ISO 8601 with offset)."""
    import csv

    out = []
    with open(path, newline="", encoding="utf-8") as f:
        for r in csv.DictReader(f):
            ts = datetime.fromisoformat(r["reported_at"].replace("Z", "+00:00"))
            if ts.tzinfo is None:
                raise ValueError(f"reported_at needs a UTC offset: {r['reported_at']}")
            out.append(Report(int(r["area_id"]), r["seats"].upper(), r["atmosphere"].upper(), ts))
    out.sort(key=lambda r: r.reported_at)
    return out


__all__ = ["Area", "Estimate", "LABELS", "Params", "Report", "estimate", "load_areas", "load_reports"]
