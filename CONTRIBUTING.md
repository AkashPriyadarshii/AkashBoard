# CONTRIBUTING.md — Contribution Guidelines

## Welcome to AkashBoard! 🔥

Thank you for considering contributing to AkashBoard. This document explains how to get started, the rules we follow, and how to submit your work.

---

## Code of Conduct

- Be respectful and constructive
- Focus on the code, not the person
- Welcome newcomers and help them learn
- No tolerance for harassment or discrimination

---

## How to Contribute

### 1. Fork & Clone

```bash
# Fork on GitHub, then:
git clone https://github.com/YOUR_USERNAME/AkashBoard.git
cd AkashBoard
git remote add upstream https://github.com/AkashPriyadarshii/AkashBoard.git
```

### 2. Create a Branch

```bash
# For features:
git checkout -b feature/amazing-feature

# For bug fixes:
git checkout -b fix/bug-description

# For docs:
git checkout -b docs/update-readme
```

### 3. Make Changes

Follow the coding conventions in [AGENTS.md](AGENTS.md).

### 4. Test

```bash
# Rust engine tests
cd engine && cargo test && cd ..

# Kotlin unit tests
./gradlew test

# Build
./gradlew assembleDebug
```

### 5. Commit

```bash
# Use conventional commits:
git commit -m "feat: add clipboard history panel"
git commit -m "fix: correct swipe detection on edge cases"
git commit -m "docs: update API reference"
git commit -m "refactor: extract theme parser to separate class"
```

**Commit Message Format:**
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
| Type | Description |
|------|------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | Formatting, no code change |
| `refactor` | Code restructuring, no feature change |
| `perf` | Performance improvement |
| `test` | Adding or updating tests |
| `chore` | Build process, tooling changes |

### 6. Push & PR

```bash
git push origin feature/amazing-feature
```

Then create a Pull Request on GitHub with:
- Clear title describing the change
- Description of what and why
- Screenshots if UI changes
- Test results if applicable

---

## Development Setup

### Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Android Studio | Ladybug+ | https://developer.android.com/studio |
| JDK | 21 | `winget install EclipseAdoptium.Temurin.21.JDK` |
| Rust | 1.96+ | `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs \| sh` |
| cargo-ndk | Latest | `cargo install cargo-ndk` |
| Android NDK | 28.2+ | Via Android Studio SDK Manager |

### Build Commands

```bash
# Build Rust engine
cd engine
cargo ndk -t arm64-v8a build --release
cp target/aarch64-linux-android/release/libakashboard_engine.so \
   ../app/src/main/jniLibs/arm64-v8a/libpredictor.so
cd ..

# Build Android app
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Types of Contributions

### 🐛 Bug Reports

Open an issue with:
- Device model and Android version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots or screen recordings
- Logcat output (if crash)

### ✨ Feature Requests

Open an issue with:
- Clear description of the feature
- Why it would benefit users
- How it fits AkashBoard's philosophy
- Mockups or examples (if UI)

### 🔧 Code Contributions

Pick an issue labeled `good first issue` or `help wanted`.

### 🌍 Translations

Use Weblate (coming soon). For now, add string resources in `app/src/main/res/values-<lang>/strings.xml`.

### 🎨 Themes

Create a theme JSON file following the schema in [DESIGN.md](DESIGN.md). Share in GitHub Discussions.

### 📖 Documentation

Fix typos, improve explanations, add examples. All help is welcome!

---

## What We're Looking For

### ✅ We Want
- Privacy-respecting features (no cloud, no analytics)
- Performance improvements (faster predictions, less memory)
- UI polish (animations, accessibility, responsiveness)
- New language support
- Bug fixes
- Documentation improvements

### ❌ We Don't Want
- Features that require network access
- Proprietary libraries or blobs
- Features that bloat the APK beyond 5MB
- Code that compromises user privacy
- Breaking changes without discussion

---

## Review Process

1. **Automated checks** must pass (build, lint, tests)
2. **Code review** by maintainer
3. **Manual testing** on real device
4. **Merge** after approval

**Response time:** We aim to review PRs within 7 days.

---

## Recognition

Contributors will be:
- Listed in the app's "About" screen
- Mentioned in release notes
- Given credit in README.md

---

## Questions?

Open a GitHub Discussion or reach out on X (Twitter): [@Akash__ydv001](https://twitter.com/Akash__ydv001)

---

## License

By contributing, you agree that your contributions will be licensed under the [GNU General Public License v3.0](LICENSE).
