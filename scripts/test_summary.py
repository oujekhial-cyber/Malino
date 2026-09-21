#!/usr/bin/env python3
"""خلاصه‌سازی نتایج تست‌های واحد JUnit XML برای گزارش CI."""
import glob
import sys
import xml.etree.ElementTree as ET

def main() -> int:
    total = fail = err = skip = 0
    failures = []
    files = glob.glob("app/build/test-results/testDebugUnitTest/*.xml")
    if not files:
        print("(no test results)")
        return 0
    for f in files:
        s = ET.parse(f).getroot()
        total += int(s.get("tests", 0))
        fail += int(s.get("failures", 0))
        err += int(s.get("errors", 0))
        skip += int(s.get("skipped", 0))
        for c in s.iter("testcase"):
            for bad in list(c.iter("failure")) + list(c.iter("error")):
                msg = (bad.get("message") or "")[:200]
                failures.append(f"FAIL {c.get('classname')}.{c.get('name')}: {msg}")
    print(f"tests={total} failures={fail} errors={err} skipped={skip}")
    for line in failures[:50]:
        print(line)
    return 0

if __name__ == "__main__":
    sys.exit(main())
