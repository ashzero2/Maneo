# Maneo

> *"Abide in me, and I in you."*
> — John 15:4

**maneo** *(Latin, verb)* — to remain, to abide, to stay, to dwell.

It is the word Jesus used in John 15 when he described the relationship between the vine and its branches. Not striving. Not performing. Simply staying close.

That is what this app is about.

---

## What it does

When you reach for Instagram, Twitter, or any app that tends to pull you away from yourself — Maneo catches that moment. Before the feed loads, you see a Bible verse and a short prayer. A breath. Then you can proceed.

It won't stop you. It just invites you to pause.

Beyond the intercept, Maneo offers:

- **Daily reminders** — morning, afternoon, and evening notifications, each with a verse and a link into the prayer journal
- **Prayer journal** — a quiet space to write. A rotating daily prompt appears above the text field, based on the time of day. Dismiss it if you'd rather write freely.
- **Screen time nudges** — after a configurable amount of time spent on a blocked app, a gentle notification asks if you'd like a moment with God before continuing. Once per day, per app. Not a wall — just a question.
- **Home screen** — today's verse, a shortcut into the journal, and a count of the apps you're holding

Everything is local. Nothing leaves your device. No account. No analytics.

---

## The name

*Maneo* in Latin, *μένω* (menō) in Greek — the same word carried across both languages, appearing 118 times in the New Testament. In John 15, Jesus uses it to describe what he wants from his followers: not activity, not achievement — abiding. Staying connected to the source.

The app is built around that same idea. It doesn't gamify your faith or track your progress. It simply tries to keep a thread of connection intact when the pull of the screen would otherwise sever it.

---

## What it is not

- Not a Bible app — [YouVersion](https://www.youversion.com) does that better
- Not a productivity tracker or habit builder
- Not a guilt machine — there are no streaks to break, no scores to lose
- Not a subscription product — free, forever, no in-app purchases
- Not a social platform

---

## Screens

| Screen | Purpose |
|--------|---------|
| Welcome | What Maneo is, in plain language |
| Permissions | Accessibility access, usage stats, notifications — each explained honestly |
| First block | Pick one app to start with. Just one. |
| Home | Today's verse, journal shortcut, blocked apps count |
| Intercept | Full-screen verse + prayer when you open a blocked app |
| Journal list | All past entries, newest first |
| Journal entry | Free-text prayer field with an optional daily prompt |
| App selector | Toggle any installed app on or off |
| Reminders | Set times for morning, afternoon, evening notifications |
| Settings | Screen time threshold, open source licences, GitHub link |

---

## Technical details

**Platform:** Android (API 26+, Oreo and above)  
**Language:** Kotlin  
**UI:** Jetpack Compose  
**License:** MIT

### How app blocking works

Maneo uses Android's `AccessibilityService` — the only Android API that reliably detects which app is in the foreground from a background service. When `onAccessibilityEvent` fires a `TYPE_WINDOW_STATE_CHANGED` event, it checks the package name against an in-memory set of blocked apps. If there's a match, `InterceptActivity` launches immediately. No heavy work happens in the event callback — just a single set lookup.

### How reminders work

Three `WorkManager` periodic workers — one each for morning, afternoon, and evening. Each is scheduled with a calculated `setInitialDelay` so it fires at the user's chosen time. On fire, it picks a verse from the local library for that slot and sends a notification. Tapping the notification deep-links into the journal with the matching slot's prompt pre-loaded.

### How screen time warnings work

A separate `WorkManager` worker runs every 15 minutes (Android's minimum interval). It queries `UsageStatsManager` for today's foreground time on each blocked app. If any app crosses the threshold, a single gentle notification is sent — once per app per day. Nothing runs as a foreground service.

### Verse library

~240 WEB (World English Bible) verses bundled as a local JSON asset — public domain, no network dependency. Each verse is tagged by slot (`morning`, `afternoon`, `evening`, `intercept`) and tone (`inviting`, `grounding`). The daily verse is picked deterministically from the date so the same verse doesn't repeat on the same day.

### Storage

- **Room** — journal entries only
- **DataStore** — everything else (blocked apps, reminder times, thresholds, onboarding state)
- No remote database, no user accounts, no cloud sync

### Dependencies

| Library | Purpose |
|---------|---------|
| Jetpack Compose | UI |
| Dagger / Hilt | Dependency injection |
| AndroidX Room | Journal database |
| AndroidX DataStore | Preferences |
| AndroidX WorkManager | Reminders and screen time polling |
| AndroidX Navigation | In-app navigation |
| kotlinx.serialization | Parsing verse and prayer JSON assets |
| Lora | Serif display font — verse and headline text |
| Inter | Sans-serif body font — UI text |

---

## Permissions

| Permission | Why |
|-----------|-----|
| `BIND_ACCESSIBILITY_SERVICE` | Detect which app is in the foreground |
| `PACKAGE_USAGE_STATS` | Screen time warning feature |
| `POST_NOTIFICATIONS` | Reminders and daily verse |
| `RECEIVE_BOOT_COMPLETED` | Restart WorkManager jobs after device reboot |

The first two require the user to grant them manually in Android Settings. They are explained plainly during onboarding — what they do, what Maneo uses them for, and how to skip them if you'd rather not grant them.

---

## Contributing

One feature per PR. A few things to keep in mind:

- **Tone is everything.** All copy — from button labels to empty states — should be warm, honest, and non-preachy. Sentence case. No exclamation marks. Read what's already there before writing anything new.
- **Features don't import each other.** Each `feature/` package is self-contained. Cross-feature communication goes through `core/domain` models only.
- **New verses** go in `app/src/main/assets/verses.json` — follow the existing schema with `id`, `reference`, `text`, `slots`, and `tone`.
- **No analytics, no network calls, no tracking** — not without explicit discussion in an issue first.
- Test on API 26 (minimum target) and the latest Android release.

### Build

```bash
git clone https://github.com/maneo-app/maneo.git
cd maneo
./gradlew assembleDebug
```

Requires JDK 17 or later and Android Studio Ladybug or newer.

---

## Visual identity

The UI is built to feel like morning light through a window. Warm, unhurried.

| Token | Colour | Usage |
|-------|--------|-------|
| Background | `#FAF7F2` | All screen backgrounds |
| Surface | `#F2EDE4` | Cards and containers |
| Primary | `#C8956C` | Buttons, accents, active states |
| Text | `#3D2C1E` | Body text — warm dark brown, not harsh black |
| Text secondary | `#8C7B6E` | Timestamps, labels, secondary copy |

Typography: **Lora** (serif) for verses and display headings. **Inter** (sans-serif) for all UI text.

---

*"I am the vine; you are the branches. Whoever abides in me and I in him, he it is that bears much fruit, for apart from me you can do nothing."*
— John 15:5
