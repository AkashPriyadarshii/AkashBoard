---
version: 1.0.0
name: repair-manual-marketing
description: >
  AkashBoard site as a vintage factory service manual. The product is shown as an
  exploded diagram with part numbers; docs are torque specs and procedures.
  Enhanced with maximum SEO, structured metadata, and direct conversion UX.
  BEST FOR: Hardware-grade, offline-first, native Android tools.
---

# AkashBoard Factory Repair Manual + High-Converting UX

## Concept
The site is the OEM service manual for AkashBoard, doubling as a high-conversion marketing landing page. Sections are CHAPTER and SECTION numbered. Exploded diagrams show the architecture as parts. 

Crucially, **the UX removes friction**: a direct "DOWNLOAD APK" button is prominently injected into the manual's navigation structure, allowing users to acquire the software instantly without leaving the fiction or jumping through GitHub release pages.

## SEO & Metadata (Mandatory)
- **Title**: High-intent, exact keywords (`AkashBoard | The Ultimate Offline Android Keyboard`).
- **Meta Description**: Compelling, keyword-rich (offline open-source Android keyboard, Rust engine, zero telemetry, absolute privacy).
- **OpenGraph**: Rich social sharing cards.
- **JSON-LD Schema**: `SoftwareApplication` structured data injected into `<head>` to dominate Google Rich Snippets for Android apps.
- **Semantic HTML**: `<main>`, `<aside>`, `<section>`, proper `<h1>` through `<h3>` hierarchy.

## Colors
- **manual paper**: `#f2efe9` (Base background)
- **age tint**: `#e7e2d6` (Alternate sections/panels)
- **ink**: `#1f2321` (Primary text)
- **procedure gray**: `#565b57` (Secondary text, metadata)
- **warning amber panel**: `#f5c518` at 18% opacity + black hazard stripe
- **part-callout orange**: `#d34e24` (Highlights, part numbers)
- **diagram line**: `#2c4a6e` (SVG diagrams)
- **download button**: Heavy industrial styling, contrasting border, interactive hover state.

## Typography
- **Headings**: `Archivo` (700 weight, condensed)
- **Body**: `Inter` (400 weight)
- **Data/Monospace**: `IBM Plex Mono` (12px, tabular)

## Layout Grammar & Direct Conversion
- **Left Margin Column (200px)**: Holds the Brand, the **Direct Download Button**, and section tabs.
- **Direct Download UX**: 
  - A prominent, high-contrast block button at the top of the sidebar.
  - Skips GitHub releases: links directly to the `.apk` asset.
  - Subtext clearly states file size and requirements (e.g., `2.0 MB • Android 8.0+`).
- **Exploded diagram hero**: SVG of architecture.
- **Procedures**: Boxed step lists.

## Signature Interaction
- **Part Highlighting**: Hovering a part number in text highlights BOTH the diagram part AND its spec-table row.
- **Hazard Stripes**: Animated background stripes for warnings.
- **Tactile Feedback**: Web Audio API mechanical switch clicks on interactive elements (tabs, buttons).
