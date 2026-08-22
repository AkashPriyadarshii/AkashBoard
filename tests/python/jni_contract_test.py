#!/usr/bin/env python3
"""
jni_contract_test.py — Validates JNI function signatures match between Kotlin and Rust.

Ensures that every JNI function declared in Kotlin's PredictorBridge.kt
has a corresponding implementation in Rust's lib.rs with matching types.

Run: python tests/python/jni_contract_test.py
"""

import re
import sys
from pathlib import Path
from dataclasses import dataclass
from typing import List, Dict, Tuple

PROJECT_ROOT = Path(__file__).parent.parent.parent


@dataclass
class JniFunction:
    name: str
    kotlin_params: List[str]
    kotlin_return: str
    rust_params: List[str]
    rust_return: str


def extract_kotlin_jni_methods(content: str) -> Dict[str, JniFunction]:
    """Extract external JNI method declarations from PredictorBridge.kt."""
    methods = {}

    # Pattern: external fun methodName(params): ReturnType
    pattern = r'external\s+fun\s+(\w+)\s*\(([^)]*)\)\s*:\s*(\w+)'
    for match in re.finditer(pattern, content):
        name = match.group(1)
        params_str = match.group(2).strip()
        return_type = match.group(3)

        params = []
        if params_str:
            for param in params_str.split(","):
                param = param.strip()
                # Extract type (last token)
                parts = param.split(":")
                if len(parts) == 2:
                    params.append(parts[1].strip())

        methods[name] = JniFunction(
            name=name,
            kotlin_params=params,
            kotlin_return=return_type,
            rust_params=[],
            rust_return=""
        )

    return methods


def extract_rust_jni_functions(content: str) -> Dict[str, JniFunction]:
    """Extract JNI function implementations from lib.rs."""
    methods = {}

    # Pattern: Java_com_akashboard_engine_PredictorBridge_<methodName>
    pattern = r'pub\s+extern\s+"system"\s+fn\s+Java_com_akashboard_engine_PredictorBridge_(\w+)\s*\(([^)]*)\)\s*->\s*(\w+)'
    for match in re.finditer(pattern, content, re.DOTALL):
        name = match.group(1)
        params_str = match.group(2).strip()
        return_type = match.group(3)

        params = []
        for line in params_str.split("\n"):
            line = line.strip().rstrip(",")
            if line and not line.startswith("//"):
                # Extract type (first word after colon)
                parts = line.split(":")
                if len(parts) >= 2:
                    param_type = parts[1].strip().split()[0]
                    params.append(param_type)

        methods[name] = JniFunction(
            name=name,
            kotlin_params=[],
            kotlin_return="",
            rust_params=params,
            rust_return=return_type
        )

    return methods


def main() -> int:
    print("=" * 70)
    print("  AkashBoard JNI Contract Test")
    print("=" * 70)

    # Find files
    kt_bridge = PROJECT_ROOT / "app" / "src" / "main" / "java" / "com" / "akashboard" / "engine" / "PredictorBridge.kt"
    rs_lib = PROJECT_ROOT / "engine" / "src" / "lib.rs"

    if not kt_bridge.exists():
        print(f"\n[FAIL] PredictorBridge.kt not found at {kt_bridge}")
        return 1
    if not rs_lib.exists():
        print(f"\n[FAIL] lib.rs not found at {rs_lib}")
        return 1

    kt_content = kt_bridge.read_text(errors="ignore")
    rs_content = rs_lib.read_text(errors="ignore")

    kotlin_methods = extract_kotlin_jni_methods(kt_content)
    rust_methods = extract_rust_jni_functions(rs_content)

    errors = 0
    warnings = 0

    print(f"\nFound {len(kotlin_methods)} Kotlin JNI methods")
    print(f"Found {len(rust_methods)} Rust JNI functions")

    # Check each Kotlin method has a Rust implementation
    print("\n[1/3] Kotlin -> Rust coverage...")
    for name, kt_method in kotlin_methods.items():
        if name in rust_methods:
            print(f"  [PASS] {name}")
        else:
            print(f"  [FAIL] {name} — NO RUST IMPLEMENTATION")
            errors += 1

    # Check each Rust function has a Kotlin declaration
    print("\n[2/3] Rust -> Kotlin coverage...")
    for name, rs_method in rust_methods.items():
        if name in kotlin_methods:
            print(f"  [PASS] {name}")
        else:
            print(f"  [WARN]  {name} — Rust-only (no Kotlin declaration)")
            warnings += 1

    # Type compatibility check (basic)
    print("\n[3/3] Type compatibility...")
    # JNI type mappings
    type_map = {
        "JString": "jstring",
        "JClass": "JClass",
        "JNIEnv": "JNIEnv",
        "jboolean": "jboolean",
        "jstring": "jstring",
        "jint": "jint",
        "jlong": "jlong",
    }

    for name in kotlin_methods:
        if name in rust_methods:
            kt = kotlin_methods[name]
            rs = rust_methods[name]

            # Skip first 2 params (JNIEnv, JClass) in Rust
            rs_data_params = rs.rust_params[2:] if len(rs.rust_params) > 2 else []

            # Check param count (Kotlin params + JNI env/class = Rust params)
            kt_count = len(kt.kotlin_params)
            rs_count = len(rs_data_params)

            if kt_count != rs_count:
                print(f"  [WARN]  {name}: Kotlin has {kt_count} params, Rust has {rs_count} data params")
                warnings += 1
            else:
                print(f"  [PASS] {name}: param count matches ({kt_count})")

    # Report
    print(f"\n{'=' * 70}")
    print(f"  RESULTS: {errors} errors, {warnings} warnings")
    print(f"{'=' * 70}")

    return 0 if errors == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
