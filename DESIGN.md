---
version: 1.0.0
name: precision-spec-sheet
description: >
  AkashBoard marketing site designed as a technical spec sheet on black canvas.
  Zero-radius rectangles, massive monospace stat callouts in orange, uppercase
  Archivo headlines, light Inter body. The specs ARE the marketing.
fusion:
  base: BMW-M (layout, sharp geometry, spec cells, light body weight)
  voltage: ClickHouse (dark canvas, single accent color, stat callout pattern)
  nested: Apple (negative letter-spacing, alternating surface rhythm, scale(0.95) press)
---

# AkashBoard Design System — Precision Spec Sheet

## Concept

The site is a technical data sheet for a hardware-grade product. Every section
reads like a spec entry: label, value, unit. The page rhythm alternates between
black canvas and near-black card surfaces — the color change is the divider,
not borders or shadows. Stat numbers are massive, orange, monospace. Body type
is light-weight and restrained. The entire page has zero drop shadows, zero
border-radius on content elements, zero decorative gradients.

The marketing is the data. Nothing else.

---

## Colors

### Brand
| Token | Hex | Use |
|-------|-----|-----|
| `primary` | `#D34E24` | All CTAs, links, stat callout numbers, accent elements |
| `primary-hover` | `#E85D2C` | Hover state on interactive elements |
| `primary-dark` | `#B8401C` | Active/pressed state |
| `primary-ghost` | `rgba(211,78,36,0.12)` | Ghost button fill, code block borders |

### Canvas
| Token | Hex | Use |
|-------|-----|-----|
| `canvas` | `#0A0A0A` | Page floor — near-pure black |
| `surface-1` | `#121212` | Alternating section bands, subtle depth |
| `surface-card` | `#1A1A1A` | Feature cards, code windows, spec cells |
| `surface-elevated` | `#222222` | Nested cards, hover states |
| `surface-code` | `#0D0D0D` | Code block backgrounds |

### Text
| Token | Hex | Use |
|-------|-----|-----|
| `on-dark` | `#FFFFFF` | Headlines, primary text on dark |
| `body` | `#C0C0C0` | Running body text (not pure white — reduces glare) |
| `body-strong` | `#E0E0E0` | Emphasized body, lead paragraphs |
| `muted` | `#6B6B6B` | Captions, labels, footer links, metadata |
| `muted-strong` | `#888888` | Secondary labels, inactive nav items |

### Hairlines
| Token | Value | Use |
|-------|-------|-----|
| `hairline` | `#2A2A2A` | 1px borders on cards, dividers |
| `hairline-strong` | `#3A3A3A` | Input focus borders, emphasis dividers |

### Accent (reserved — use sparingly)
| Token | Hex | Use |
|-------|-----|-----|
| `success` | `#22C55E` | Verification badges, build-pass indicators |
| `warning` | `#F59E0B` | Warning panel accent stripe |

---

## Typography

### Font Stack
| Role | Font | Fallback |
|------|------|----------|
| **Display / Headlines** | `'Archivo'` | `system-ui, sans-serif` |
| **Body** | `'Inter'` | `system-ui, sans-serif` |
| **Mono / Stats / Code** | `'IBM Plex Mono'` | `'JetBrains Mono', ui-monospace, monospace` |

### Hierarchy

| Token | Size | Weight | Line Height | Letter Spacing | Font | Use |
|-------|------|--------|-------------|----------------|------|-----|
| `hero` | 56px | 900 | 1.0 | -1.5px | Archivo | Hero headline |
| `display-lg` | 40px | 700 | 1.05 | -1px | Archivo | Section headlines |
| `display-md` | 32px | 700 | 1.1 | -0.5px | Archivo | Sub-section headlines |
| `display-sm` | 24px | 700 | 1.2 | 0 | Archivo | Card titles |
| `label-uppercase` | 12px | 700 | 1.4 | 1.5px | Archivo | Section labels, category tags |
| `body-lg` | 18px | 300 | 1.6 | 0 | Inter | Lead paragraphs, hero subtitle |
| `body-md` | 16px | 400 | 1.6 | 0 | Inter | Default running text |
| `body-sm` | 14px | 400 | 1.55 | 0 | Inter | Fine print, footer body |
| `stat-callout` | 56px | 700 | 1.0 | -1.5px | IBM Plex Mono | Spec stat numbers — ALWAYS orange |
| `stat-label` | 14px | 500 | 1.4 | 1px | Archivo | Labels under stat numbers |
| `code` | 14px | 400 | 1.6 | 0 | IBM Plex Mono | Code blocks, terminal output |
| `button` | 14px | 700 | 1.0 | 1.5px | Archivo | All button labels — uppercase |
| `nav-link` | 14px | 500 | 1.4 | 0 | Inter | Top nav menu items |

### Principles
- **Display weights stay 700-900.** Archivo at 900 for the hero only; 700 for everything else. Weight 500 is absent from the display ladder.
- **Body weight stays 300-400.** Light body (300) for lead paragraphs, regular (400) for running text. Never bold body — the contrast between heavy display and light body is the signature.
- **Stat callouts are ALWAYS monospace + orange.** JetBrains Mono at 56px/700, colored `{colors.primary}`. The stat IS the marketing.
- **Uppercase + 1.5px tracking for labels.** All-caps labels at `{typography.label-uppercase}` are machined, not typed. Never lowercase labels.
- **Negative tracking at display sizes.** -1.5px on hero, -1px on section headlines. The tightened tracking gives precision engineering feel.

---

## Spacing

Base unit: 8px.

| Token | Value |
|-------|-------|
| `xxs` | 4px |
| `xs` | 8px |
| `sm` | 12px |
| `md` | 16px |
| `lg` | 24px |
| `xl` | 32px |
| `xxl` | 48px |
| `section` | 96px |

- Section vertical padding: `{spacing.section}` (96px)
- Card internal padding: `{spacing.xl}` (32px)
- Between cards in grid: `{spacing.lg}` (24px)
- Hero vertical padding: `{spacing.xxl}` (48px) top, `{spacing.section}` (96px) bottom

---

## Radius

| Token | Value | Use |
|-------|-------|-----|
| `none` | 0px | **Default.** All cards, buttons, inputs, containers |
| `xs` | 2px | Badge pills only |
| `sm` | 4px | Code block left border accent |
| `full` | 9999px | Circular icon buttons only |

**Zero radius is the brand.** The rectangular silhouette IS engineering precision.
Rounded corners appear only on circular icon buttons and tiny badge pills.

---

## Elevation & Depth

| Level | Treatment | Use |
|-------|-----------|-----|
| Flat | No shadow, no border | Canvas sections, hero, nav |
| Hairline | 1px `{colors.hairline}` border | Feature cards, code windows |
| Surface step | `{colors.surface-card}` over canvas | Cards, spec cells — no shadow |

**No drop shadows anywhere.** Depth comes from surface-color contrast
(black → near-black → slightly-lighter-black). The contrast is subtle,
engineering-grade, not marketing-elevated.

---

## Layout

### Container
- Max content width: 1200px, centered
- Full-bleed: hero bands, CTA bands, warning panels
- Grid: 12-column, 24px gutters

### Section Rhythm
Alternate between `canvas` and `surface-1` backgrounds. The color change
IS the section divider — no borders between sections.

Pattern: `canvas → surface-1 → canvas → surface-1 → canvas`

### Responsive Breakpoints

| Name | Width | Changes |
|------|-------|---------|
| Mobile | < 768px | Single column; hero 56→36px; stat callouts 56→40px; nav hamburger |
| Tablet | 768–1024px | 2-column grids; nav horizontal |
| Desktop | 1024–1440px | Full layout; 3-column grids |
| Wide | > 1440px | Content locks at 1200px |

### Touch Targets
- Minimum 44×44px on all interactive elements
- Buttons: 48px height, uppercase label, zero radius
- Nav links: 44px min tap area via padding

---

## Components

### Top Navigation
**`top-nav`**
- Background: `{colors.canvas}`
- Height: 64px
- Position: fixed, top 0
- On scroll: `backdrop-filter: blur(12px)`, background → `rgba(10,10,10,0.85)`, 1px bottom hairline
- Left: "AKASHBOARD" wordmark in Archivo 700 16px + "v1.0" badge
- Right: nav links + orange download CTA
- Mobile: hamburger menu, full-screen black overlay

### Hero
**`hero-band`**
- Background: `{colors.canvas}`
- Full viewport height, flex centered
- Content: `<h1>` in `{typography.hero}` (56px/900 Archivo, white, -1.5px tracking)
- Subtitle in `{typography.body-lg}` (18px/300 Inter, `{colors.body}`)
- CTA: orange button ("DOWNLOAD APK") + ghost outline button ("VIEW SOURCE")
- Below: 4-up stat callout row (telemetry, latency, size, dependencies)
- Behind: subtle SVG grid pattern at 3% opacity (circuit-board aesthetic)

### Stat Callouts
**`stat-callout`**
- Transparent background, no card, no shadow
- Number: `{typography.stat-callout}` (56px/700 IBM Plex Mono, `{colors.primary}`)
- Label: `{typography.stat-label}` (14px/500 Archivo uppercase, `{colors.muted}`)
- Arranged 4-up on desktop, 2-up on mobile
- Numbers animate on scroll-reveal via countUp JS

### Feature Cards
**`feature-card-dark`**
- Background: `{colors.surface-card}`
- Border: 1px `{colors.hairline}`
- Radius: `{rounded.none}` (0px)
- Padding: `{spacing.xl}` (32px)
- Icon: orange SVG inline, 24×24
- Title: `{typography.display-sm}` (24px/700 Archivo, white)
- Body: `{typography.body-md}` (16px/400 Inter, `{colors.body}`)
- Arranged 3-up desktop, 2-up tablet, 1-up mobile

### Code Window Card
**`code-window-card`**
- Background: `{colors.surface-code}`
- Border: 1px `{colors.hairline}`
- Radius: `{rounded.none}`
- Top bar: 3 dots (red/yellow/green circles, 8px) + "main.rs" label
- Code: `{typography.code}` (14px/400 IBM Plex Mono)
- Left border accent: 3px `{colors.primary}` (orange)
- Syntax highlighting: orange keywords, white functions, gray comments

### Warning Panel
**`warning-panel`**
- Background: `rgba(245,158,11,0.08)` (warning accent at 8%)
- Left border: 4px `{colors.warning}`
- Radius: `{rounded.none}`
- Body: `{typography.body-md}` in `{colors.body}`
- No hazard stripes — clean, restrained

### CTA Band
**`cta-band`**
- Background: `{colors.primary}` (full orange)
- Text: white, `{typography.display-lg}` (40px/700 Archivo)
- Padding: `{spacing.section}` (96px)
- Black button with white text on orange surface
- Radius: `{rounded.none}`

### Footer
**`footer`**
- Background: `{colors.canvas}`
- Text: `{colors.muted}`
- 4-column link grid (desktop), 2-up (tablet), 1-up (mobile)
- Top: "AKASHBOARD" wordmark
- Bottom: GPLv3 notice, author links
- Padding: 64px vertical
- No radius, no shadow

---

## Motion & Interaction

### Baseline: L2 (Fluid Interaction)

#### Scroll Reveal
```css
.reveal {
  opacity: 0;
  transform: translateY(28px);
  transition: opacity 0.7s cubic-bezier(0.16, 1, 0.3, 1),
              transform 0.7s cubic-bezier(0.16, 1, 0.3, 1);
}
.reveal.in-view {
  opacity: 1;
  transform: translateY(0);
}
```

#### Stagger (children inside `.reveal`)
```css
.reveal.in-view > *:nth-child(1) { transition-delay: 0s; }
.reveal.in-view > *:nth-child(2) { transition-delay: 0.1s; }
.reveal.in-view > *:nth-child(3) { transition-delay: 0.2s; }
.reveal.in-view > *:nth-child(4) { transition-delay: 0.3s; }
```

#### Nav Scroll → Frosted Glass
```css
.nav.scrolled {
  background: rgba(10, 10, 10, 0.85);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--hairline);
}
```

#### Button Press
```css
.btn:active {
  transform: scale(0.95);
}
```

#### Card Hover (glow border, NOT lift/shadow)
```css
.feature-card:hover {
  border-color: var(--primary);
  box-shadow: 0 0 0 1px var(--primary),
              0 0 20px rgba(211, 78, 36, 0.1);
}
```

#### Code Block Scroll Reveal
```css
.code-window-card {
  opacity: 0;
  transform: translateX(-20px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}
.code-window-card.in-view {
  opacity: 1;
  transform: translateX(0);
}
```

### Prefers Reduced Motion
```css
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
    scroll-behavior: auto !important;
  }
}
```

### Focus States
```css
:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}
```

### Scroll Progress Bar
- Fixed top, 3px height, orange, scaleX on scroll
- z-index: 1000

---

## Signature Detail: Spec Callout

The defining visual element. Every key metric gets a massive monospace number
in orange, with an uppercase label below in muted gray. No cards, no
backgrounds, no shadows — just the number and the label, floating on the
canvas. The stat IS the hero.

Layout:
```
     0.00 bytes
  MAX TELEMETRY
```

- Number: 56px / 700 / IBM Plex Mono / `{colors.primary}` / -1.5px tracking
- Label: 14px / 500 / Archivo uppercase / `{colors.muted}` / 1.5px tracking
- Gap between number and label: 8px
- Arranged in a row, evenly spaced

The stat callout row appears below the hero and repeats as section anchors
throughout the page. The numbers are the visual rhythm.

---

## Do's and Don'ts

### Do
- Anchor every page on `{colors.canvas}` (#0A0A0A). The black + orange pairing IS the brand.
- Use `{rounded.none}` (0px) by default on ALL content elements. Zero radius = engineering precision.
- Set stat callouts in `{typography.stat-callout}` (56px/700 IBM Plex Mono) colored `{colors.primary}`. These numbers are the marketing.
- Use `{typography.label-uppercase}` (12px/700 Archivo, 1.5px tracking, uppercase) for ALL labels and category tags.
- Alternate `canvas` and `surface-1` backgrounds between sections. The color change is the divider.
- Use Inter at weight 300 for lead paragraphs and 400 for body. Body is always light.
- Show actual Rust code in `{component.code-window-card}`. The code IS the proof.
- Use `transform: scale(0.95)` as the active/press state on all buttons.
- Keep nav fixed with `backdrop-filter: blur(12px)` on scroll.

### Don't
- Don't use rounded corners on cards, buttons, or inputs. Zero radius is the brand. Only exception: circular icon buttons.
- Don't add drop shadows anywhere. Depth comes from surface-color contrast.
- Don't introduce a second accent color. Orange is the only interactive color.
- Don't bold body type above weight 400. Body stays 300-400; the contrast with heavy display is the signature.
- Don't use centered hero text. Hero is left-aligned.
- Don't use purple, blue gradients, or any gradient backgrounds.
- Don't use generic "Boost your workflow" or "Unlock the power" copy. Every word earns itself.
- Don't skip `prefers-reduced-motion` — all motion has a reduced-motion fallback.
- Don't forget focus-visible states — all interactives must be keyboard-reachable.

---

## Audit Checklist

1. **Generic at a glance?** No — the stat callout pattern (massive monospace numbers) is not a pattern any generic agent ships. The zero-radius geometry is distinctly non-default.
2. **Copy sounds human?** Yes — short, factual, no hedging. "Nothing leaves your phone." not "Experience the future of privacy."
3. **On-product or costume?** On-product — the spec-sheet metaphor matches a keyboard whose selling points ARE its specs.
4. **Holds at payload?** Yes — real content (code blocks, architecture diagram, build instructions) carries the same visual language as the hero.
5. **A11y + motion pass?** Contrast ≥4.5:1 on all text pairs, focus-visible on all interactives, `prefers-reduced-motion` fallback, keyboard-reachable nav.
