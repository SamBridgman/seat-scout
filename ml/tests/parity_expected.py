"""Print the reference model's estimates as JSON, for tests/sql_parity.mjs.

    python3 tests/parity_expected.py REPORTS_CSV TIME [TIME ...]
"""

import json
import sys
from datetime import datetime
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT))

from seatscout_model import estimate, load_areas, load_reports  # noqa: E402


def main() -> None:
    areas = load_areas(str(ROOT / "data" / "areas.csv"))
    reports = load_reports(sys.argv[1])
    out = {}
    for t in sys.argv[2:]:
        at = datetime.fromisoformat(t.replace("Z", "+00:00"))
        out[t] = [
            {
                "area_id": e.area_id, "dimension": e.dimension, "label": e.label,
                "probability": e.probability, "probabilities": e.probabilities, "basis": e.basis,
                "history_weight": e.history_weight, "history_reports": e.history_reports,
                "live_reports": e.live_reports,
            }
            for e in estimate(areas, reports, at)
        ]
    json.dump(out, sys.stdout)


if __name__ == "__main__":
    main()
