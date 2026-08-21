// AkashBoard Engine — Build configuration for Android NDK cross-compilation
//
// This file configures the Rust build for ARM64 Android targets.
// It's used by cargo-ndk to set up the correct toolchain.

fn main() {
    // The build is handled by cargo-ndk externally:
    //   cargo ndk -t arm64-v8a build --release
    //
    // This file exists for future build customizations (e.g., version info, features)
    println!("cargo:rerun-if-changed=src/lib.rs");
    println!("cargo:rerun-if-changed=src/predictor.rs");
    println!("cargo:rerun-if-changed=src/learner.rs");
}
