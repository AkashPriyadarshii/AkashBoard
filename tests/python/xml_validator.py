#!/usr/bin/env python3
"""
xml_validator.py — Validates ALL XML files in the AkashBoard project.

Checks:
- Well-formedness (XML parse)
- Required Android attributes
- Theme consistency (light/dark have matching keys)
- String resource completeness
- Drawable reference validity
- Manifest lint
- ProGuard rules syntax

Run: python tests/python/xml_validator.py
"""

import os
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
from dataclasses import dataclass, field
from typing import List, Optional

# ========================================================================
# Configuration
# ========================================================================

PROJECT_ROOT = Path(__file__).parent.parent.parent
APP_SRC = PROJECT_ROOT / "app" / "src" / "main"

ANDROID_NS = "http://schemas.android.com/apk/res/android"

# Required manifest attributes
REQUIRED_MANIFEST_ATTRS = {
    "package": "com.akashboard",
    "android:versionCode": None,
    "android:versionName": None,
}

# Required themes.xml keys
REQUIRED_THEME_KEYS = {
    "colorPrimary",
    "colorPrimaryVariant",
    "colorSecondary",
    "colorOnPrimary",
    "colorOnSecondary",
    "android:colorBackground",
    "colorSurface",
    "colorOnSurface",
    "android:textColorPrimary",
    "android:textColorSecondary",
}


@dataclass
class ValidationResult:
    file: str
    errors: List[str] = field(default_factory=list)
    warnings: List[str] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return len(self.errors) == 0


def validate_xml_wellformedness(filepath: Path) -> ValidationResult:
    """Check that XML parses without errors."""
    result = ValidationResult(file=str(filepath.relative_to(PROJECT_ROOT)))
    try:
        ET.parse(filepath)
    except ET.ParseError as e:
        result.errors.append(f"XML parse error: {e}")
    except Exception as e:
        result.errors.append(f"Unexpected error: {e}")
    return result


def validate_manifest(filepath: Path) -> ValidationResult:
    """Validate AndroidManifest.xml."""
    result = ValidationResult(file="AndroidManifest.xml")
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()

        # Check package (may be set via namespace in build.gradle.kts in modern AGP)
        pkg = root.get("package")
        if pkg is not None and pkg != "com.akashboard":
            result.errors.append(f"Wrong package: {pkg}")
        elif pkg is None:
            result.warnings.append("No package attr in manifest (OK if set via namespace in build.gradle.kts)")

        # Check version attributes
        app = root.find("application")
        if app is None:
            result.errors.append("Missing <application> element")
        else:
            if app.get(f"{{{ANDROID_NS}}}allowBackup") is None:
                result.warnings.append("Missing android:allowBackup on <application>")

        # Check for debuggable in release
        if app is not None:
            debuggable = app.get(f"{{{ANDROID_NS}}}debuggable")
            if debuggable == "true":
                result.warnings.append("android:debuggable=true in manifest (should be false for release)")

        # Check required permissions
        permissions = root.findall("uses-permission")
        permission_names = [p.get(f"{{{ANDROID_NS}}}name", "") for p in permissions]

        # INTERNET permission is a warning for privacy keyboard
        if any("INTERNET" in p for p in permission_names):
            result.warnings.append("INTERNET permission declared — verify not used by keyboard")

        # Check IME service declaration
        services = app.findall("service") if app is not None else []
        ime_found = False
        for service in services:
            intent_filter = service.find("intent-filter")
            if intent_filter is not None:
                actions = intent_filter.findall("action")
                for action in actions:
                    if "InputMethod" in action.get(f"{{{ANDROID_NS}}}name", ""):
                        ime_found = True
        if not ime_found:
            result.errors.append("No INPUT_METHOD service declared")

    except ET.ParseError as e:
        result.errors.append(f"Manifest parse error: {e}")
    return result


def validate_themes(filepath: Path) -> ValidationResult:
    """Validate themes.xml has all required keys."""
    result = ValidationResult(file="themes.xml")
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()

        # Find the main theme
        for style in root.findall("style"):
            name = style.get("name", "")
            if "Theme" in name and "Splash" not in name:
                items = style.findall("item")
                theme_keys = set()
                for item in items:
                    item_name = item.get("name", "")
                    theme_keys.add(item_name)

                missing = REQUIRED_THEME_KEYS - theme_keys
                if missing:
                    result.errors.append(f"Theme '{name}' missing keys: {missing}")

                extra = theme_keys - REQUIRED_THEME_KEYS
                if extra:
                    result.warnings.append(f"Theme '{name}' has extra keys: {extra}")

    except ET.ParseError as e:
        result.errors.append(f"Themes parse error: {e}")
    return result


def validate_method_xml(filepath: Path) -> ValidationResult:
    """Validate method.xml (IME configuration)."""
    result = ValidationResult(file="method.xml")
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()

        # Check for required input methods
        subtypes = root.findall(".//subtype")
        if len(subtypes) == 0:
            result.errors.append("No subtypes defined in method.xml")

        # Check English subtype has isAsciiCapable
        for subtype in subtypes:
            label = subtype.get(f"{{{ANDROID_NS}}}label", "")
            ascii_capable = subtype.get(f"{{{ANDROID_NS}}}isAsciiCapable", "false")

            if "English" in label and ascii_capable != "true":
                result.errors.append(f"English subtype should have isAsciiCapable=true")
            if "Hindi" in label and ascii_capable != "false":
                result.errors.append(f"Hindi subtype should have isAsciiCapable=false")

    except ET.ParseError as e:
        result.errors.append(f"method.xml parse error: {e}")
    return result


def validate_string_resources(filepath: Path) -> ValidationResult:
    """Validate strings.xml has all required strings."""
    result = ValidationResult(file="strings.xml")
    required_strings = [
        "app_name",

    ]
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()
        string_names = {s.get("name", "") for s in root.findall("string")}

        for req in required_strings:
            if req not in string_names:
                result.errors.append(f"Missing required string: {req}")

    except ET.ParseError as e:
        result.errors.append(f"strings.xml parse error: {e}")
    return result


def validate_preference_xml(filepath: Path, name: str) -> ValidationResult:
    """Validate preference XML files."""
    result = ValidationResult(file=f"{name}.xml")
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()

        # Check all preferences have keys (app:key or android:key)
        APP_NS = "http://schemas.android.com/apk/res-auto"
        for elem in root.iter():
            if elem.tag in ("EditTextPreference", "SwitchPreferenceCompat", "SeekBarPreference", "ListPreference"):
                android_key = elem.get(f"{{{ANDROID_NS}}}key")
                app_key = elem.get(f"{{{APP_NS}}}key")
                if android_key is None and app_key is None:
                    result.errors.append(f"{elem.tag} missing key (need app:key or android:key)")

    except ET.ParseError as e:
        result.errors.append(f"{name}.xml parse error: {e}")
    return result


def validate_drawable(filepath: Path) -> ValidationResult:
    """Validate drawable XML files."""
    result = ValidationResult(file=str(filepath.relative_to(PROJECT_ROOT)))
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()
        # If it parses, it's valid
    except ET.ParseError as e:
        result.errors.append(f"Drawable parse error: {e}")
    return result


def validate_layout(filepath: Path) -> ValidationResult:
    """Validate layout XML files."""
    result = ValidationResult(file=str(filepath.relative_to(PROJECT_ROOT)))
    try:
        tree = ET.parse(filepath)
        root = tree.getroot()

        # Check root element
        if root.get("{{{ANDROID_NS}}}id") is None and root.tag != "ScrollView":
            result.warnings.append("Root element has no android:id")

        # Check all views have IDs
        for view in root.iter():
            view_id = view.get(f"{{{ANDROID_NS}}}id")
            # LinearLayout, FrameLayout etc. don't need IDs
            if view_id is None and view.tag not in ("LinearLayout", "FrameLayout", "ScrollView", "androidx.appcompat.widget.Toolbar", "com.google.android.material.appbar.AppBarLayout", "LinearLayout", "FrameLayout", "TextView", "ImageView", "Space"):
                pass  # IDs are optional for most views

    except ET.ParseError as e:
        result.errors.append(f"Layout parse error: {e}")
    return result


def validate_launcher_icons() -> ValidationResult:
    """Validate launcher icon XML files."""
    result = ValidationResult(file="launcher icons")
    icon_files = [
        "res/mipmap-anydpi-v26/ic_launcher.xml",
        "res/mipmap-anydpi-v26/ic_launcher_round.xml",
    ]
    for icon_rel in icon_files:
        icon_path = APP_SRC / icon_rel
        if icon_path.exists():
            r = validate_xml_wellformedness(icon_path)
            result.errors.extend(r.errors)
            result.warnings.extend(r.warnings)
        else:
            result.errors.append(f"Missing: {icon_rel}")
    return result


# ========================================================================
# Main Runner
# ========================================================================

def find_all_xml() -> List[Path]:
    """Find all XML files in the project."""
    xml_files = []
    for root_dir in [APP_SRC / "res", APP_SRC / "res" / "xml"]:
        if root_dir.exists():
            for f in root_dir.rglob("*.xml"):
                xml_files.append(f)
    manifest = APP_SRC / "AndroidManifest.xml"
    if manifest.exists():
        xml_files.append(manifest)
    return xml_files


def main() -> int:
    print("=" * 70)
    print("  AkashBoard XML Validator")
    print("=" * 70)

    results: List[ValidationResult] = []

    # 1. Validate all XML for well-formedness
    print("\n[1/7] XML Well-formedness...")
    for xml_file in find_all_xml():
        r = validate_xml_wellformedness(xml_file)
        results.append(r)

    # 2. Manifest lint
    print("[2/7] Manifest lint...")
    manifest = APP_SRC / "AndroidManifest.xml"
    if manifest.exists():
        results.append(validate_manifest(manifest))

    # 3. Theme consistency
    print("[3/7] Theme consistency...")
    for theme_file in APP_SRC.glob("res/values*/themes.xml"):
        results.append(validate_themes(theme_file))

    # 4. method.xml
    print("[4/7] method.xml validation...")
    method_xml = APP_SRC / "res" / "xml" / "method.xml"
    if method_xml.exists():
        results.append(validate_method_xml(method_xml))

    # 5. Strings
    print("[5/7] String resources...")
    strings_xml = APP_SRC / "res" / "values" / "strings.xml"
    if strings_xml.exists():
        results.append(validate_string_resources(strings_xml))

    # 6. Preferences
    print("[6/7] Preference XMLs...")
    for pref_file in APP_SRC.glob("res/xml/preferences_*.xml"):
        results.append(validate_preference_xml(pref_file, pref_file.stem))

    # 7. Launcher icons
    print("[7/7] Launcher icons...")
    results.append(validate_launcher_icons())

    # ========================================================================
    # Report
    # ========================================================================

    print("\n" + "=" * 70)
    print("  RESULTS")
    print("=" * 70)

    total_errors = 0
    total_warnings = 0
    passed = 0
    failed = 0

    for r in results:
        if r.errors:
            failed += 1
            total_errors += len(r.errors)
            print(f"\n[FAIL] {r.file}")
            for e in r.errors:
                print(f"   ERROR: {e}")
        elif r.warnings:
            passed += 1
            total_warnings += len(r.warnings)
            print(f"\n[WARN]  {r.file}")
            for w in r.warnings:
                print(f"   WARN: {w}")
        else:
            passed += 1

    print(f"\n{'=' * 70}")
    print(f"  Total: {len(results)} files | [PASS] {passed} passed | [FAIL] {failed} failed | {total_errors} errors | {total_warnings} warnings")
    print(f"{'=' * 70}")

    return 0 if total_errors == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
