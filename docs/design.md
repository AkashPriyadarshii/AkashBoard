---
version: 1.0
name: AkashBoard-site-design-system
description: >
  Marketing design language for AkashBoard, a privacy-first Android keyboard
  with a Rust prediction engine. The site is built from the product's own
  atoms: keycaps. Every surface references mechanical-keyboard construction —
  double-shot legends, press-travel shadows, artisan accent keys. Palette and
  geometry come from keycap-set culture (carbon mods, warm-gray alphas, one
  Serika-taxi-yellow artisan key), not from generic SaaS decoration.

anti-brief:
  - "No cream #F4F1EA + serif + terracotta"
  - "No near-black + acid-mint/acid-green accent"
  - "No broadsheet hairline newspaper layout"
  - "No purple-gradient SaaS hero with floating phone mockup"
  - "No Inter-everywhere typography"

colors:
  carbon: "#17191D"        # page canvas — matches app Canvas family
  carbon-deep: "#101216"   # pressed key underside / terminal card
  slate: "#262A31"         # modifier-keycap face, card surfaces
  slate-lift: "#2C3038"    # keycap top gradient stop
  hairline: "#33383F"      # borders on dark
  alpha: "#E9E7E2"         # light keycap face (contrast moments, sparingly)
  alpha-edge: "#B9B6AE"    # underside of alpha keys
  ink: "#141518"           # legends on alpha keys, text on light
  legend: "#F2F1ED"        # primary text on dark
  mute: "#9BA0AA"          # secondary text on dark
  serika: "#FFCF3F"        # THE accent — artisan key. CTAs, focus, highlights.
                           # One accent only. Never gradients of it.

typography:
  display:
    fontFamily: "Archivo, system-ui, sans-serif"
    usage: headlines, hero. Weight 800–900, tight tracking (-0.03em),
           sentence case. Keycap-legend energy scaled up.
  body:
    fontFamily: "Archivo, system-ui, sans-serif"
    usage: paragraphs. Weight 400/500, line-height 1.6, max-width 62ch.
  mono:
    fontFamily: "'JetBrains Mono', ui-monospace, monospace"
    usage: keycap legends, eyebrows, spec chips, stats, code, timestamps.
           Uppercase for labels, tracking +0.08em. Never for long prose.

geometry:
  keycap-radius: 10px
  keycap-shadow: "0 4px 0 {carbon-deep}, 0 5px 10px rgba(0,0,0,.45)"
  keycap-pressed: "translateY(3px) + 0 1px 0 shadow — physical key travel"
  alpha-shadow: "same recipe with {alpha-edge} underside"
  radius-scale: 10px (keycaps) / 14px (cards) / 999px (chips)
  spacing: 8px base grid; section rhythm 96–128px desktop, 64px mobile

signature:
  element: Live keyboard hero — a real QWERTY rendered in CSS keycaps that
           the visitor can type on (tap or physical keyboard). A suggestion
           strip runs prefix prediction and teh→the autocorrect in-page,
           labeled honestly: demo runs in your browser, nothing sent
           anywhere. The hero IS the product mockup; no fake screenshots.
  supporting: nav is a keycap row ending in one serika artisan key (Download);
              section eyebrows are mono spec-chips stating verifiable facts
              (PERMISSIONS: NONE · ENGINE TESTS: 203); feature cards wear
              keycap glyphs instead of icons.

motion:
  - keycap press travel on hover/active (80ms ease-out) — the core feel
  - hero keyboard staggers in with a single left-to-right key wave on load
  - suggestion strip caret blinks while demo focused
  - prefers-reduced-motion: all travel/wave/blink disabled, opacity only

layout-map:
  nav:       keycap row — logo key, section keys, serika Download key
  hero:      headline → subhead → live demo module → hint line
  privacy:   3 spec cards (NO SOCKET / ON-DEVICE RUST / YOUR MODEL),
             each states a repo-verifiable fact, no marketing adjectives
  features:  6 keycap-glyph cards — swipe, corrections, clipboard,
             emoji, themes, one-handed
  engine:    copy left, terminal card right ($ cargo test … 203 passed;
             model.json path shown as-is)
  install:   3-step numbered sequence (genuine process — numbering earned)
  footer:    blank keycap divider row, GPL-3.0, built in Patna India, links

voice:
  - Plain verbs, sentence case, spec-sheet register. Facts, not adjectives.
  - Every claim must be checkable in the repo (permissions, test counts, paths).
  - Errors/states explained plainly; no apology, no hype.
  - The privacy promise is stated once, mechanically, then proven by demo.

accessibility-floor:
  - All demo keys are real <button> elements with aria-labels
  - Visible focus ring in serika yellow (3px outline + 2px offset)
  - Contrast: legend on carbon ≥ 12:1, mute ≥ 5.5:1, ink on alpha ≥ 13:1
  - Demo operable by keyboard alone (physical keydown mapped to keys)
