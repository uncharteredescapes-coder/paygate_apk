# DESIGN.md — MFS Gateway Design System

Shared source of truth for the **SaaS web dashboard / hosted checkout** and the **Android app**.
Version 1.0 · Dark-first · Minimal · Derived from the app icon (`icon-v2.svg`).

---

## 1. Principles

1. **Quiet by default.** Near-black canvas, white type, one accent. Color means something (money, status), never decoration.
2. **Money is the hero.** Amounts and TrxIDs get the largest, clearest, most legible treatment on every screen.
3. **Trust through clarity.** One primary action per screen, explicit status, plain-language errors, no surprises.
4. **One system, two platforms.** Same tokens, same names, same behavior on web and Android. Platform idioms (Material 3 on Android) may change *form*, never *meaning*.
5. **Fast and light.** Hairline borders over shadows, no gradients, motion only to explain state change.

---

## 2. Brand foundation (from the icon)

| Icon element | Meaning | Design-system role |
|---|---|---|
| Near-black tile `#0A0A0A` | Calm, secure canvas | `bg` / launcher background |
| White arch `#F8FAFC` | The gateway | Primary text/foreground, structural strokes |
| Green coin `#10B981` | A transaction passing through | The single accent: primary actions, success, money-in |
| Rounded stroke ends (36/512 weight) | Friendly precision | Icon stroke style, rounded corners, pill shapes |

**Logo usage**
- Clear space: ≥ 25% of icon width on all sides.
- Minimum size: 24 px (web), 24 dp (app).
- Only on `#0A0A0A`, white, or `#FAFAFA`. Never recolor the coin; never add shadows, gradients, or outlines.
- On light backgrounds use the icon tile as-is (dark tile on light page), not the arch alone.

---

## 3. Color

### 3.1 Palettes

**Neutral** (pure gray, no blue tint, so the near-black stays near-black)

| Token | Hex | Use |
|---|---|---|
| `neutral-950` | `#0A0A0A` | Dark canvas, text on emerald |
| `neutral-900` | `#111111` | Dark surface |
| `neutral-850` | `#171717` | Dark raised surface |
| `neutral-800` | `#1F1F1F` | Dark hover / subtle fill |
| `neutral-700` | `#262626` | Dark border |
| `neutral-600` | `#333333` | Dark strong border |
| `neutral-500` | `#8A8A8A` | Subtle text (dark theme) |
| `neutral-400` | `#A3A3A3` | Muted text (dark theme) |
| `neutral-300` | `#D4D4D4` | Light strong border |
| `neutral-200` | `#E5E5E5` | Light border |
| `neutral-100` | `#F0F0F0` | Light subtle fill |
| `neutral-50`  | `#FAFAFA` | Light canvas |
| `white-icon`  | `#F8FAFC` | Primary text on dark (matches the arch) |

**Emerald** (the coin)

| Token | Hex |
|---|---|
| `emerald-50` | `#ECFDF5` |
| `emerald-100` | `#D1FAE5` |
| `emerald-200` | `#A7F3D0` |
| `emerald-300` | `#6EE7B7` |
| `emerald-400` | `#34D399` |
| **`emerald-500`** | **`#10B981`** ← brand accent |
| `emerald-600` | `#059669` |
| `emerald-700` | `#047857` |
| `emerald-800` | `#065F46` |
| `emerald-900` | `#064E3B` |
| `emerald-950` | `#022C22` |

**Status**

| Token | Hex | Meaning |
|---|---|---|
| `amber-500` | `#F59E0B` | Pending / waiting |
| `amber-700` | `#B45309` | Pending text on light |
| `red-500` | `#EF4444` | Failed / destructive |
| `red-700` | `#B91C1C` | Error text on light |

There is **no blue** in the system. Informational states use neutral.

### 3.2 Semantic tokens (use these in code, never raw hex)

| Token | Dark (default) | Light |
|---|---|---|
| `--bg` | `#0A0A0A` | `#FAFAFA` |
| `--surface` | `#111111` | `#FFFFFF` |
| `--surface-raised` | `#171717` | `#FFFFFF` |
| `--surface-hover` | `#1F1F1F` | `#F0F0F0` |
| `--border` | `#262626` | `#E5E5E5` |
| `--border-strong` | `#333333` | `#D4D4D4` |
| `--text` | `#F8FAFC` | `#0A0A0A` |
| `--text-muted` | `#A3A3A3` | `#525252` |
| `--text-subtle` | `#8A8A8A` | `#737373` |
| `--accent` | `#10B981` | `#10B981` |
| `--accent-hover` | `#34D399` | `#059669` |
| `--accent-pressed` | `#059669` | `#047857` |
| `--on-accent` | `#0A0A0A` | `#0A0A0A` |
| `--accent-text` | `#34D399` | `#047857` |
| `--accent-subtle` | `#022C22` | `#ECFDF5` |
| `--success` | `#10B981` | `#047857` |
| `--warning` | `#F59E0B` | `#B45309` |
| `--danger` | `#EF4444` | `#B91C1C` |
| `--focus-ring` | `#34D399` | `#059669` |
| `--overlay` | `rgba(0,0,0,.6)` | `rgba(10,10,10,.4)` |

### 3.3 Rules

- **Text on emerald is always `#0A0A0A`, never white.** White on `#10B981` is only ~2.5:1 and fails WCAG; near-black on emerald is ~7.8:1.
- **Emerald as text on light backgrounds uses `emerald-700`** (`--accent-text`), not `emerald-500` (fails contrast).
- One accent-filled element (primary button) per view. Everything else is neutral.
- Status is never color-only: always pair color with an icon and a label.
- Dark is the brand expression and the fallback when the system has no preference. Light is a full, equal-quality theme. Both follow the system setting, with a manual override.

### 3.4 Contrast (verified targets)

| Pair | Ratio | Pass |
|---|---|---|
| `--text` on `--bg` (dark) | ≈ 18:1 | AAA |
| `--text-muted` on `--bg` (dark) | ≈ 7.9:1 | AAA |
| `--text-subtle` on `--bg` (dark) | ≈ 5.7:1 | AA |
| `--on-accent` on `#10B981` | ≈ 7.8:1 | AAA |
| `#10B981` on `#0A0A0A` | ≈ 7.8:1 | AAA |
| `emerald-700` on `#FFFFFF` | ≈ 5.5:1 | AA |
| `--text-subtle` on `#FFFFFF` | ≈ 4.7:1 | AA |

### 3.5 Wallet provider marks

Provider colors appear **only** inside the small provider chip/logo, never as page or button color. Use the providers' official logo assets and confirm values against each provider's current brand guidelines (hex below is approximate).

| Provider | Approx. brand color |
|---|---|
| bKash | `#E2136E` |
| Nagad | `#F6921E` |
| Rocket | `#8C3494` |

---

## 4. Typography

| Role | Latin | Bangla | Mono |
|---|---|---|---|
| UI / body | **Inter** | **Hind Siliguri** (alt: Noto Sans Bengali) | — |
| TrxIDs, API keys, codes | — | — | **JetBrains Mono** (alt: Geist Mono) |

Android: bundled in `res/font` / Google Fonts downloadable. Web: self-host, `font-display: swap`.

### Scale (web / app)

| Style | Size / Line | Weight | Tracking | Use |
|---|---|---|---|---|
| `amount-xl` | 40 / 48 | 600 | -0.02em | Checkout amount, balance |
| `display` | 32 / 40 | 600 | -0.02em | Page hero |
| `h1` | 24 / 32 | 600 | -0.01em | Screen title |
| `h2` | 20 / 28 | 600 | -0.01em | Section title |
| `h3` | 16 / 24 | 600 | 0 | Card title |
| `body` | 16 / 24 (app) · 14 / 20 (dense web) | 400 | 0 | Default text |
| `body-strong` | same | 500 | 0 | Labels, table headers |
| `caption` | 12 / 16 | 400–500 | 0.01em | Helper text, timestamps |
| `mono` | 14 / 20 | 500 | 0.02em | TrxID, keys |

Rules
- **Numbers:** always `font-variant-numeric: tabular-nums` (Compose: `fontFeatureSettings = "tnum"`) for amounts, tables, timers.
- **Bangla:** add ~10% line-height; never use letter-spacing on Bangla text; no italics.
- Max line length 68 characters for prose. Sentence case everywhere (no ALL CAPS except `TrxID`-style codes).
- Minimum text size 12; interactive text 14+.

---

## 5. Spacing, layout, grid

**4-pt base.** Allowed steps: `0, 4, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80, 96`.

| Context | Rule |
|---|---|
| Component inner padding | 12–16 |
| Card padding | 20 (web) / 16 (app) |
| Gap between related items | 8–12 |
| Gap between sections | 32–48 |
| Screen edge margin | 16 (app) · 24 (tablet) · 32+ (desktop) |

**Touch targets:** ≥ 44×44 px web, ≥ 48×48 dp Android.

---

## 6. Shape, borders, elevation

| Token | Value | Use |
|---|---|---|
| `radius-xs` | 6 | Chips inside inputs, code tags |
| `radius-sm` | 8 | Badges, small buttons |
| `radius-md` | 10 | Buttons, inputs |
| `radius-lg` | 14 | Cards |
| `radius-xl` | 20 | Dialogs, bottom sheets |
| `radius-full` | 999 | Status pills, avatars, switches |

- **Borders over shadows.** 1 px `--border` defines surfaces in both themes.
- **Dark theme:** no shadows. Elevation = lighter surface (`--surface` → `--surface-raised`).
- **Light theme:** one soft shadow for floating layers only.