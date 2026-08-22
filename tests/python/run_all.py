#!/usr/bin/env python3
"""
run_all.py — Master test runner for AkashBoard Python validation suite.

Runs all validation scripts and aggregates results.

Usage:
    python tests/python/run_all.py
    python tests/python/run_all.py --xml-only
    python tests/python/run_all.py --security-only
"""

import sys
import subprocess
import io
from pathlib import Path

# Fix Windows console encoding
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

TESTS_DIR = Path(__file__).parent


def run_test(script: str, label: str) -> int:
    """Run a test script and return exit code."""
    print(f"\n{'-' * 70}")
    print(f"  RUNNING: {label}")
    print(f"{'-' * 70}")

    result = subprocess.run(
        [sys.executable, str(TESTS_DIR / script)],
        capture_output=False,
        text=True
    )
    return result.returncode


def main() -> int:
    print("=" * 70)
    print("  AkashBoard Python Test Suite — Master Runner")
    print("=" * 70)

    # Parse args
    xml_only = "--xml-only" in sys.argv
    security_only = "--security-only" in sys.argv

    scripts = []
    if not security_only:
        scripts.append(("xml_validator.py", "XML Validation"))
        scripts.append(("build_validator.py", "Build Validation"))
        scripts.append(("jni_contract_test.py", "JNI Contract Test"))
    if not xml_only:
        scripts.append(("security_fuzzer.py", "Security Fuzzer & Audit"))

    total_errors = 0
    total_scripts = 0

    for script, label in scripts:
        code = run_test(script, label)
        total_scripts += 1
        if code != 0:
            total_errors += 1

    # Final report
    print(f"\n{'-' * 70}")
    print(f"  MASTER RESULTS")
    print(f"{'-' * 70}")
    print(f"  Scripts run: {total_scripts}")
    print(f"  Passed: {total_scripts - total_errors}")
    print(f"  Failed: {total_errors}")
    print(f"{'-' * 70}")

    if total_errors == 0:
        print("  [PASS] ALL PYTHON VALIDATION SUITES PASSED")
    else:
        print(f"  [FAIL] {total_errors} SUITE(S) FAILED")

    return 0 if total_errors == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
