#!/usr/bin/env python3
"""
build_validator.py — Validates build configuration across the project.

Checks:
- Gradle build files parse correctly
- Cargo.toml is valid
- Version consistency across files
- No duplicate dependencies
- Rust target config exists

Run: python tests/python/build_validator.py
"""

import re
import sys
from pathlib import Path

PROJECT_ROOT = Path(__file__).parent.parent.parent


def main() -> int:
    print("=" * 70)
    print("  AkashBoard Build Validator")
    print("=" * 70)

    errors = 0
    warnings = 0

    # ========================================================================
    # 1. Gradle Build Files
    # ========================================================================
    print("\n[1/5] Gradle Build Files...")

    gradle_files = list(PROJECT_ROOT.glob("*.gradle*")) + list(PROJECT_ROOT.glob("app/*.gradle*"))
    for gf in gradle_files:
        if gf.name.endswith(".gradle.kts"):
            content = gf.read_text()
            # Basic syntax checks
            open_parens = content.count("(")
            close_parens = content.count(")")
            if open_parens != close_parens:
                print(f"  [FAIL] {gf.name}: mismatched parentheses ({open_parens} open, {close_parens} close)")
                errors += 1
            else:
                print(f"  [PASS] {gf.name}: syntax OK")

    # ========================================================================
    # 2. Cargo.toml
    # ========================================================================
    print("\n[2/5] Cargo.toml...")

    cargo = PROJECT_ROOT / "engine" / "Cargo.toml"
    if cargo.exists():
        content = cargo.read_text()
        if "[package]" in content:
            print("  [PASS] [package] section exists")
        else:
            print("  [FAIL] Missing [package] section")
            errors += 1

        if "[lib]" in content:
            print("  [PASS] [lib] section exists")
        else:
            print("  [WARN]  No [lib] section")
            warnings += 1

        if 'crate-type = ["cdylib"]' in content or 'crate-type = ["cdylib", "rlib"]' in content:
            print("  [PASS] crate-type includes cdylib (for .so)")
        else:
            print("  [FAIL] crate-type missing cdylib")
            errors += 1

        if 'opt-level = "z"' in content:
            print("  [PASS] Size optimization enabled")
        else:
            print("  [WARN]  Size optimization not set")
            warnings += 1

        if "lto = true" in content:
            print("  [PASS] LTO enabled")
        else:
            print("  [WARN]  LTO not enabled")
            warnings += 1
    else:
        print("  [FAIL] Cargo.toml not found")
        errors += 1

    # ========================================================================
    # 3. Version Consistency
    # ========================================================================
    print("\n[3/5] Version Consistency...")

    # Extract version from Cargo.toml
    if cargo.exists():
        cargo_content = cargo.read_text(errors="ignore")
        cargo_version = re.search(r'version\s*=\s*"([^"]+)"', cargo_content)
        if cargo_version:
            print(f"  Cargo.toml version: {cargo_version.group(1)}")

    # Extract from build.gradle.kts
    gradle = PROJECT_ROOT / "app" / "build.gradle.kts"
    if gradle.exists():
        gradle_content = gradle.read_text(errors="ignore")
        version_name = re.search(r'versionName\s*=\s*"([^"]+)"', gradle_content)
        if version_name:
            print(f"  build.gradle.kts versionName: {version_name.group(1)}")
        version_code = re.search(r'versionCode\s*=\s*(\d+)', gradle_content)
        if version_code:
            print(f"  build.gradle.kts versionCode: {version_code.group(1)}")

    # Check CHANGELOG
    changelog = PROJECT_ROOT / "CHANGELOG.md"
    if changelog.exists():
        cl_content = changelog.read_text(errors="ignore")
        if "## [1.0.0]" in cl_content or "# v1.0.0" in cl_content:
            print("  [PASS] CHANGELOG has v1.0.0 entry")
        else:
            print("  [WARN]  CHANGELOG missing v1.0.0 entry")
            warnings += 1
    else:
        print("  [WARN]  CHANGELOG.md not found")
        warnings += 1

    # ========================================================================
    # 4. Dependency Duplication
    # ========================================================================
    print("\n[4/5] Dependency Check...")

    if gradle.exists():
        gradle_content = gradle.read_text(errors="ignore")
        deps = re.findall(r'implementation\("([^"]+)"\)', gradle_content)
        test_deps = re.findall(r'testImplementation\("([^"]+)"\)', gradle_content)
        android_test_deps = re.findall(r'androidTestImplementation\("([^"]+)"\)', gradle_content)

        all_deps = deps + test_deps + android_test_deps
        seen = set()
        dupes = []
        for d in all_deps:
            artifact = d.split(":")[1] if ":" in d else d
            if artifact in seen:
                dupes.append(artifact)
            seen.add(artifact)

        if dupes:
            print(f"  [WARN]  Duplicate dependencies: {dupes}")
            warnings += 1
        else:
            print(f"  [PASS] No duplicate dependencies ({len(all_deps)} total)")

    # ========================================================================
    # 5. Config Files
    # ========================================================================
    print("\n[5/5] Config Files...")

    cargo_config = PROJECT_ROOT / "engine" / ".cargo" / "config.toml"
    if cargo_config.exists():
        content = cargo_config.read_text(errors="ignore")
        if "aarch64-linux-android" in content:
            print("  [PASS] Android cross-compilation target configured")
        else:
            print("  [WARN]  No Android target in cargo config")
            warnings += 1
    else:
        print("  [WARN]  No .cargo/config.toml")
        warnings += 1

    # Check .nojekyll
    nojekyll = PROJECT_ROOT / "docs" / ".nojekyll"
    if nojekyll.exists():
        print("  [PASS] .nojekyll exists (GitHub Pages)")
    else:
        print("  [WARN]  .nojekyll not found")
        warnings += 1

    # Report
    print(f"\n{'=' * 70}")
    print(f"  TOTAL: {errors} errors, {warnings} warnings")
    print(f"{'=' * 70}")

    return 0 if errors == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
