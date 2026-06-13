# Maneo — Feature Spec & Architecture
> *"Abide in me, and I in you."* — John 15:4

**Platform:** Android (API 26+)  
**Language:** Kotlin  
**UI:** Jetpack Compose  
**License:** MIT (open source)  
**Monetisation:** None. Forever free.

---

## 1. North Star

> The user just wants peace, warmth, and rest.

Every feature decision, every screen, every line of copy should be tested against this. If it adds anxiety, complexity, or guilt — it doesn't belong in Maneo.

---

## 2. Feature List

### V1 — Core (ship this)

| # | Feature | Description |
|---|---------|-------------|
| F1 | App Blocker | Intercept selected apps via `AccessibilityService`. Show verse + prayer screen before user can proceed. |
| F2 | Intercept Screen | Warm, minimal card — one verse (inviting/grounding tone), one short prayer, one "Amen" button. No bypass, no delay, no timer. |
| F3 | App Selection | User picks which apps to block. Simple list of installed apps, toggle each on/off. |
| F4 | Screen Time Warning | After a user-configurable usage threshold (e.g. 30 min on Instagram), show a gentle notification nudge — not a hard block. Uses `UsageStatsManager`. |
| F5 | Daily Verse Notification | One morning notification with a verse. Tapping opens the Prayer Journal. |
| F6 | Scheduled Reminders | 3 daily reminders — morning, afternoon, evening. Each user-adjustable. Each actionable — tapping opens journal with a matching slot prompt. |
| F7 | Prayer Journal | Free-text entry. Optional rotating daily prompt above the text field (dismissible). Entries stored locally with timestamp and slot tag. Viewable as a simple chronological list. |
| F8 | Offline Verse Library | ~300 curated WEB (World English Bible) verses. Tagged by: `morning`, `afternoon`, `evening`, `intercept`. Bundled as a local JSON asset — no network required. |
| F9 | Onboarding | 3 screens: (1) What Maneo is + permission transparency, (2) Grant permissions (Accessibility, Usage Stats, Notifications) with plain-language explanation, (3) Pick your first app to block. |

### V2 — Backlog (don't build yet)

- Streak tracking
- Home screen widget (Jetpack Glance)
- Mood check-in on intercept
- Scheduled blocking by time window (e.g. block Instagram 10pm–7am)

### Permanently out of scope

- AI prayer generation
- Analytics dashboard / charts
- Website / browser blocking
- Social or community features
- Premium themes or any IAP
- iOS

---

## 3. Architecture

### Philosophy
**Modular but not over-engineered.** Each layer has a clear job. Contributors can work on one module without understanding the entire codebase. No unnecessary abstractions — if something is simple, keep it simple.

### Package Structure

```
com.maneo.app
│
├── core/                        # Shared across all features
│   ├── data/
│   │   ├── db/                  # Room database, DAOs
│   │   └── prefs/               # DataStore (user preferences)
│   ├── domain/
│   │   └── model/               # Plain Kotlin data classes (Verse, JournalEntry, ReminderSlot)
│   └── util/                    # Extensions, constants, helpers
│
├── feature/
│   ├── blocker/                 # F1, F2, F3
│   │   ├── service/             # AccessibilityService implementation
│   │   ├── ui/                  # Intercept screen (Compose)
│   │   └── repository/          # Blocked apps list (DataStore)
│   │
│   ├── journal/                 # F7
│   │   ├── ui/                  # Journal list + entry screens (Compose)
│   │   ├── repository/          # JournalRepository (Room)
│   │   └── domain/              # SaveEntry, GetEntries use cases
│   │
│   ├── verse/                   # F5, F8
│   │   ├── assets/              # verses.json (bundled)
│   │   ├── repository/          # VerseRepository (reads from assets)
│   │   └── domain/              # GetVerseForSlot use case
│   │
│   ├── reminders/               # F5, F6
│   │   ├── worker/              # WorkManager workers (one per slot)
│   │   ├── ui/                  # Reminder settings screen (Compose)
│   │   └── repository/          # ReminderRepository (DataStore)
│   │
│   ├── screentime/              # F4
│   │   ├── service/             # UsageStatsManager polling (lightweight)
│   │   └── repository/          # ScreenTimeRepository
│   │
│   └── onboarding/              # F9
│       └── ui/                  # 3-screen onboarding flow (Compose)
│
├── ui/
│   ├── theme/                   # Color, Typography, Shape (Compose theme)
│   ├── components/              # Shared Compose components (ManneoCard, VerseText, etc.)
│   └── navigation/              # NavHost, routes, nav graph
│
└── MainActivity.kt
```

### Why this structure
- Each `feature/` module is self-contained — a contributor working on the journal never needs to touch the blocker
- `core/` holds only what is genuinely shared — models, DB, prefs
- No feature imports another feature directly — they communicate through `core/domain` models only
- Not a multi-module Gradle project (that's over-engineering for v1) — just clean package separation within one module. Can be split into Gradle modules later if the project grows.

---

## 4. Key Technical Decisions

### App Blocking — `AccessibilityService`
- The only Android API that reliably detects foreground app changes from a background service
- Registered in `AndroidManifest.xml` with `android:accessibilityFlags`
- `onAccessibilityEvent` filters for `TYPE_WINDOW_STATE_CHANGED` events only
- Checks event `packageName` against blocked apps list (loaded from DataStore on service start, cached in memory)
- If match found → launches `InterceptActivity` (full-screen, no task animation)
- **Critical:** zero heavy work inside `onAccessibilityEvent`. Just a package name check against an in-memory Set. Fast and battery-safe.

### Screen Time Warning — `UsageStatsManager`
- Queried via a `WorkManager` periodic task (every 15 minutes — Android minimum interval)
- Not a foreground service — no persistent notification, no battery concern
- Compares today's usage for each blocked app against user threshold
- If threshold crossed → fires a single gentle notification for that app (not repeated until next day)
- Requires `PACKAGE_USAGE_STATS` permission (user must grant manually in Settings — explained clearly in onboarding)

### Reminders — `WorkManager`
- One `PeriodicWorkRequest` per slot (morning, afternoon, evening)
- Scheduled with `setInitialDelay` calculated from user's chosen time
- Each worker: picks a verse for its slot from `VerseRepository`, builds a notification with the verse preview, fires it
- On notification tap → deep link into `JournalEntryScreen` with the slot's prompt pre-loaded
- Rescheduled automatically if user changes reminder time in settings

### Verse Library — Local JSON Asset
```json
[
  {
    "id": "v001",
    "reference": "Matthew 11:28",
    "text": "Come to me, all you who are weary and burdened, and I will give you rest.",
    "slots": ["intercept", "morning"],
    "tone": ["inviting"]
  }
]
```
- Loaded once at app start into memory via `VerseRepository`
- `GetVerseForSlot` use case filters by slot, picks pseudo-randomly (seeded by date so same verse doesn't repeat on same day across slots)
- ~300 verses × tagged across slots = large enough pool to avoid repetition for ~3 months

### Local Storage
- **Room** — Journal entries only. Simple schema: `id`, `text`, `prompt_used`, `slot`, `created_at`
- **DataStore (Preferences)** — Everything else: blocked apps list, reminder times, screen time thresholds, onboarding completion flag
- No remote database. No user accounts. No analytics. Nothing leaves the device.

### Dependency Injection
- **Hilt** — Android-standard, well-documented, easy for contributors to understand
- Each feature has its own `@Module` inside its package

---

## 5. Data Models

```kotlin
// core/domain/model/

data class Verse(
    val id: String,
    val reference: String,
    val text: String,
    val slots: List<String>,   // "morning", "afternoon", "evening", "intercept"
    val tone: List<String>     // "inviting", "grounding"
)

data class JournalEntry(
    val id: Long = 0,
    val text: String,
    val promptUsed: String?,   // null if user dismissed the prompt
    val slot: String?,         // "morning" / "afternoon" / "evening" / null (manual entry)
    val createdAt: Long        // epoch millis
)

data class ReminderSlot(
    val slot: String,          // "morning" / "afternoon" / "evening"
    val hour: Int,
    val minute: Int,
    val enabled: Boolean
)
```

---

## 6. Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Onboarding — Welcome | `onboarding/welcome` | What Maneo is. One screen, warm illustration, short copy. |
| Onboarding — Permissions | `onboarding/permissions` | Accessibility, Usage Stats, Notifications. Each explained plainly. |
| Onboarding — First Block | `onboarding/first_block` | Pick one app to block. Just one. |
| Home | `home` | Today's verse. Quick journal entry shortcut. Blocked apps count. Clean, warm. |
| Intercept | `intercept` (Activity, not nav graph) | Full-screen warm card. Verse + prayer + Amen button. Launched by AccessibilityService. |
| Journal List | `journal/list` | Chronological list of past entries. Empty state: *"Your prayers live here."* |
| Journal Entry | `journal/entry` | Optional prompt + free text area + Save button. |
| App Selector | `blocker/apps` | List of installed apps. Toggle to block. Search bar. |
| Reminders | `reminders/settings` | Three time pickers (morning, afternoon, evening). Toggle each on/off. |
| Settings | `settings` | Screen time threshold. Translation (KJV/WEB future). About + GitHub link. Open source licences. |

---

## 7. Visual Identity

**Philosophy:** Warmth, peace, rest. The UI should feel like morning light through a window — never like a productivity dashboard.

| Token | Value | Usage |
|-------|-------|-------|
| `colorBackground` | `#FAF7F2` | All screen backgrounds |
| `colorSurface` | `#F2EDE4` | Cards, bottom sheets |
| `colorPrimary` | `#C8956C` | Buttons, active states, accents |
| `colorOnPrimary` | `#FFFFFF` | Text on primary colour |
| `colorText` | `#3D2C1E` | Body text — warm dark brown, not harsh black |
| `colorTextSecondary` | `#8C7B6E` | Secondary text, timestamps, prompts |
| `colorBorder` | `#E0D6CA` | Subtle dividers |

**Typography:**
- Display / Verse text — `Lora` (serif, warm, readable — fits scripture beautifully)
- Body / UI — `Inter` (clean, modern, universally legible)

**Tone of copy:** Conversational, warm, never preachy. Active voice. Sentence case everywhere. No exclamation marks.

---

## 8. Permissions Required

| Permission | Why | How requested |
|-----------|-----|---------------|
| `BIND_ACCESSIBILITY_SERVICE` | Detect foreground app for blocking | Onboarding step 2 — explained plainly |
| `PACKAGE_USAGE_STATS` | Screen time warning feature | Onboarding step 2 — explained plainly |
| `POST_NOTIFICATIONS` | Reminders and daily verse | Onboarding step 2 — standard Android prompt |
| `RECEIVE_BOOT_COMPLETED` | Restart WorkManager jobs after reboot | Silent — no user action needed |
| `FOREGROUND_SERVICE` | Keep AccessibilityService alive | Silent — persistent notification: "Maneo is active" |

---

## 9. Contribution Guidelines (README summary)

- One feature per PR
- No feature imports another feature's internal classes — use `core/domain` models only
- New verses go in `feature/verse/assets/verses.json` — follow the existing schema
- All copy changes go through a review — tone must match the warm, non-preachy voice
- No analytics, tracking, or network calls without explicit discussion
- Test on API 26 (minimum) and latest Android release

---

## 10. What Maneo is not

- Not a Bible app (YouVersion does that)
- Not a productivity tracker
- Not a guilt machine
- Not a subscription product
- Not a social platform

Maneo is a quiet, warm companion that helps you return to God when your thumb reaches for distraction. That's it. Keep it that way.