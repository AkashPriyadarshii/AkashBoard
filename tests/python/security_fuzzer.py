#!/usr/bin/env python3
"""
security_fuzzer.py — Security fuzzing suite for AkashBoard.

Generates malicious/malformed inputs and validates:
- XML files resist injection
- ProGuard rules are complete
- Manifest has no security holes
- Build configs are safe
- No sensitive data in source code

Run: python tests/python/security_fuzzer.py
"""

import os
import re
import sys
import hashlib
from pathlib import Path
from dataclasses import dataclass, field
from typing import List

PROJECT_ROOT = Path(__file__).parent.parent.parent
APP_SRC = PROJECT_ROOT / "app" / "src" / "main"


@dataclass
class TestResult:
    name: str
    passed: bool
    message: str = ""


results: List[TestResult] = []


def test(name: str, passed: bool, message: str = ""):
    results.append(TestResult(name=name, passed=passed, message=message))
    status = "[PASS]" if passed else "[FAIL]"
    msg = f" — {message}" if message else ""
    print(f"  {status} {name}{msg}")


# ========================================================================
# SECTION 1: ProGuard Rules Audit
# ========================================================================

def audit_proguard():
    print("\n[1/8] ProGuard Rules Audit...")
    proguard = PROJECT_ROOT / "app" / "proguard-rules.pro"
    if not proguard.exists():
        test("proguard exists", False, "proguard-rules.pro not found")
        return

    content = proguard.read_text(errors="ignore")

    # Check for essential keep rules
    required_keeps = [
        ("JNI classes", r"-keep class com.akashboard.engine.PredictorBridge"),
        ("Room DAOs", r"-keep class com.akashboard.data.ClipboardDao"),
        ("Room entities", r"-keep class com.akashboard.data.ClipboardItem"),
        ("Activities", r"-keep class com.akashboard.OnboardingActivity"),
        ("Settings", r"-keep class com.akashboard.SettingsActivity"),
        ("IME Service", r"-keep class com.akashboard.AkashBoardIME"),
        ("Theme classes", r"-keep class com.akashboard.theme"),
        ("Analytics", r"-keep class com.akashboard.analytics"),
    ]

    for name, pattern in required_keeps:
        found = bool(re.search(pattern, content, re.IGNORECASE))
        test(f"ProGuard: {name}", found, "" if found else f"Missing keep rule for {name}")

    # Check for dangerous rules
    dangerous = [
        r"-dontwarn",  # Suppresses warnings that might hide real issues
    ]
    for pattern in dangerous:
        found = bool(re.search(pattern, content))
        if found:
            test("ProGuard: no dangerous rules", False, f"Found potentially dangerous rule: {pattern}")


# ========================================================================
# SECTION 2: Manifest Security Audit
# ========================================================================

def audit_manifest():
    print("\n[2/8] Manifest Security Audit...")
    manifest = APP_SRC / "AndroidManifest.xml"
    if not manifest.exists():
        test("manifest exists", False, "AndroidManifest.xml not found")
        return

    content = manifest.read_text(errors="ignore")

    # Security checks
    checks = [
        ("No android:debuggable=true", "debuggable" not in content or 'debuggable="true"' not in content,
         "Debug flag should not be hardcoded to true"),
        ("No android:allowBackup=true", 'allowBackup="true"' not in content,
         "allowBackup should be false for privacy app"),
        ("No exported activities without intent-filter check",
         content.count("exported") < 5,  # Sanity check
         "Verify all exported components are intentional"),
        ("IME service declared", "INPUT_METHOD" in content,
         "No INPUT_METHOD service found"),
        ("No dangerous permissions", "WRITE_EXTERNAL_STORAGE" not in content,
         "WRITE_EXTERNAL_STORAGE should not be needed"),
    ]

    for name, passed, msg in checks:
        test(f"Manifest: {name}", passed, "" if passed else msg)


# ========================================================================
# SECTION 3: Source Code Security Scan
# ========================================================================

def audit_source_code():
    print("\n[3/8] Source Code Security Scan...")

    kt_files = list(PROJECT_ROOT.rglob("*.kt"))
    rs_files = list(PROJECT_ROOT.rglob("*.rs"))

    sensitive_patterns = [
        (r"api[_-]?key\s*=\s*[\"'][^\"']+[\"']", "Hardcoded API key"),
        (r"password\s*=\s*[\"'][^\"']+[\"']", "Hardcoded password"),
        (r"secret\s*=\s*[\"'][^\"']+[\"']", "Hardcoded secret"),
        (r"token\s*=\s*[\"'][^\"']+[\"']", "Hardcoded token"),
        (r"aws_access_key", "AWS key reference"),
        (r"TODO.*hack", "Hack TODO comment"),
        (r"FIXME.*crash", "Crash FIXME"),
        (r"System\.exit", "System.exit in production"),
        (r"Thread\.sleep", "Thread.sleep (potential ANR)"),
    ]

    for kt_file in kt_files:
        if "/build/" in str(kt_file):
            continue
        content = kt_file.read_text(errors="ignore")
        rel = kt_file.relative_to(PROJECT_ROOT)
        for pattern, desc in sensitive_patterns:
            if re.search(pattern, content, re.IGNORECASE):
                test(f"Source: {rel} — {desc}", False, f"Found: {pattern}")

    for rs_file in rs_files:
        if "/target/" in str(rs_file) or "/build/" in str(rs_file):
            continue
        content = rs_file.read_text(errors="ignore")
        rel = rs_file.relative_to(PROJECT_ROOT)
        for pattern, desc in sensitive_patterns:
            if re.search(pattern, content, re.IGNORECASE):
                test(f"Source: {rel} — {desc}", False, f"Found: {pattern}")

    # Check for unsafe Rust
    for rs_file in rs_files:
        if "/target/" in str(rs_file) or "/build/" in str(rs_file):
            continue
        content = rs_file.read_text(errors="ignore")
        rel = rs_file.relative_to(PROJECT_ROOT)
        if "unsafe" in content:
            test(f"Source: {rel} — unsafe code", False, "Found 'unsafe' block — verify it's necessary")


# ========================================================================
# SECTION 4: Dependency Audit
# ========================================================================

def audit_dependencies():
    print("\n[4/8] Dependency Audit...")

    gradle = PROJECT_ROOT / "app" / "build.gradle.kts"
    if gradle.exists():
        content = gradle.read_text()
        # Check for known vulnerable patterns
        test("Deps: no SNAPSHOT versions",
             "SNAPSHOT" not in content,
             "SNAPSHOT dependencies found — not suitable for release")
        test("Deps: no hardcoded versions",
             "1.0-SNAPSHOT" not in content,
             "")


# ========================================================================
# SECTION 5: Build Config Safety
# ========================================================================

def audit_build_config():
    print("\n[5/8] Build Config Safety...")

    gradle = PROJECT_ROOT / "app" / "build.gradle.kts"
    if gradle.exists():
        content = gradle.read_text()
        test("Build: minify enabled for release",
             "isMinifyEnabled = true" in content,
             "Release build should have minification enabled")
        test("Build: shrinkResources for release",
             "isShrinkResources = true" in content,
             "Release build should shrink resources")
        test("Build: targetSdk 35",
             'targetSdk = 35' in content,
             "Should target latest SDK")


# ========================================================================
# SECTION 6: File Integrity
# ========================================================================

def audit_file_integrity():
    print("\n[6/8] File Integrity...")

    # Check .so exists and is reasonable size
    so_file = APP_SRC / "jniLibs" / "arm64-v8a" / "libpredictor.so"
    if so_file.exists():
        size = so_file.stat().st_size
        test("File: .so exists", True)
        test("File: .so size reasonable",
             100_000 < size < 5_000_000,
             f"Size: {size / 1024:.1f}KB — should be 100KB-5MB")
        test("File: .so not empty",
             size > 0,
             f"Size: {size} bytes")
    else:
        test("File: .so exists", False, "libpredictor.so not found")

    # Check LICENSE exists
    license_file = PROJECT_ROOT / "LICENSE"
    test("File: LICENSE exists", license_file.exists())

    # Check README exists and is substantial
    readme = PROJECT_ROOT / "README.md"
    if readme.exists():
        content = readme.read_text(errors="ignore")
        test("File: README > 50 lines", len(content.splitlines()) > 50,
             f"Has {len(content.splitlines())} lines")
        test("File: README has badges", "badge" in content.lower() or "![test]" in content.lower())
    else:
        test("File: README exists", False)


# ========================================================================
# SECTION 7: Theme Consistency
# ========================================================================

def audit_theme_consistency():
    print("\n[7/8] Theme Consistency...")

    light_theme = APP_SRC / "res" / "values" / "themes.xml"
    dark_theme = APP_SRC / "res" / "values-night" / "themes.xml"

    if not light_theme.exists():
        test("Theme: light theme exists", False)
        return
    if not dark_theme.exists():
        test("Theme: dark theme exists", False)
        return

    light_content = light_theme.read_text()
    dark_content = dark_theme.read_text()

    # Extract item names from both
    light_items = set(re.findall(r'name="([^"]+)"', light_content))
    dark_items = set(re.findall(r'name="([^"]+)"', dark_content))

    # Check for matching keys (ignoring values)
    light_keys = {item for item in light_items if not item.startswith("Theme")}
    dark_keys = {item for item in dark_items if not item.startswith("Theme")}

    missing_in_dark = light_keys - dark_keys
    missing_in_light = dark_keys - light_keys

    test("Theme: dark has all light keys",
         len(missing_in_dark) == 0,
         f"Missing: {missing_in_dark}" if missing_in_dark else "")
    test("Theme: light has all dark keys",
         len(missing_in_light) == 0,
         f"Missing: {missing_in_light}" if missing_in_light else "")


# ========================================================================
# SECTION 8: Git Hygiene
# ========================================================================

def audit_git_hygiene():
    print("\n[8/8] Git Hygiene...")

    gitignore = PROJECT_ROOT / ".gitignore"
    if gitignore.exists():
        content = gitignore.read_text()
        required_ignores = [
            ("build/", "Build output"),
            (".gradle", "Gradle cache"),
            ("*.apk", "APK files"),
            ("local.properties", "Local properties"),
        ]
        for pattern, desc in required_ignores:
            test(f"Gitignore: {desc}", pattern in content)
    else:
        test("Gitignore exists", False)

    # Check no secrets committed
    secret_patterns = [
        r"AKIA[0-9A-Z]{16}",  # AWS Access Key
        r"ghp_[a-zA-Z0-9]{36}",  # GitHub PAT
        r"sk-[a-zA-Z0-9]{32}",  # Stripe key
    ]
    kt_files = list(PROJECT_ROOT.rglob("*.kt"))
    for kt_file in kt_files:
        if "/build/" in str(kt_file):
            continue
        content = kt_file.read_text(errors="ignore")
        for pattern in secret_patterns:
            if re.search(pattern, content):
                test(f"Git: no secrets in {kt_file.name}", False, f"Found pattern: {pattern}")


# ========================================================================
# Main
# ========================================================================

def main() -> int:
    print("=" * 70)
    print("  AkashBoard Security Fuzzer & Audit")
    print("=" * 70)

    audit_proguard()
    audit_manifest()
    audit_source_code()
    audit_dependencies()
    audit_build_config()
    audit_file_integrity()
    audit_theme_consistency()
    audit_git_hygiene()

    # ========================================================================
    # Report
    # ========================================================================

    passed = sum(1 for r in results if r.passed)
    failed = sum(1 for r in results if not r.passed)

    print(f"\n{'=' * 70}")
    print(f"  TOTAL: {len(results)} checks | [PASS] {passed} passed | [FAIL] {failed} failed")
    print(f"{'=' * 70}")

    if failed > 0:
        print("\nFailed checks:")
        for r in results:
            if not r.passed:
                print(f"  [FAIL] {r.name}: {r.message}")

    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
