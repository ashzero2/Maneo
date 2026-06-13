# Maneo — Implementation Plan

> Every decision in this document is verified against `ManeoSpec.md`.
> The check is always: **does this serve peace, warmth, and rest — or does it introduce anxiety, complexity, or guilt?**

---

## 0. Governing Constraints

| Constraint | Source |
|---|---|
| Android API 26+ only | Spec §2 header |
| Kotlin + Jetpack Compose | Spec §2 header |
| No network calls, no accounts, no analytics | Spec §4, §10 |
| No feature imports another feature's internals | Spec §3 |
| Single Gradle module (not multi-module) | Spec §3 "Why this structure" |
| MIT licence | Spec §2 header |

---

## 1. Tech Stack & Dependencies

### `build.gradle.kts` (app)

```
// Compose
implementation(platform("androidx.compose:compose-bom:2026.05.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.13.0")
implementation("androidx.navigation:navigation-compose:2.9.8")

// DI — Hilt (Spec §4)
implementation("com.google.dagger:hilt-android:2.59.2")
ksp("com.google.dagger:hilt-compiler:2.59.2")
implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
implementation("androidx.hilt:hilt-work:1.3.0")
ksp("androidx.hilt:hilt-compiler:1.3.0")

// Room — journal only (Spec §4 "Local Storage")
implementation("androidx.room:room-runtime:2.8.4")
implementation("androidx.room:room-ktx:2.8.4")
ksp("androidx.room:room-compiler:2.8.4")

// DataStore — everything except journal (Spec §4 "Local Storage")
implementation("androidx.datastore:datastore-preferences:1.2.1")

// WorkManager — reminders + screen time polling (Spec §4)
implementation("androidx.work:work-runtime-ktx:2.11.2")

// Typography — Lora + Inter bundled as TTF in res/font/ (Spec §7)
// ui-text-google-fonts NOT used — bundled TTFs work offline from first launch, no GMS dependency

// Kotlin
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
```

### `build.gradle.kts` (project-level)

```
plugins {
    id("com.android.application") version "9.2.1" apply false
    // NOTE: org.jetbrains.kotlin.android is NOT needed — AGP 9.0+ bundles Kotlin support directly
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21" apply false
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
    id("com.google.devtools.ksp") version "2.3.9" apply false
}
```

### `build.gradle.kts` (app — android block)

```
android {
    compileSdk = 36

    defaultConfig {
        minSdk = 26        // Spec §2 — API 26+ only
        targetSdk = 36
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // NOTE: kotlinOptions { jvmTarget } is removed in AGP 9.x — use compileOptions only
}
```

### AGP 9.x compatibility notes (verified against actual build)

| Issue | Resolution |
|---|---|
| `org.jetbrains.kotlin.android` plugin → error in AGP 9.0+ | Removed — AGP 9+ bundles Kotlin |
| `kotlinOptions { jvmTarget }` → unresolved reference | Removed — use `compileOptions` only |
| Hilt 2.59.2 max Kotlin metadata = 2.3.0 | Use Kotlin **2.3.21** (not 2.4.0) |
| AGP 9.2.1 minimum Gradle = 9.4.1 | Wrapper set to `gradle-9.4.1-bin.zip` |
| Hilt < 2.59.0 → "Android BaseExtension not found" in AGP 9 | Use Hilt **2.59.2** |

**Why `kotlinx-serialization` over Gson/Moshi:** no reflection, fast, Kotlin-native. Used exclusively to parse `verses.json` at startup.

**Why KSP over KAPT:** KSP is faster, officially recommended for Room and Hilt. KSP moved to simple semantic versioning (2.3.x+) from 2.3.6 onwards — the old `{kotlin}-{ksp}` format was dropped.

---

## 2. Package Structure

Mirrors `ManeoSpec.md §3` exactly. Create these packages before writing any feature code.

```
com.maneo.app
│
├── core/
│   ├── data/
│   │   ├── db/                  # ManeoDatabase, DAOs
│   │   └── prefs/               # DataStore keys + accessor
│   ├── domain/
│   │   └── model/               # Verse, JournalEntry, ReminderSlot
│   └── util/                    # Extensions, constants
│
├── feature/
│   ├── blocker/
│   │   ├── service/             # ManeoAccessibilityService
│   │   ├── ui/                  # InterceptActivity + InterceptScreen composable
│   │   └── repository/          # BlockedAppsRepository (DataStore)
│   │
│   ├── journal/
│   │   ├── ui/                  # JournalListScreen, JournalEntryScreen
│   │   ├── repository/          # JournalRepository (Room)
│   │   └── domain/              # SaveEntry, GetEntries use cases
│   │
│   ├── verse/
│   │   ├── assets/              # verses.json
│   │   ├── repository/          # VerseRepository
│   │   └── domain/              # GetVerseForSlot
│   │
│   ├── reminders/
│   │   ├── worker/              # MorningWorker, AfternoonWorker, EveningWorker
│   │   ├── ui/                  # RemindersSettingsScreen
│   │   └── repository/          # ReminderRepository (DataStore)
│   │
│   ├── screentime/
│   │   ├── service/             # ScreenTimeCheckWorker (WorkManager, NOT a Service)
│   │   └── repository/          # ScreenTimeRepository (DataStore for thresholds + last-notified)
│   │
│   └── onboarding/
│       └── ui/                  # WelcomeScreen, PermissionsScreen, FirstBlockScreen
│
├── ui/
│   ├── theme/                   # ManeoTheme, Color.kt, Type.kt, Shape.kt
│   ├── components/              # ManeoCard, VerseText, PrimaryButton
│   └── navigation/              # NavGraph, Routes, AppNavHost
│
└── MainActivity.kt
```

---

## 3. Build Phases

### Phase 0 — Project Scaffolding
*No features. Just the skeleton that all phases build on.*

**Tasks:**
1. Create Android project (Empty Activity, API 26, Kotlin, Compose)
2. Apply all dependencies from §1
3. Create all packages from §2 (empty files with package declarations are fine)
4. Implement `ManeoTheme` with exact spec tokens:

```kotlin
// ui/theme/Color.kt
val Background   = Color(0xFFFAF7F2)
val Surface      = Color(0xFFF2EDE4)
val Primary      = Color(0xFFC8956C)
val OnPrimary    = Color(0xFFFFFFFF)
val TextPrimary  = Color(0xFF3D2C1E)
val TextSecondary= Color(0xFF8C7B6E)
val Border       = Color(0xFFE0D6CA)
```

```kotlin
// ui/theme/Type.kt
// Lora for display/verse text — loaded via GoogleFont
// Inter for all body/UI text — loaded via GoogleFont
val Lora  = FontFamily(GoogleFont("Lora"), ...)
val Inter = FontFamily(GoogleFont("Inter"), ...)
```

5. Wire `ManeoTheme` into `MainActivity`
6. Set up `@HiltAndroidApp` on `ManeoApplication`, register in `AndroidManifest.xml`
7. Add `ManeoApplication` to manifest

**Definition of done:** App builds and launches to an empty themed screen. No crashes.

---

### Phase 1 — Core Models + Storage

*No UI. Pure data layer.*

**Tasks:**

**1a. Domain models** (`core/domain/model/`) — copy exactly from Spec §5:
- `Verse.kt`
- `JournalEntry.kt`
- `ReminderSlot.kt`

**1b. Room — Journal only** (`core/data/db/`):
```kotlin
@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val promptUsed: String?,
    val slot: String?,
    val createdAt: Long
)

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAll(): Flow<List<JournalEntryEntity>>

    @Insert
    suspend fun insert(entry: JournalEntryEntity): Long
}

@Database(entities = [JournalEntryEntity::class], version = 1)
abstract class ManeoDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
}
```

**1c. DataStore keys** (`core/data/prefs/PrefsKeys.kt`):
```kotlin
object PrefsKeys {
    val BLOCKED_APPS        = stringSetPreferencesKey("blocked_apps")
    val ONBOARDING_DONE     = booleanPreferencesKey("onboarding_done")
    val SCREEN_TIME_THRESHOLD_MINS = intPreferencesKey("screen_time_threshold_mins")

    // Reminder slots — each stores "HH:mm" string + enabled bool
    val MORNING_TIME        = stringPreferencesKey("reminder_morning_time")
    val MORNING_ENABLED     = booleanPreferencesKey("reminder_morning_enabled")
    val AFTERNOON_TIME      = stringPreferencesKey("reminder_afternoon_time")
    val AFTERNOON_ENABLED   = booleanPreferencesKey("reminder_afternoon_enabled")
    val EVENING_TIME        = stringPreferencesKey("reminder_evening_time")
    val EVENING_ENABLED     = booleanPreferencesKey("reminder_evening_enabled")

    // Screen time: stores last-notified date per package ("pkg:date" set)
    val SCREEN_TIME_NOTIFIED = stringSetPreferencesKey("screen_time_notified")
}
```

**1d. Hilt modules** — one `@Module` per feature, provide `ManeoDatabase`, `DataStore<Preferences>` from `core/`.

**Definition of done:** Unit tests pass for Room insert/query. DataStore reads/writes correctly in an instrumented test.

---

### Phase 2 — Verse Library (F8)

*Self-contained. No permissions needed. Ship this first.*

**Tasks:**

**2a. `verses.json`** — place at `feature/verse/assets/verses.json` (copy to `src/main/assets/`).

Schema (from Spec §4):
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

**Slot distribution target (~300 total):**
| Slot | Count | Notes |
|---|---|---|
| `intercept` | 120 | Must be inviting/grounding — not accusatory |
| `morning` | 80 | Hope, new mercies, beginning |
| `afternoon` | 60 | Steadfastness, peace, rest in the day |
| `evening` | 80 | Rest, gratitude, surrender |

Verses can appear in multiple slots. All text from WEB (World English Bible — public domain).

**2b. `VerseRepository`** (`feature/verse/repository/`):
```kotlin
// Loads verses.json once on init, keeps in memory
// No network. No coroutine needed for memory reads.
class VerseRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val verses: List<Verse> by lazy { loadFromAssets() }

    fun getVersesForSlot(slot: String): List<Verse> =
        verses.filter { slot in it.slots }
}
```

**2c. `GetVerseForSlot` use case** (`feature/verse/domain/`):
```kotlin
// Seeded by date so the same verse doesn't repeat on the same day across slots.
// No state persisted — deterministic from (slot + date).
class GetVerseForSlot @Inject constructor(
    private val repo: VerseRepository
) {
    operator fun invoke(slot: String, date: LocalDate = LocalDate.now()): Verse {
        val pool = repo.getVersesForSlot(slot)
        val seed = date.toEpochDay() * 31 + slot.hashCode()
        return pool[abs(seed.toInt()) % pool.size]
    }
}
```

**Definition of done:** `GetVerseForSlot("intercept")` returns a verse. Same call on the same day always returns the same verse. Different slots return different verses on the same day.

---

### Phase 3 — Intercept Screen (F2)

*The heart of the app. Build before wiring the AccessibilityService so you can test it standalone.*

**Design rules from spec:**
- Full-screen warm card
- One verse (inviting/grounding tone)
- One short prayer (hardcoded per verse, or a small fixed set of ~10 prayers rotated by date)
- One "Amen" button — taps it → finishes the Activity (user proceeds to their app)
- **No bypass. No countdown timer. No delay.**

**Tasks:**

**3a. `InterceptActivity`** — standalone Activity, not in the nav graph:
```kotlin
// AndroidManifest flags:
// android:theme="@style/Theme.Maneo.Fullscreen"
// android:excludeFromRecents="true"
// android:taskAffinity="" — separate task so Back doesn't expose it in recents
// android:showWhenLocked="true" — visible over lock screen if needed
@AndroidEntryPoint
class InterceptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ManeoTheme {
                InterceptScreen(onAmen = { finish() })
            }
        }
    }
}
```

**3b. `InterceptScreen` composable:**
```
Layout:
- Full background: colorBackground (#FAF7F2)
- Centered column, generous vertical padding
- Verse reference (small, Inter, colorTextSecondary)
- Verse text (Lora, 22sp, colorText, centered, max 4 lines)
- Spacer
- Prayer text (Inter, 15sp, colorTextSecondary, italic, centered)
- Spacer
- "Amen" button (filled, colorPrimary, full width, rounded corners)
```

Prayers: add a `prayers` field to the verse JSON, or maintain a separate small `prayers.json` (~20 short prayers, rotated by date independent of verse). Keeping them separate keeps the verse schema clean.

**3c. Prayer rotation** — same seeding approach as verses:
```kotlin
// prayers.json — ~20 entries, each 1–2 sentences, WEB-style language
// Rotated by date, independent of verse slot
```

**Spec check:** No timer, no bypass, no guilt-inducing copy. The Amen button does not close the blocked app — it simply closes the InterceptActivity, returning the user to wherever they were (which is the blocked app). This is intentional per the spec — no hard block.

**Definition of done:** Launch `InterceptActivity` directly via adb. Verse shows. Tapping Amen closes the screen. Works without AccessibilityService running.

---

### Phase 4 — App Blocker (F1 + F3)

*This phase requires the AccessibilityService permission. Test on a real device.*

**Tasks:**

**4a. `ManeoAccessibilityService`** (`feature/blocker/service/`):
```kotlin
@AndroidEntryPoint
class ManeoAccessibilityService : AccessibilityService() {

    // Loaded once at service start, refreshed when DataStore updates
    private var blockedPackages: Set<String> = emptySet()

    override fun onServiceConnected() {
        // Subscribe to BlockedAppsRepository flow
        // Update blockedPackages in memory whenever DataStore changes
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return  // don't intercept ourselves
        if (pkg in blockedPackages) {
            launchInterceptActivity()
        }
    }

    private fun launchInterceptActivity() {
        val intent = Intent(this, InterceptActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
```

**Critical constraint from spec:** Zero heavy work in `onAccessibilityEvent`. The `blockedPackages` Set must already be in memory. No DB calls, no coroutine launches, no DataStore reads — just `pkg in blockedPackages`.

**4b. Manifest registration:**
```xml
<service
    android:name=".feature.blocker.service.ManeoAccessibilityService"
    android:exported="true"
    android:label="@string/accessibility_service_label"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

```xml
<!-- res/xml/accessibility_service_config.xml -->
<accessibility-service
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:canRetrieveWindowContent="false"
    android:description="@string/accessibility_description" />
```

`canRetrieveWindowContent="false"` — we only need the package name, not content. This reduces the permission footprint and is more honest with users.

**4c. `BlockedAppsRepository`** (`feature/blocker/repository/`):
```kotlin
// Reads/writes blocked apps Set to DataStore (PrefsKeys.BLOCKED_APPS)
// Exposes Flow<Set<String>> for the service to observe
```

**4d. App Selector screen** (`feature/blocker/ui/AppSelectorScreen`):
```
- Query PackageManager for all installed, launchable apps
- Exclude system/launcher apps
- Show: app icon, app name, toggle
- Search bar at top (filters by name)
- Sort: checked apps first, then alpha
- No "block all" or "unblock all" shortcuts — spec says simple
```

**OEM battery kill risk mitigation:** In onboarding and settings, link directly to Settings → Battery → Special app access → Unrestricted for Maneo. Add a plain-language note: "Some phones turn off background features to save battery. Allow Maneo to run in the background to keep blocking working." Don't use scary language — keep it warm.

**Definition of done:** Block Instagram. Open Instagram on the device. InterceptScreen appears. Tap Amen. Instagram opens. Unblock Instagram. Open Instagram. No intercept.

---

### Phase 5 — Prayer Journal (F7)

*Pure local CRUD. No permissions needed beyond what Phase 0 established.*

**Tasks:**

**5a. `JournalRepository`** (`feature/journal/repository/`):
```kotlin
class JournalRepository @Inject constructor(private val dao: JournalDao) {
    fun getEntries(): Flow<List<JournalEntry>> = dao.getAll().map { it.map(::toDomain) }
    suspend fun saveEntry(entry: JournalEntry) = dao.insert(entry.toEntity())
}
```

**5b. Use cases** (`feature/journal/domain/`):
- `SaveEntry` — validates text is not blank, sets `createdAt = System.currentTimeMillis()`
- `GetEntries` — returns `Flow<List<JournalEntry>>`

**5c. `JournalListScreen`:**
```
- Chronological list (newest first)
- Each row: timestamp (relative: "this morning", "yesterday", "Jun 3"), first ~80 chars of text
- Empty state: "Your prayers live here." (from spec §6)
- FAB: pen icon → JournalEntryScreen
- No delete in v1 (not in spec — don't add it)
```

**5d. `JournalEntryScreen`:**
```
- Optional prompt displayed above the text field
  - If arrived via reminder notification deep link: show the slot's prompt
  - If arrived via FAB: show a random general prompt (or none)
  - Dismissible — tapping "×" hides the prompt (sets promptUsed = null)
- Multi-line free text area (fills available space)
- "Save" button at bottom
- On save: navigate back to list
- No character limit, no word count, no formatting toolbar
```

**Prompts** — a small set per slot (10–15 each), rotated by date. Store in a `prompts.json` asset alongside `verses.json`:
```json
{
  "morning": ["What are you carrying into this day?", ...],
  "afternoon": ["Where have you felt God's presence today?", ...],
  "evening": ["What are you releasing before you sleep?", ...]
}
```

**Spec check:** Free text only. No mood emoji, no categories, no tags. Those are V2 backlog at best. Local only — nothing leaves the device.

**Definition of done:** Write an entry. It appears in the list. Re-open the app — entry persists.

---

### Phase 6 — Reminders + Daily Verse Notification (F5 + F6)

*WorkManager-based. Test on device — emulator WorkManager timing can be unreliable.*

**Tasks:**

**6a. One Worker per slot** (`feature/reminders/worker/`):

```kotlin
@HiltWorker
class MorningReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getVerseForSlot: GetVerseForSlot
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val verse = getVerseForSlot("morning")
        NotificationHelper.sendReminderNotification(
            context = applicationContext,
            title = "Good morning",
            body = verse.text.take(80) + "…",
            slot = "morning"
        )
        return Result.success()
    }
}
```

Same pattern for `AfternoonReminderWorker` and `EveningReminderWorker`.

**6b. Scheduling logic** (`feature/reminders/repository/ReminderRepository`):
```kotlin
// For each slot: cancel existing WorkRequest by tag, then enqueue new PeriodicWorkRequest
// Period: 24 hours
// setInitialDelay: calculated from (user's chosen time - now), handling next-day wraparound
// Tag: "reminder_morning", "reminder_afternoon", "reminder_evening"

// Also enqueue a BOOT_COMPLETED broadcast receiver that calls rescheduleAll()
// to handle device restarts (spec §8 RECEIVE_BOOT_COMPLETED)
```

**6c. `ReminderSettingsScreen`:**
```
- Three rows: Morning / Afternoon / Evening
- Each row: label, time picker (Material3 TimePickerDialog), on/off toggle
- Changing time or toggling calls ReminderRepository to reschedule
- Default times: Morning 7:00am, Afternoon 12:30pm, Evening 8:00pm
```

**6d. Notification deep link:** Tapping a reminder notification → opens `JournalEntryScreen` with slot pre-loaded. Use a `NavDeepLink` on the journal entry route, triggered from the notification's `PendingIntent`.

**6e. Daily Verse Notification (F5):** This is the morning reminder with a slight copy difference. Tapping it opens the journal (same deep link). No separate worker needed — `MorningReminderWorker` handles both.

**Spec check on F5:** "Tapping opens the Prayer Journal" — confirmed, the deep link does exactly this.

**Definition of done:** Set morning reminder to 2 minutes from now. Notification fires. Verse text visible in notification. Tapping opens Journal entry screen with morning prompt.

---

### Phase 7 — Screen Time Warning (F4)

*Gentle notification, not a hard block. Uses `UsageStatsManager`. Requires manual permission grant.*

**Tasks:**

**7a. `ScreenTimeCheckWorker`** (`feature/screentime/service/`):
```kotlin
@HiltWorker
class ScreenTimeCheckWorker @AssistedInject constructor(...) : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        val threshold = prefsRepo.getScreenTimeThresholdMins()  // default: 30
        val blockedApps = blockedAppsRepo.getBlockedApps()
        val today = LocalDate.now()

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfToday(), System.currentTimeMillis()
        )

        for (app in blockedApps) {
            val usedMs = usageStats.find { it.packageName == app }?.totalTimeInForeground ?: continue
            val usedMins = usedMs / 60_000
            if (usedMins >= threshold && !alreadyNotifiedToday(app, today)) {
                sendScreenTimeWarning(app, usedMins)
                markNotifiedToday(app, today)
            }
        }
        return Result.success()
    }
}
```

**7b. Enqueue as `PeriodicWorkRequest`** at app start (or after onboarding):
```kotlin
// 15-minute interval — Android minimum
// ExistingPeriodicWorkPolicy.KEEP so it's not re-enqueued on every launch
```

**7c. "Already notified today" state** — stored as a `Set<String>` in DataStore (`PrefsKeys.SCREEN_TIME_NOTIFIED`). Each entry is `"$packageName:$date"`. Cleared automatically when the date changes (checked on read).

**7d. Notification copy:**
```
Title: "You've spent {X} minutes on {App Name}"
Body:  "Maybe a moment with God before you continue?"
```
Warm, never preachy. Not "you're addicted" — just a gentle nudge.

**7e. Permission check:** Before calling `queryUsageStats`, check `AppOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, ...)`. If denied, skip silently — never crash, never nag the user past onboarding.

**Definition of done:** Grant PACKAGE_USAGE_STATS manually. Set threshold to 1 minute. Use a blocked app for 1 minute. Within 15 minutes, gentle notification fires. Does not fire again for that app today.

---

### Phase 8 — Onboarding (F9)

*Three screens. Must be the first thing new users see. Controlled by `ONBOARDING_DONE` flag in DataStore.*

**Routing logic in `NavGraph`:**
```kotlin
// On app start: if !onboardingDone → navigate to onboarding/welcome, else → home
// After onboarding completes: set ONBOARDING_DONE = true, navigate to home (clear backstack)
```

**Screen 1 — Welcome (`onboarding/welcome`):**
```
- App name "Maneo" in Lora, large
- Tagline from spec north star tone: "A quiet pause before the scroll."
- 2–3 sentences on what it does — warm, honest, no hype
- "Get started" button → Screen 2
- No sign-up prompt, no email, no social login
```

**Screen 2 — Permissions (`onboarding/permissions`):**

Three permission rows, each with:
- Icon
- Permission name
- Plain-language "why" (from spec §8)
- "Grant" button → opens the relevant system settings screen

| Permission | Button action |
|---|---|
| Accessibility | `startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))` |
| Usage Stats | `startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))` |
| Notifications | `ActivityCompat.requestPermissions(POST_NOTIFICATIONS)` (API 33+) |

After each grant, check and show a checkmark. The "Continue" button at bottom is always enabled — **never block progress**. Users who skip a permission lose that feature gracefully; the app must not crash.

**Spec check:** "Each explained plainly." The copy for each permission must not be technical. Write like a human, not a legal document.

**Screen 3 — First Block (`onboarding/first_block`):**
```
- Headline: "Pick one app to start with."
- Subtext: "You can always add more later."
- The same AppSelectorScreen list (reused component)
- "Done" button enabled once at least one app is selected (but skip allowed too)
- On Done: save selection to BlockedAppsRepository, set ONBOARDING_DONE = true, navigate to Home
```

**Definition of done:** Fresh install. Onboarding flows start to finish. All permissions grantable within the flow. App arrives at Home after completion. Reinstalling shows onboarding again. Second launch skips to Home.

---

### Phase 9 — Home Screen + Navigation Shell

*The main destination after onboarding. The "daily face" of the app.*

**Home screen layout (`home` route):**
```
- Top: app name or date in Lora
- Today's verse card (ManeoCard):
    - Verse text (Lora)
    - Reference (Inter, secondary)
    - Loaded via GetVerseForSlot("morning") seeded by today's date
- Quick entry button: "Write a prayer" → JournalEntryScreen (no slot, no prompt auto-set)
- Footer stat: "X apps being held" (count of blocked apps)
  — warm framing, not "X apps blocked"
```

**Bottom nav (3 items):**
```
Home | Journal | Settings
```

No top app bar on Home. No hamburger menu. No drawer. Three tabs, that's it.

**Navigation routes** (from Spec §6):

```kotlin
object Routes {
    const val ONBOARDING_WELCOME    = "onboarding/welcome"
    const val ONBOARDING_PERMISSIONS= "onboarding/permissions"
    const val ONBOARDING_FIRST_BLOCK= "onboarding/first_block"
    const val HOME                  = "home"
    const val JOURNAL_LIST          = "journal/list"
    const val JOURNAL_ENTRY         = "journal/entry?slot={slot}"
    const val APP_SELECTOR          = "blocker/apps"
    const val REMINDERS_SETTINGS    = "reminders/settings"
    const val SETTINGS              = "settings"
    // InterceptActivity is NOT in the nav graph — it's a separate Activity
}
```

---

### Phase 10 — Settings Screen

**Layout:**
```
- Screen time threshold: slider or stepper (15 / 30 / 60 / 90 / 120 min). Default: 30.
  Label: "Remind me after {X} minutes on a blocked app"
- Reminders: tappable row → RemindersSettingsScreen
- App list: tappable row → AppSelectorScreen
- Divider
- About section:
    - "Maneo is free and open source."
    - GitHub link (opens browser)
    - "Built with love, for peace." (no version number needed in v1)
    - Open source licences (standard Android OSS licences screen)
```

**Spec check:** No analytics toggle (there is no analytics). No premium/IAP row. No account/sync row. Keep it minimal.

---

## 4. Shared Components (`ui/components/`)

Build these early — they're used by almost every screen.

| Component | Description |
|---|---|
| `ManeoCard` | Rounded card, `colorSurface` background, subtle `colorBorder` stroke, warm shadow |
| `VerseText` | Lora font composable with reference line below in Inter/secondary |
| `PrimaryButton` | Full-width, `colorPrimary` fill, `OnPrimary` text, 12dp corner radius |
| `SectionTitle` | Inter, small caps style, `colorTextSecondary` — for section headers |
| `AppRow` | App icon + name + toggle — used in AppSelectorScreen |

---

## 5. Permissions Strategy

| Permission | Requested when | If denied |
|---|---|---|
| `POST_NOTIFICATIONS` | Onboarding screen 2 | Reminders silent — no crash, no nag |
| `BIND_ACCESSIBILITY_SERVICE` | Onboarding screen 2 (links to Settings) | Blocking disabled — app works as journal only |
| `PACKAGE_USAGE_STATS` | Onboarding screen 2 (links to Settings) | Screen time feature disabled silently |
| `RECEIVE_BOOT_COMPLETED` | Manifest only — no user prompt | Reminders won't restart after reboot |

**Principle:** Graceful degradation on every denied permission. The app must never crash because a permission is missing. Check before every use.

---

## 6. Verses.json — Writing Guidelines

All verses from WEB (World English Bible — public domain, no attribution required legally, but credit WEB in About).

**For `intercept` slot — must be:**
- Inviting, not accusatory ("Come to me..." not "Flee from temptation...")
- Grounding — present tense peace, not future-reward framing
- Short enough to read in one breath (under 30 words ideally)

**For `morning` slot:** New mercies, hope, commissioning for the day
**For `afternoon` slot:** Peace in the middle, steadfast love, rest amid activity
**For `evening` slot:** Rest, surrender, gratitude, God's faithfulness

**Explicitly avoid:**
- Verses about judgment, wrath, or punishment
- Verses that could read as guilt-inducing given the context (user just tried to open Instagram)
- Long passages — single verses only

**Spec check:** The north star is "peace, warmth, rest." Every verse choice must serve that. A verse that adds guilt when shown on the intercept screen violates the north star, regardless of its theological importance.

---

## 7. Implementation Order Summary

```
Phase 0 → Scaffold + Theme
Phase 1 → Core models + Room + DataStore
Phase 2 → Verse library (F8) — no permissions needed
Phase 3 → Intercept screen UI (F2) — no service needed yet
Phase 4 → AccessibilityService + App Selector (F1, F3)
Phase 5 → Prayer Journal (F7)
Phase 6 → Reminders + Daily Verse (F5, F6)
Phase 7 → Screen Time Warning (F4)
Phase 8 → Onboarding (F9)
Phase 9 → Home + Navigation shell
Phase 10 → Settings screen
```

This order means:
- Phases 0–3 can be built and tested on an emulator
- Phases 4–7 require a real device (AccessibilityService, UsageStats, real WorkManager timing)
- Onboarding (Phase 8) is built last because it depends on all the features it introduces
- Home (Phase 9) is the last screen composed because it references feature counts

---

## 8. Known Risks & Mitigations

| Risk | Mitigation |
|---|---|
| OEM background kill (Xiaomi, Huawei, Samsung) silently kills AccessibilityService | Onboarding links to battery exemption. Service uses minimal resources. |
| WorkManager 15-min minimum means screen time check is approximate | Expected behaviour, note it in UI: "checked approximately every 15 minutes" |
| `TYPE_WINDOW_STATE_CHANGED` fires for dialogs and system UI, not just app launches | Filter: ignore events where `packageName` is system UI packages or our own package |
| InterceptActivity appearing on top of the lock screen | Set `showWhenLocked` and `turnScreenOn` flags — the spec intends full-screen takeover |
| Verse JSON parse failure at cold start | Wrap in try-catch, return a hardcoded fallback verse — never crash on missing asset |
| User in an emergency needs to pass through a blocked app immediately | By spec design there is no bypass. This is intentional. Document it honestly in onboarding. |

---

## 9. What is Never Built (Spec §10 + V2 Backlog)

If a contributor opens a PR for any of the following, reject it:
- Analytics, crash reporting that sends data off-device
- Network calls of any kind
- Streak counters or gamification (V2 — do not build in V1)
- Premium features, IAP, subscriptions
- iOS port
- Browser/website blocking
- AI prayer generation
- Social features
- A countdown timer on the intercept screen
- A "bypass" button on the intercept screen
