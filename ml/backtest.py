"""Backtest the Seat-Scout estimate model on past reports.

Each report in the test period is predicted from reports made strictly before
it, then compared with what the student reported. No future data is used.

    python3 backtest.py --reports data/reports_export.csv
    python3 backtest.py --reports data/synthetic_reports.csv   # pipeline check only

Export real reports from the Supabase SQL editor as CSV with:

    select e.area_id, e.seats, e.atmosphere, e.reported_at,
           case when e.reporter is not null then dense_rank() over (order by e.reporter) end as reporter
    from report_events e
    order by e.reported_at, e.id;
"""

from __future__ import annotations

import argparse
import csv
import math
from collections import Counter, defaultdict
from dataclasses import replace
from pathlib import Path

from seatscout_model import LABELS, Params, estimate, load_areas, load_reports

EPS = 1e-9


def run(areas, reports, test_share: float, params: Params):
    split = int(len(reports) * (1 - test_share))
    test = reports[split:]
    if not test or split == 0:
        raise SystemExit("need reports both before and inside the test period")
    pattern_only = replace(params, live_weight=0.0)

    stats = defaultdict(lambda: defaultdict(float))
    for i, r in enumerate(test, start=split):
        before = reports[:i]
        full = {e.dimension: e for e in estimate(areas, before, r.reported_at, params, [r.area_id])}
        patt = {e.dimension: e for e in estimate(areas, before, r.reported_at, pattern_only, [r.area_id])}
        last_in_room = next((p for p in reversed(before) if p.area_id == r.area_id), None)
        for dim, labels in LABELS.items():
            truth = r.label(dim)
            s = stats[dim]
            s["n"] += 1
            majority = Counter(p.label(dim) for p in before).most_common(1)[0][0]
            persistence = last_in_room.label(dim) if last_in_room else majority

            for name, est in (("model", full[dim]), ("pattern", patt[dim])):
                s[f"{name}_acc"] += est.label == truth
                s[f"{name}_ll"] += -math.log(max(est.probabilities[truth], EPS))
            s["uniform_ll"] += math.log(len(labels))
            s["majority_acc"] += majority == truth
            s["persistence_acc"] += persistence == truth

            basis = full[dim].basis
            s[f"basis_{basis}"] += 1
            s[f"basis_{basis}_acc"] += full[dim].label == truth
    return len(test), stats


def report(n_test: int, n_total: int, stats) -> None:
    print(f"Test reports: {n_test} of {n_total} (predicted from earlier reports only)\n")
    for dim in LABELS:
        s = stats[dim]
        n = s["n"]
        print(f"== {dim}")
        print(f"  {'method':<28}{'accuracy':>10}{'log loss':>10}")
        print(f"  {'model (pattern + live)':<28}{s['model_acc'] / n:>10.3f}{s['model_ll'] / n:>10.3f}")
        print(f"  {'pattern only':<28}{s['pattern_acc'] / n:>10.3f}{s['pattern_ll'] / n:>10.3f}")
        print(f"  {'baseline: last room report':<28}{s['persistence_acc'] / n:>10.3f}{'-':>10}")
        print(f"  {'baseline: global majority':<28}{s['majority_acc'] / n:>10.3f}{'-':>10}")
        print(f"  {'baseline: uniform':<28}{'-':>10}{s['uniform_ll'] / n:>10.3f}")
        shown = s["basis_live"] + s["basis_pattern"]
        print(f"  coverage (estimate shown, not 'insufficient'): {shown / n:.1%}")
        for basis in ("live", "pattern", "insufficient"):
            k = s[f"basis_{basis}"]
            acc = f"{s[f'basis_{basis}_acc'] / k:.3f}" if k else "-"
            print(f"    basis={basis:<13} share {k / n:>6.1%}   accuracy {acc}")
        print()


def main() -> None:
    here = Path(__file__).resolve().parent
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--areas", default=str(here / "data" / "areas.csv"))
    ap.add_argument("--reports", required=True)
    ap.add_argument("--test-share", type=float, default=0.2, help="latest share of reports to test on")
    args = ap.parse_args()

    areas = load_areas(args.areas)
    reports = load_reports(args.reports)
    with open(args.reports, newline="", encoding="utf-8") as f:
        if any(row.get("source") == "synthetic" for row in csv.DictReader(f)):
            print("NOTE: synthetic data. These numbers check the pipeline, not real BU conditions.\n")
    n_test, stats = run(areas, reports, args.test_share, Params())
    report(n_test, len(reports), stats)


if __name__ == "__main__":
    main()
