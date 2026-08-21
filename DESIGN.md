# DESIGN.md — UI/UX Design System

## AkashBoard v1.0

**Author:** Akash Priyadarshi
**Date:** August 21, 2026
**Status:** Production Design
**Design Language:** Quiet Precision

---

## 0. Design Philosophy

> "A quiet, adaptive, highly configurable keyboard whose quality is felt through responsiveness and control rather than advertised through decoration."

### What This Product Is

AkashBoard is **not** a themed keyboard with a settings screen. It is a **high-performance input system** with a configuration environment.

### Performance Priority Order

The keyboard must optimize, **in this exact order**:

1. **Input latency** — Every millisecond matters. Users feel lag instantly.
2. **Hit accuracy** — Keys must register correctly, every time.
3. **Reachability** — One-handed use must be comfortable on all screen sizes.
4. **Editing speed** — Cursor movement, deletion, selection must be fast.
5. **Contextual usefulness** — Suggestions, corrections, completions must be relevant.
6. **Accessibility** — Must work for everyone, regardless of ability.
7. **Personalization** — Themes, layouts, shortcuts must adapt to the user.
8. **Visual character** — The keyboard must look good, but NEVER at the cost of items 1-7.

**Visual styling must never compromise the first five.**

### Design Keywords

The interface should communicate:
- Engineered
- Trustworthy
- Tactile
- Modern
- Configurable
- Fast
- Calm

The interface should NOT communicate:
- Toy
- Gaming keyboard
- AI gimmick
- Concept-art UI
- Futuristic sci-fi dashboard
- Dribbble shot
- Glassmorphism showcase

### What We Deliberately Do NOT Use

- Neon gradients
- Permanent "AI" branding
- Floating glass blobs
- Giant pill-shaped keys
- Excessive glassmorphism (frosted glass everywhere)
- Fake 3D key extrusion
- Oversized shadows
- Animated gradient backgrounds
- Decorative particles
- Endless theme-card galleries
- Five-level nested settings for trivial options
- Generic dashboard cards
- Fake "futuristic" copy
- Motion that exists only to look impressive
- A toolbar permanently packed with features

These choices are rejected because they compete with typing, consume vertical space, increase cognitive load, or create a generic visual signature.

---

## 1. Product Architecture

There are **two products** in one system:

### A. Live Keyboard (Restrained)

**Purpose:** Type, edit, switch language, access high-value secondary actions.

**Characteristics:**
- Dense
- Immediate
- Tactile
- Predictable
- Low visual noise
- Optimized for one-handed use
- Adaptive to field context

The keyboard is the typing surface. It must remain calm, predictable, and extremely responsive. Visual expression is minimal. Every pixel serves a functional purpose.

### B. Companion Configuration App (Expressive)

**Purpose:** Configure, personalize, inspect, test, manage languages, dictionary, gestures, privacy.

**Characteristics:**
- Spacious
- Adaptive
- More visually expressive
- Direct manipulation
- Live preview
- Deeper configuration

The companion app is allowed more visual expression because it is not occupying the user's typing surface.

---

## 2. Performance Requirements

### Frame Rate Targets

| Context | Target | Minimum |
|---------|--------|---------|
| Key press animation | 120fps | 60fps |
| Suggestion bar transitions | 120fps | 60fps |
| Swipe trail rendering | 120fps | 60fps |
| Theme switching | 60fps | 60fps |
| Keyboard show/hide | 60fps | 60fps |
| Emoji panel scroll | 120fps | 60fps |
| Clipboard panel scroll | 120fps | 60fps |

### Latency Targets

| Action | Target | Maximum |
|--------|--------|---------|
| Touch to visual feedback | <8ms | 16ms |
| Touch to character output | <16ms | 32ms |
| Prediction response | <1ms | 5ms |
| Swipe word recognition | <5ms | 10ms |
| Auto-correct application | <8ms | 16ms |

### Memory Budget

| Component | Target | Maximum |
|-----------|--------|---------|
| Keyboard view | 5MB | 10MB |
| Prediction engine (Rust) | 2MB | 4MB |
| Clipboard history | 1MB | 2MB |
| Theme data | 500KB | 1MB |
| Total app | 15MB | 30MB |

### Battery Impact

| Metric | Target |
|--------|--------|
| Active typing (1hr/day) | <1% battery |
| Idle (keyboard not visible) | 0% battery |
| Background learning | <0.5% battery/day |

### Implementation Rules

- **No animation on the main typing path that can block input**
- **No network dependency for basic key input**
- **No remote call required to display normal keys**
- **Local response for key press feedback**
- **Local cursor movement**
- **Local deletion**
- **Local language switch**
- **Predictable memory use**
- **Stable performance during long typing sessions**
- **A visually beautiful keyboard that introduces input latency is considered a design failure**

---

## 3. Surface Model

Use three surface levels:

```
Level 0 — Canvas
  The environment around content.

Level 1 — Surface
  Main app/keyboard container.

Level 2 — Interactive Surface
  Keys, controls, selected states, floating toolbars.

Level 3 — Transient Surface
  Sheets, popovers, menus, dialogs.
```

### Surface Rules

- Do not stack many translucent surfaces
- Translucency is reserved for:
  - Contextual toolbar (temporary)
  - Temporary overlay
  - Companion-app floating navigation
  - Sheets/popovers where spatial context benefits from transparency
- The keyboard itself should normally be an **opaque or near-opaque surface** for legibility and visual stability
- No more than 2 translucent layers visible simultaneously

---

## 4. Color System

### Rule

Color communicates **state and hierarchy**. It is not the decoration layer.

> **Insight from Apple:** UI chrome recedes so the product can speak. No decorative gradients, no shadows on chrome.
> **Insight from Linear:** The accent color appears on focus rings and a few intentional CTAs — never decoratively.
> **Insight from Stripe:** Use tabular-figure body type where numerics matter.
> **Insight from Figma:** Weight (not opacity) carries the hierarchy. Body copy is always the same color at different weights.

### Light Theme

```css
Canvas       #F5F6F8          /* Background behind keyboard */
Surface      #FFFFFF          /* Keyboard surface */
Surface-2    #ECEEF2          /* Elevated elements */
Key          #E7E9ED          /* Default key background */
Key-Pressed  #D9DCE2          /* Key on press */
Text         #15171A          /* Primary text (key labels) */
Text-2       #60656D          /* Secondary text (suggestions) */
Accent       system-derived   /* Dynamic color from OS */
Destructive  #B3261E          /* Error, delete actions */
Success      #1A7D3F          /* Confirmation states */
Warning      #B45309          /* Caution states */
```

### Dark Theme

```css
Canvas       #111214          /* Background behind keyboard */
Surface      #1A1C20          /* Keyboard surface */
Surface-2    #24272C          /* Elevated elements */
Key          #2B2E34          /* Default key background */
Key-Pressed  #363A42          /* Key on press */
Text         #F2F3F5          /* Primary text (key labels) */
Text-2       #A6ABB4          /* Secondary text (suggestions) */
Accent       system-derived   /* Dynamic color from OS */
Destructive  #FF6B60          /* Error, delete actions */
Success      #34C759          /* Confirmation states */
Warning      #FF9F0A          /* Caution states */
```

### Dynamic Color

Use OS-derived accent color where the platform supports it (Android 12+ dynamic colors, Material You).

**Use accent for:**
- Cursor
- Active modifier (shift, caps)
- Selected language
- Text selection highlight
- Focused setting
- Active slider thumb
- Important status indicator

**Do NOT:**
- Tint the entire keyboard with accent
- Use accent for key backgrounds
- Use accent for suggestion bar background
- Over-saturate the interface with accent

### Color Accessibility

All color combinations must meet **WCAG 2.1 AA** standards:
- Normal text: 4.5:1 contrast ratio minimum
- Large text: 3:1 contrast ratio minimum
- Interactive elements: 3:1 against adjacent colors
- No information conveyed by color alone (use shape + color)

---

## 5. Typography

### Rule

Use the **platform system font** unless there is a demonstrable product reason not to.

> **Insight from Apple:** SF Pro at thin weights with negative letter-spacing creates confident editorial cadence.
> **Insight from Stripe:** Thin (300) weights for display, tabular figures for numerics.
> **Insight from Linear:** Measured negative tracking on display sizes creates "quietly luxurious" feel.
> **Insight from Figma:** Variable typeface at fine weight increments (320, 330, 340) — a single voice that flexes rather than a multi-weight family.
> **Insight from Superhuman:** Tight tracking, weight 460-540, body at 1.5 line-height.

- Optical sizing where supported
- Platform text scaling respected
- Size-specific tracking
- Readable line height
- Restrained weight hierarchy

### Keyboard Key Labels

Key labels are **not** selected by aesthetic preference. They are selected for **rapid recognition at the actual device viewing distance**.

```css
Key Label:         22sp   Medium (500)   Line height: 1.0
Key Sub-label:     10sp   Regular (400)   Line height: 1.0
Suggestion Text:   16sp   Medium (500)   Line height: 1.4
Suggestion Sub:    12sp   Regular (400)   Line height: 1.4
Emoji:             24sp   Regular (400)   Line height: 1.0
Settings Label:    16sp   Regular (400)   Line height: 1.5
Settings Value:    14sp   Medium (500)   Line height: 1.5
```

### Companion App Typography

```css
Display:    28-32sp   Weight: 300-400   Tracking: -0.5px
Title:      22-24sp   Weight: 500       Tracking: -0.3px
Section:    16-18sp   Weight: 500       Tracking: 0
Body:       15-16sp   Weight: 400       Tracking: 0
Secondary:  13-14sp   Weight: 400       Tracking: 0
Caption:    12-13sp   Weight: 400       Tracking: +0.2px
```

---

## 6. Shape System

Use a **narrow radius family**:

```css
xs:  6px
sm:  8px
md:  12px
lg:  16px
```

### Rules

- Avoid arbitrary per-component radii
- Keyboard keys: 8-12px radius on normal phones (adjust based on key size)
- Do not turn keys into extreme capsules
- App controls: 8-16px depending on hierarchy
- Large cards should NOT automatically receive 24-32px radius

> **Insight from Apple:** Tight-radius pills for buttons, product tiles use square corners.
> **Insight from Linear:** Cards live as charcoal panels with hairline borders — not rounded cards with shadows.
> **Insight from Stripe:** Tight-radius pills (6-8px) for buttons, not oversized.

---

## 7. Spacing System

Base unit: **4px**

```css
xs:   4px
sm:   8px
md:   12px
lg:   16px
xl:   24px
xxl:  32px
```

### Keyboard Geometry

Geometry is measured **before decoration**.

The engine adapts to:
- Device width
- Device height
- Keyboard safe-area requirements
- Orientation
- One-handed mode
- Language
- Key count
- Row count
- Device density
- User-selected keyboard height

**Do not hard-code one keyboard screenshot into the app.**

> **Insight from GitHub Primer:** Responsive behavior is foundational. Layout, typography, color and spacing are shared primitives.
> **Insight from Shopify Polaris:** Internationalization must be designed into the system.

---

## 8. Key Model

### Visual Key vs Touch Hitbox

The visual key and touch hitbox are **separate concepts**:

```
         touch region (larger)
   ┌─────────────────────┐
   │     ┌──────────┐    │
   │     │  visual  │    │
   │     │   key    │    │
   │     └──────────┘    │
   └─────────────────────┘
```

The engine enlarges effective hit regions into surrounding whitespace where doing so does not create ambiguous overlaps. **This is more valuable than increasing visible key size indefinitely.**

> **Insight from Radix Primitives:** Focus management is explicit. Collision handling matters. Origin-aware animation matters.

### Key Data Model

```kotlin
data class KeyData(
    val id: String,              // Unique key identifier
    val label: String,           // Display label (e.g., "Q")
    val code: Int,               // Key code (e.g., KEYCODE_Q)
    val rect: RectF,             // Visual bounding rectangle
    val hitRect: RectF,          // Touch hit rectangle (larger than rect)
    val type: KeyType,           // Letter, shift, delete, space, etc.
    val popupLabel: String?,     // Long-press popup
    val popupCode: Int?,         // Long-press key code
    val width: Int,              // Width multiplier (1 = standard)
    val accessibilityLabel: String // For screen readers
)

enum class KeyType {
    LETTER, SHIFT, DELETE, SPACE, ENTER,
    SYMBOL切换, EMOJI, VOICE, GLOBE, COMMA, PERIOD
}
```

---

## 9. Keyboard Layout

### Baseline QWERTY

```
Q W E R T Y U I O P
 A S D F G H J K L
  ⇧ Z X C V B N M ⌫

123  language     SPACE      ,  ↵
```

Row offsets and dimensions are **calculated per device**. The actual layout is dynamic, not static.

### Bottom Row

The bottom row is an **ergonomic control region**. Elements:
- Numeric/symbol switch
- Language switch
- Emoji
- Space
- Punctuation
- Enter/search
- Backspace (as appropriate)

Do not permanently add every possible control. Keep it minimal.

> **Insight from Apple:** Purpose before decoration. Simplicity without hiding necessary functionality.

---

## 10. Spacebar Interactions

The spacebar is a **primary interaction surface**.

| Gesture | Action |
|---------|--------|
| Tap | Insert space |
| Horizontal swipe | Cursor movement (continuous tracking) |
| Long press | Cursor/navigation mode |
| Double tap | (configurable) |

Cursor movement must track the finger **continuously**. Do not use a hidden gesture without teaching it at least once.

---

## 11. Backspace Interactions

| Gesture | Action |
|---------|--------|
| Tap | Delete one character |
| Hold | Accelerated deletion (linear acceleration) |
| Swipe left | Progressive word deletion |

Bulk deletion should **accelerate predictably** rather than suddenly jumping from character-by-character to uncontrolled deletion.

---

## 12. Cursor System

### Primary Method
**Horizontal spacebar gesture**

### Secondary Method
**Explicit cursor mode** (optional, not permanently consuming keyboard area)

Cursor mode exposes:
```
←      →
word   char
select
```

---

## 13. Prediction Rail

The prediction rail is **contextual**. It changes based on input field type.

### Normal Text
```
because    tomorrow    already
```

### Correction
```
tomorrow  →  corrected word
```

### URL Field
```
.com      /      ://
```

### Email Field
```
@gmail.com      @outlook.com
```

### Selection Active
```
Cut      Copy      Paste      Select All
```

### Number Field
```
No prediction rail (hidden)
```

### Password Field
```
No prediction content (hidden)
```

> **Insight from Linear:** The prediction rail should feel like a dense, technical tool — not a marketing surface. Each suggestion is a precise, functional item.

---

## 14. Emoji Panel

### Structure
```
Search bar

Recent
Smileys & People
Animals & Nature
Food & Drink
Travel & Places
Objects
Symbols
Flags
```

### Priority
1. Search
2. Recent
3. Category switching

Do not waste vertical space on decorative category art.

---

## 15. Symbol Layouts

### Everyday Mode
```
! @ # $ % & * ( )
```

### Developer Mode
```
{ } [ ] ( )
< > / \
| _ ~
= + - *
: ; ,
```

### Math Mode
```
± × ÷ ≈ ≠ ≤ ≥ √ ∑ π ∞
```

The layout is **selectable** instead of forcing all symbols into one crowded page.

---

## 16. One-Handed Mode

Do not merely shrink the keyboard. Use a **constrained-width keyboard anchored left or right**.

Settings:
```
One-handed mode
  Off
  Left
  Right
  Custom width (continuous slider with live preview)
```

---

## 17. Keyboard Height

Expose a **live slider**:
```
Short ─────────●──────── Tall
```

Preview updates **immediately**. Do not use only Small/Medium/Large. A continuous adjustment is more useful because device dimensions vary significantly.

---

## 18. Motion System

> **Insight from Apple HIG:** Purpose, Agency, Responsibility, Familiarity, Flexibility, Simplicity, Craft, Delight. Motion should explain change, not exist to look impressive.
> **Insight from Fluent 2:** Motion should feel natural and velocity-aware. Motion must remain functional.
> **Insight from Apple Reference:** Critically damped defaults. Reserve bounce for momentum-driven interactions only. Velocity handoff is the seam between gesture and animation.

### Default Motion (Critically Damped)

```css
damping:  1.0
response: 0.3-0.4s
```

Use for:
- Toolbar expansion
- Setting transitions
- Sheets
- Repositioning
- Suggestion bar changes

### Momentum Motion (Mild Under-Damping)

```css
damping:  ~0.8
response: 0.3-0.4s
```

Use ONLY when the user's gesture has momentum:
- Keyboard reposition flick
- One-handed keyboard movement
- Gesture-driven sheet
- Custom layout manipulation

**Do not add bounce to static menu appearances.**

### Animation Specifications

| Animation | Duration | Easing | Properties |
|-----------|----------|--------|------------|
| Key press | 80ms | ease-out | scale 1.0→0.92, bg→accent-glow |
| Key release | 120ms | ease-in-out | scale 0.92→1.0, glow→none |
| Suggestion slide in | 200ms | ease-out | translateX, staggered 50ms |
| Suggestion slide out | 200ms | ease-in | translateX |
| Layout switch | 150ms | ease-in-out | opacity crossfade |
| Keyboard show | 250ms | ease-out | translateY from bottom |
| Keyboard hide | 200ms | ease-in | translateY to bottom |
| Theme switch | 300ms | ease-in-out | opacity crossfade |
| Emoji panel open | 200ms | ease-out | translateY from bottom |
| Toolbar expand | 200ms | critically damped | height + opacity |

---

## 19. Interruptibility

**Never disable interaction merely because an animation is running.**

If the user reverses direction during an animation:

```
current visual position + current velocity → new target
```

The animation continues from the **actual current state**.

Never:
```
old target → jump → new target
```

That visible discontinuity makes the UI feel synthetic.

> **Insight from Apple Reference:** The uploaded reference explicitly recommends interactive prototypes because the interaction itself exposes design problems that static designs hide.

### Implementation

```kotlin
// Interruptible animation using spring physics
class InterruptibleAnimator {
    private var currentValue: Float = 0f
    private var currentVelocity: Float = 0f
    private var targetValue: Float = 0f
    
    fun setTarget(newTarget: Float) {
        // Use CURRENT position and velocity as starting point
        // NOT the old target
        targetValue = newTarget
        // Spring simulation continues from current state
    }
}
```

---

## 20. Gesture Engine

Every gesture follows the same model:

```
touch down
   ↓
immediate feedback (<8ms)
   ↓
continuous tracking
   ↓
intent detection (with hysteresis)
   ↓
commit / cancel
   ↓
optional physics (spring/momentum)
```

### Requirements

- No artificial delay before tracking begins
- Hysteresis for directional gestures (prevent jitter)
- Cancel-by-dragging-away where appropriate
- Preserve finger-to-content offset
- Do not lock the user during animations
- Velocity handoff between gesture and animation

> **Insight from Apple Reference:** Velocity handoff is the seam between gesture and animation. A swipe should feel like one uninterrupted physical interaction.

### Gesture Velocity

On release:
```
dragging → release velocity → spring initial velocity → target selection
```

A swipe should feel like **one uninterrupted physical interaction**.

---

## 21. Rubber-Banding

Use soft resistance at configurable boundaries.

Examples:
- One-handed keyboard horizontal position
- Custom keyboard height
- Toolbar drag
- Custom layout editor

The user should feel: **"There is a limit."**
Not: **"The interface froze."**

---

## 22. Components

### Component List

```
KeyboardSurface
Key
ModifierKey
SpaceKey
BackspaceKey
ReturnKey
PredictionItem
PredictionRail
KeyboardToolbar
ToolbarAction
LanguageSwitcher
EmojiPicker
SymbolPicker
CursorMode
SettingRow
SettingGroup
SliderControl
ToggleControl
SegmentedControl
ThemePreview
KeyboardPreview
LayoutComposer
ColorTokenPicker
PrivacyStatusRow
Sheet
Popover
Dialog
Toast
```

### Component Requirements

Every component needs:
- Semantic state (default, active, disabled)
- Pressed state (with visual feedback)
- Disabled state (dimmed, non-interactive)
- Focus state (visible focus ring)
- Accessibility label
- Motion rule (which animation applies)
- Reduced-motion behavior (cross-fade instead of slide)

> **Insight from Radix Primitives:** Behavior and accessibility are inseparable from components. Focus management is explicit. Composable primitives outperform giant opinionated visual abstractions.

---

## 23. Design Tokens

**Design tokens are authoritative.** No arbitrary one-off values in production UI.

If the design requires `radius = 11`, do not invent it inside one screen. Add it to the token system or use the nearest existing token.

> **Insight from Atlassian:** Tokens are the source of truth. Spacing, color, typography, elevation, borders and radius should be systematic.
> **Insight from USWDS:** Design tokens reduce arbitrary decisions.

### Token Registry

```kotlin
object DesignTokens {
    // Colors (see Section 4)
    // Typography (see Section 5)
    // Spacing (see Section 7)
    // Radius (see Section 6)
    // Motion (see Section 18)
    // Elevation
    object Elevation {
        const val KEY = 1f
        const val RAISED_SURFACE = 2f
        const val MODAL = 4f
    }
}
```

---

## 24. Accessibility

Accessibility is **part of the core layout engine**, not a feature toggle.

> **Insight from WCAG 2.2:** Controls need strong focus visibility. Visual focus must not be obscured. Accessibility is a measurable requirement rather than a styling preference.
> **Insight from GOV.UK:** Reusable components. Strong patterns for common tasks. Consistent, understandable interaction. Accessibility as a default property.
> **Insight from Radix:** Focus management is explicit. Every interactive control is keyboard accessible. Focus order is logical. Escape closes temporary surfaces.

### Support

- Larger text (up to 200% scaling)
- Increased contrast (high-contrast mode)
- Reduced motion (replace movement with cross-fade)
- Reduced transparency (opaque surfaces)
- Larger keyboard height
- Larger key spacing
- Stronger/weaker haptics (configurable)
- Alternate sound feedback
- Screen-reader descriptions (TalkBack)
- Platform text scaling
- Left/right one-handed modes
- Adjustable touch sensitivity

### Reduced Motion

When system reduced-motion is enabled:
- Replace movement with cross-fade
- Remove overshoot/bounce
- Preserve state feedback (color changes, opacity)
- Preserve focus visibility
- Keep haptic feedback (if not also reduced)

> **Insight from Apple HIG & Fluent 2:** Reduced motion is an alternate feedback strategy rather than simply deleting all feedback.

---

## 25. Dark/Light Mode

### Automatic Switching

```kotlin
fun onConfigurationChanged(newConfig: Configuration) {
    val isDarkMode = (newConfig.uiMode and
        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    
    val theme = if (isDarkMode) darkTheme else lightTheme
    applyTheme(theme)
}
```

### Transition

- Smooth crossfade between dark/light (300ms)
- No flash of unstyled content
- Cache both themes for instant switching

---

## 26. Sound & Haptics

### Haptic Hierarchy

```
Key press:             low (10ms)
Modifier (shift):      low-medium (15ms)
Mode switch:           medium (25ms)
Selection/commit:      medium (20ms)
Error:                 distinct (double-tap, 10ms)
```

### Rules

- Visual, sound, and haptic should originate from the **same event**
- Never add haptic feedback simply because an action exists
- Haptic intensity must be configurable
- Sound and haptic can be independently enabled/disabled

> **Insight from Apple HIG:** Clear feedback. Detailed craft. Every interaction should feel intentional.

### Sound Packs

| Pack | Description | Volume |
|------|-------------|--------|
| Mechanical | Cherry MX Blue click | 70% |
| Linear | Cherry MX Red thock | 50% |
| Retro | Typewriter clack | 80% |
| Sci-fi | Digital beep | 60% |
| Bubble | Soft pop | 40% |
| None | Silent | 0% |

---

## 27. Iconography

Use **one coherent icon family** per platform.

### Rules

- Consistent stroke/weight
- Optical alignment
- Consistent hit areas
- Icons never replace a necessary label
- Use familiar symbols for familiar actions

### Standard Icons

```
⌫    backspace
⇧    shift
🌐   language
123  symbols
⚙    settings
🎤   voice input
📋   clipboard
😊   emoji
```

Do not create custom "AI-looking" icons for normal features.

> **Insight from Figma:** Monochrome system core. The accent color appears only on focus rings and a few intentional CTAs — never decoratively.

---

## 28. Microcopy

Use **concrete labels**.

### Good

```
Keyboard height
Cursor gestures
Personal dictionary
Network access
Installed languages
Auto-correction
Clipboard history
```

### Bad

```
Typing experience
Supercharge your keyboard
Magic controls
Smart productivity
AI power
Intelligent suggestions
Revolutionary input
```

The design should **explain the feature** rather than market it.

> **Insight from Apple:** Purpose before decoration. Simplicity without hiding necessary functionality.
> **Insight from Linear:** The system reads as software-craft documentation: dense, technical, and quietly luxurious.

---

## 29. Settings Architecture

```
Settings
├── Typing
├── Correction
├── Predictions
├── Gestures
├── Languages
├── Appearance
├── Sounds & Haptics
├── Clipboard
├── Dictionary
├── Accessibility
├── Privacy
└── Advanced
```

Advanced settings are one level deeper. The common path stays visible.

### Settings Row Anatomy

```
┌────────────────────────────────────────────┐
│ Auto-correction                    ON   ›  │
│ Correct common typing mistakes             │
└────────────────────────────────────────────┘
```

Do not turn every setting into a large marketing card. Prefer compact rows, clear grouping, and direct descriptions.

> **Insight from Apple:** Settings should be dense, scannable, and functional — not spacious marketing surfaces.

---

## 30. Empty States

Do not use illustrations unless they materially improve comprehension.

```
Personal dictionary

No custom words yet.

Add words you use often so correction
does not replace them.

[ Add word ]
```

---

## 31. Error States

Errors should be **local and specific**.

### Bad
```
Something went wrong.
```

### Good
```
Keyboard could not load German.

Try again or check the installed language pack.
[ Retry ]
```

---

## 32. Loading States

Avoid generic full-screen spinners for normal keyboard operations.

The typing surface should **not** become a loading screen.

Use:
- Local immediate state
- Skeleton only in companion-app content when necessary
- Optimistic UI where safe
- Background loading outside the input path

---

## 33. Responsive Design

### Phone (Portrait)
```
Q W E R T Y U I O P
 A S D F G H J K L
  ⇧ Z X C V B N M ⌫
123  😊  [  SPACE  ]  ⏎
```

### Phone (Landscape)
```
Q W E R T Y U I O P  │  1 2 3 4 5 6 7 8 9 0
 A S D F G H J K L   │  ! @ # $ % ^ & * ( )
 ⇧ Z X C V B N M ⌫  │  + = - _ / \ | ~ `
123  😊  [  SPACE  ]  ⏎
```

### Companion App

| Width | Layout |
|-------|--------|
| Phone | Single column, full-width, bottom nav |
| Tablet | Sidebar + content, live preview beside controls |
| Desktop | Persistent sidebar, configuration workspace, large preview |

The content hierarchy stays constant even when the layout changes.

> **Insight from GitHub Primer:** Responsive behavior is foundational. The same interface language needs to work across a broad range of usage contexts.

---

## 34. Theme System

### Theme as Token Collection

Themes are **token collections**, not image-based skins.

Minimum tokens:
```
canvas
surface
surface-2
key
keyPressed
text
secondaryText
accent
selection
cursor
destructive
```

Do not ship hundreds of hard-coded themes. Generate controlled variants from the token system.

> **Insight from Figma:** The monochrome system core makes color blocks feel intentional rather than decorative. Same vocabulary, different rhythm per variant.

### Built-in Themes (5)

| Theme | Background | Accent | Character |
|-------|-----------|--------|-----------|
| Akash Dark | #1A1C20 | system-derived | Default, calm |
| Akash Light | #FFFFFF | system-derived | Clean, professional |
| Neon Cyber | #0A0A0A | #00FF88 | Subtle gaming |
| Minimal | #000000 | #FFFFFF | Pure, OLED |
| Sunset | #1A0A1E | #FF6B35 | Warm, expressive |

### Theme JSON Schema

```json
{
  "name": "Akash Dark",
  "version": 1,
  "author": "Akash Priyadarshi",
  "colors": {
    "canvas": "#111214",
    "surface": "#1A1C20",
    "surface2": "#24272C",
    "key": "#2B2E34",
    "keyPressed": "#363A42",
    "text": "#F2F3F5",
    "textSecondary": "#A6ABB4",
    "accent": "system-derived",
    "destructive": "#FF6B60"
  },
  "dimensions": {
    "cornerRadius": 8,
    "keyElevation": 1,
    "keyPadding": 4,
    "suggestionBarHeight": 48
  },
  "animation": {
    "pressScale": 0.92,
    "pressDuration": 80,
    "transitionDuration": 200
  }
}
```

---

## 35. Export/Import

### Export Format

Single JSON file containing:
- Typing profile (DNA, stats, patterns)
- Prediction model (n-grams, corrections)
- Shortcuts
- Active theme + custom themes
- Preferences
- Clipboard history

### Schema Version

```json
{
  "schemaVersion": 1,
  "exportDate": "2026-08-21T12:00:00Z",
  "appVersion": "1.0.0",
  ...
}
```

Forward-compatible: new fields are optional, never break old imports.

---

## 36. Privacy Center

A keyboard handles potentially sensitive input. Privacy must be **explicit and prominent**.

```
Privacy

Network access                         OFF

Typing data collected                  None

Personal dictionary                    On device

Clipboard                              Local

Cloud sync                             OFF

Open source                            GPLv3

Privacy report                         →
```

Do not hide this under "Advanced."

> **Insight from Apple HIG:** Agency and recoverability. The user must always know what's happening with their data.

---

## 37. Interaction Quality Gates

A feature is **not finished** until all of these pass:

```
[ ] Instant touch feedback (<8ms)
[ ] No unnecessary input latency
[ ] Predictable hitbox
[ ] Accessible label
[ ] Clear state
[ ] Reversible where appropriate
[ ] Correct reduced-motion behavior
[ ] Correct dark/light behavior
[ ] Correct landscape behavior
[ ] Correct large-text behavior
[ ] Correct one-handed behavior
[ ] No clipping
[ ] No jump during interruption
[ ] No visual collision
[ ] 60fps minimum, 120fps target
[ ] Zero memory leaks
[ ] Zero crashes in 1000 interactions
```

---

## 38. Design Review Questions

Before shipping a UI feature:

1. What user problem does this solve?
2. Why is this control here?
3. Could a familiar interaction solve it?
4. Does it increase typing speed or accuracy?
5. Does it consume valuable keyboard height?
6. What happens on touch-down?
7. What happens if the gesture reverses?
8. What happens with reduced motion?
9. What happens on a small screen?
10. What happens on a large screen?
11. What happens with accessibility settings?
12. What happens offline?
13. What happens if the user makes a mistake?
14. Is the visual effect communicating something or merely decorating?
15. Can this component be reused without special-case styling?

---

## 39. Design System Provenance

This design system was synthesized from 10 world-class design systems:

| System | Key Insight for AkashBoard |
|--------|---------------------------|
| **Apple HIG** | Purpose before decoration. Critically damped motion. Velocity handoff. Reduced motion as alternate feedback strategy. |
| **Stripe** | Thin weights (300) for display. Tabular figures for numerics. Tight-radius pills. Atmospheric restraint. |
| **Linear** | Near-black canvas. Single accent, used sparingly. Dense, technical, quietly luxurious. Charcoal panels with hairline borders. |
| **Figma** | Monochrome core. Weight (not opacity) carries hierarchy. Variable typeface at fine increments. Color blocks for rhythm. |
| **Superhuman** | Speed-obsessed. Tight tracking. Weight 460-540. High-end newsletter feel. Sober, dense. |
| **Vercel** | Clean, minimal, performance-focused. Sharp corners on buttons, soft on containers. Two-tier radius philosophy. |
| **Airbnb** | Design tokens as source of truth. Internationalization built in. Accessibility as quality. |
| **Notion** | Clean, functional UI. Dense information architecture. No decorative chrome. |
| **Raycast** | Keyboard-first. Every action reachable via keyboard. Minimal visual noise. |
| **Uber** | Motion design as explanation, not decoration. Natural physics. Velocity-aware transitions. |

---

## 40. Final Aesthetic Target

The finished product should look like:

```
ENGINEERING
     +
EDITORIAL CLARITY
     +
TACTILE HARDWARE
     +
MODERN ADAPTIVE SOFTWARE
```

Not:

```
DRIBBBLE SHOT
     +
AI MARKETING
     +
GLASSMORPHISM
     +
RANDOM ANIMATION
```

The visual sophistication comes from **geometry, hierarchy, spacing, typography, state transitions, responsiveness, and physical interaction**.

> **Insight from Linear:** The system reads as software-craft documentation: dense, technical, and quietly luxurious.
> **Insight from Apple:** Detailed craft. The interface should feel engineered, not designed.

---

## 41. Final Design Sentence

**A quiet, adaptive, highly configurable keyboard whose quality is felt through responsiveness and control rather than advertised through decoration.**

---

## 42. Acceptance Bar

The design is considered successful only when a first-time user can:

- Install and enable the keyboard without confusion
- Switch languages immediately
- Type normally without learning the product
- Understand prediction behavior
- Move the cursor quickly
- Delete text efficiently
- Enter symbols without hunting
- Adjust keyboard size
- Use one-handed mode
- Understand privacy behavior
- Customize appearance
- Recover from mistakes
- Use the keyboard with accessibility settings
- Move between phone and companion app without losing the mental model

The interface should become **familiar quickly and invisible during typing**.

---

## Appendix A: Design Token Registry (Kotlin)

```kotlin
object DesignTokens {
    object Color {
        // Light
        const val CANVAS_LIGHT = 0xFFF5F6F8L
        const val SURFACE_LIGHT = 0xFFFFFFFFL
        const val SURFACE2_LIGHT = 0xFFECEEF2L
        const val KEY_LIGHT = 0xFFE7E9EDL
        const val KEY_PRESSED_LIGHT = 0xFFD9DCE2L
        const val TEXT_LIGHT = 0xFF15171AL
        const val TEXT2_LIGHT = 0xFF60656DL
        const val DESTRUCTIVE_LIGHT = 0xFFB3261EL

        // Dark
        const val CANVAS_DARK = 0xFF111214L
        const val SURFACE_DARK = 0xFF1A1C20L
        const val SURFACE2_DARK = 0xFF24272CL
        const val KEY_DARK = 0xFF2B2E34L
        const val KEY_PRESSED_DARK = 0xFF363A42L
        const val TEXT_DARK = 0xFFF2F3F5L
        const val TEXT2_DARK = 0xFFA6ABB4L
        const val DESTRUCTIVE_DARK = 0xFFFF6B60L
    }

    object Spacing {
        const val XS = 4f
        const val SM = 8f
        const val MD = 12f
        const val LG = 16f
        const val XL = 24f
        const val XXL = 32f
    }

    object Radius {
        const val XS = 6f
        const val SM = 8f
        const val MD = 12f
        const val LG = 16f
    }

    object Elevation {
        const val KEY = 1f
        const val RAISED_SURFACE = 2f
        const val MODAL = 4f
    }

    object Motion {
        const val DEFAULT_DAMPING = 1.0f
        const val DEFAULT_RESPONSE = 0.35f
        const val MOMENTUM_DAMPING = 0.8f
        const val MOMENTUM_RESPONSE = 0.35f
    }

    object Animation {
        const val KEY_PRESS_DURATION = 80L
        const val KEY_RELEASE_DURATION = 120L
        const val SUGGESTION_DURATION = 200L
        const val LAYOUT_SWITCH_DURATION = 150L
        const val KEYBOARD_SHOW_DURATION = 250L
        const val KEYBOARD_HIDE_DURATION = 200L
        const val THEME_SWITCH_DURATION = 300L
        const val KEY_PRESS_SCALE = 0.92f
    }
}
```
