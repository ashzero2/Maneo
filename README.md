<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" width="100" alt="Maneo" />

<h1>Maneo</h1>

<p><em>maneo</em> &ensp;·&ensp; Latin, verb &ensp;·&ensp; <em>to remain · to abide · to stay · to dwell</em></p>

<p><em>"Abide in me, and I in you. As the branch cannot bear fruit by itself,<br/>unless it abides in the vine, neither can you, unless you abide in me."</em><br/>— John 15:4</p>

[![License: MIT](https://img.shields.io/badge/License-MIT-c8956c?style=flat-square)](LICENSE)
[![Platform](https://img.shields.io/badge/Android-API%2026%2B-3D2C1E?style=flat-square&logo=android&logoColor=c8956c)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-c8956c?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Free](https://img.shields.io/badge/Free-Forever-3D2C1E?style=flat-square)](https://github.com/maneo-app/maneo)

</div>

---

## What Maneo is

When you reach for a distracting app, Maneo catches that moment. Before the feed loads, you see a verse from Scripture and a short prayer. A breath. A chance to remember what you were made for. Then you can proceed — no guilt, no wall.

It won't stop you. It just invites you to pause.

The name comes from the Greek word *μένω* (menō) — the same word Jesus uses in John 15 when he speaks of abiding. Not striving. Not performing. Simply staying close to the vine. It appears 118 times in the New Testament.

---

## Features

### The Intercept
When you open a blocked app, a full-screen verse and prayer appears before you reach the feed. An **Amen** button waits quietly — or you can continue anyway. After ten opens in a day, the tone shifts gently to grounding verses. Everything you choose is counted quietly across the week.

### Daily Reminders
Three moments: morning, afternoon, evening. Each brings a verse matched to the time of day — inviting in the morning, grounding in the evening. Tapping a notification opens the journal with a prompt for that moment.

### Prayer Journal
A quiet place to write. A rotating daily prompt appears above the text field, drawn from the time of day. Dismiss it if you'd rather write freely. In the evening, if you've paused many times that day, it asks gently: *"What were you looking for?"* All entries stay on your device.

### This Week
A formation-framed review in Settings — how many times you paused, how many times you waited, an estimated time spent in reflection. No streaks. No scores. Just a quiet look at how you've been.

### Home Screen
Today's verse, a shortcut to write a prayer, and a count of apps you're holding. When you've been pausing, a gentle note of encouragement appears.

---

## Privacy

Everything stays on your device.

- No account. No sign-in.
- No analytics. No crash reporting.
- No network calls — verses, prayers, and prompts are all bundled locally.
- Journal entries are stored in a local SQLite database, encrypted at rest with a key held in the Android Keystore.
- Export your entire journal any time via the share sheet.

---

## Screens

| Screen | Purpose |
|--------|---------|
| Welcome | What Maneo is, plainly |
| Permissions | Accessibility, usage stats, notifications — each explained honestly |
| First block | Pick one app. Just one. |
| Home | Today's verse, journal shortcut, blocked app count, weekly encouragement |
| Intercept | Full-screen verse + prayer when you open a blocked app |
| Journal list | All entries, newest first |
| Journal entry | Free-text prayer with optional daily prompt |
| App selector | Toggle any installed app on or off |
| Reminders | Morning, afternoon, evening — each adjustable |
| This week | Formation review — pauses, waits, estimated reflection time |
| Settings | Screen time threshold, intercept pause timer, export journal |

---

## Visual Identity

The UI is built to feel like morning light through a window — warm, unhurried.

| Token | Colour | Usage |
|-------|--------|-------|
| Background | `#FAF7F2` | All screen backgrounds |
| Surface | `#F2EDE4` | Cards and containers |
| Primary | `#C8956C` | Buttons, accents, active states |
| Text | `#3D2C1E` | Body text — warm dark brown, not harsh black |
| Text secondary | `#8C7B6E` | Timestamps, labels, secondary copy |

**Typography:** Lora (serif) for verses and display headings &ensp;·&ensp; Nunito Sans (sans-serif) for all UI text.

---

## Technical Details

**Platform:** Android API 26+ (Oreo and above)  
**Language:** Kotlin  
**UI:** Jetpack Compose  
**Architecture:** Single-module, feature-packaged, Hilt DI

### How app blocking works

Maneo uses Android's `AccessibilityService` — the only API that reliably detects which app is in the foreground from a background service. When a `TYPE_WINDOW_STATE_CHANGED` event fires, it does a single in-memory set lookup against blocked packages. If there's a match, `InterceptActivity` launches immediately. The service runs as a foreground service with a low-priority notification for reliability on aggressive OEM battery managers.

### How reminders work

Three `WorkManager` periodic workers — one each for morning, afternoon, and evening — scheduled with a calculated `setInitialDelay` so they fire at the user's chosen time. Each picks a verse by slot and tone, marks it seen in a 14-day rolling de-dup window, and posts a notification. Tapping deep-links into the journal with the matching prompt pre-loaded.

### Verse library

316 WEB (World English Bible) verses bundled as a local JSON asset — public domain, no network dependency. Each verse is tagged by slot (`morning`, `afternoon`, `evening`, `intercept`) and tone (`inviting`, `grounding`). Verses are picked deterministically from the date within a de-duplicated pool, so the same verse doesn't repeat within 14 days.

### Storage

| Store | Contents |
|-------|----------|
| **Room** (SQLite) | Journal entries, encrypted at rest via Android Keystore |
| **DataStore** | Blocked apps, reminder times, thresholds, seen-verse windows, weekly counts, onboarding state |

### Dependencies

| Library | Purpose |
|---------|---------|
| Jetpack Compose | UI |
| Dagger / Hilt | Dependency injection |
| AndroidX Room | Journal database |
| AndroidX DataStore | Preferences |
| AndroidX WorkManager | Reminders and screen time polling |
| kotlinx.serialization | Local JSON assets |

---

## Permissions

| Permission | Why |
|-----------|-----|
| `BIND_ACCESSIBILITY_SERVICE` | Detect which app is in the foreground |
| `PACKAGE_USAGE_STATS` | Screen time nudge feature |
| `POST_NOTIFICATIONS` | Reminders and daily verse |
| `RECEIVE_BOOT_COMPLETED` | Restart WorkManager jobs after reboot |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Keep the accessibility service alive on aggressive OEMs |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Optional — prevents the service being killed overnight |

The first two require manual grant in Android Settings. Each is explained plainly during onboarding — what it does, what Maneo uses it for, and how to skip it if you'd rather not grant it.

---

## Build

```bash
git clone https://github.com/maneo-app/maneo.git
cd maneo
./gradlew assembleDebug
```

Requires **JDK 17** or later and **Android Studio Meerkat** or newer.

---

## Contributing

One feature per PR. A few things worth keeping in mind:

- **Tone is everything.** All copy — button labels, empty states, notifications — should be warm, honest, and non-preachy. Sentence case. No exclamation marks. Read what's already there before writing anything new.
- **Features don't import each other.** Each `feature/` package is self-contained. Cross-feature communication goes through `core/domain` models only.
- **New verses** go in `app/src/main/assets/verses.json` — follow the existing schema: `id`, `reference`, `text`, `slots`, `tone`.
- **No analytics, no network calls, no tracking** — not without explicit discussion in an issue first.
- Test on API 26 (minimum) and the latest Android release.

---

## What Maneo is not

- Not a Bible app — [YouVersion](https://www.youversion.com) does that better
- Not a productivity tracker or habit builder
- Not a guilt machine — no streaks to break, no scores to lose
- Not a subscription product — free, forever, no in-app purchases
- Not a social platform

---

<div align="center">

*"I am the vine; you are the branches.*<br/>
*Whoever abides in me and I in him, he it is that bears much fruit,*<br/>
*for apart from me you can do nothing."*

**— John 15:5**

</div>
