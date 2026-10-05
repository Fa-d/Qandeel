# Redesign plan: Celestial Qandeel, on Android and iOS

The phone app moves from four switchable "skins" over a template-like layout to one signature,
immersive look, gets an onboarding flow and a simpler structure, and becomes Kotlin Multiplatform
so the same UI ships on iOS. Wear OS keeps its own UI.

## Decisions

| Question | Decision |
| --- | --- |
| iOS | **Kotlin Multiplatform + Compose Multiplatform.** The domain and data move to KMP and the phone UI to Compose Multiplatform, so the redesign is written once. iOS gets thin platform adapters (AVPlayer audio, URLSession downloads, Now Playing). |
| Visual concept | **Celestial Lamp.** A deep night sky with drifting star dust and depth parallax. A 3D qandeel woven from strings of light floats over the pool of light it casts: its star-shaped rings breathe, twist and vibrate, and its heart swells with each recited word. Gold light on deep green and indigo. |
| 3D rendering | **Compose shaders + Canvas.** GPU shaders (AGSL on Android 13+, SkSL through Skiko on iOS and desktop) and Canvas-projected 3D geometry. No 3D engine and no WebView. |
| Styles | **One signature look.** The Mushaf, Material, Expressive and Glass styles and the Appearance style picker go away. What remains is Theme (Auto, Light, Dark) and Reduce motion. |
| Navigation | **3 tabs + floating player:** Home, Quran, You. The reader and the full player open over the tabs. |
| Onboarding | Listening setup, reading comfort, look and motion, offline and watch. Every step can be skipped and changed later. |
| Reader while playing (Phase 4) | **Lyrics mode.** While its surah plays, the reader keeps the reciting ayah on a line in the upper third, lit, with the gold word pointer, and fades the others back. Scrolling away pauses it; a chip brings it back. |
| Full player (Phase 4) | **The ayah only.** Full screen and centred on the ayah (Arabic with the word pointer, the recited word's meaning, the translation). The lantern stays behind it as a soft glow that swells with each recited word, not as a hero. |
| Scope | **Reorganize only.** No new features: ayah of the day, bookmarks and a downloads manager are out of scope. |
| Delivery | **One PR per phase** (below). |

## Information architecture

```
Onboarding (first launch only)
  Welcome → Listening → Reading → Look & motion → Offline & watch → Home

Tabs ─┬─ Home    lamp hero · continue listening · recently heard (from history) · progress glance
      ├─ Quran   one search (name, number, 2:255) · Surah | Juz | Offline
      └─ You     progress · settings (listening, reading, appearance) · about
Over the tabs:  Reader (surah)   ·   Floating mini player → immersive full player
```

Where every existing feature goes:

| Today | After |
| --- | --- |
| Home top bar: Progress, Reading settings, Appearance icons | You tab |
| Home: continue card | Home, as the hero card under the lamp |
| Home: search + Surah/Juz toggle | Quran tab: search + Surah, Juz and Offline (Offline filters by the download state the list already has) |
| Progress screen | You › Progress (a glance on Home links to it) |
| Reading settings sheet (size, translation, follow along, voice, word by word, theme) | You › Settings, grouped as Listening, Reading and Appearance. The reader keeps a quick "Aa" sheet with size, translation and word by word only |
| Appearance screen (style grid, tone, wallpaper colors) | You › Settings › Appearance: Theme and Reduce motion. On Android, wallpaper colors are dropped because the look is fixed |
| About | You › About |
| Reader overflow: download, remove download, send to watch | Reader header: one offline button with its state; send to watch in the top bar's overflow |
| Mini player | Floating pill above the tab bar, with a progress ring |
| Full player sheet | Full-screen immersive player: the ayah with the word pointer over the lantern's soft glow, the surah bar, transport, then mode and voice, repeat, speed and sleep in one options row |

## The Celestial design language

- **Depth layers, back to front:** the sky shader (a gradient plus slow noise nebula) → star dust
  particles (3 parallax planes, which move with scroll and, if allowed, with the gyroscope) → the
  lamp (3D) → glass content cards → chrome.
- **The lamp** (approved after a first version, a glass prism on a chain with a flame, read as "a
  fiery key ring"): a floating lantern of strings of light, projected in 3D on a Canvas. Its rings
  follow a qandeel's swell, each the rub el hizb softened into an eight-lobed flower; they breathe
  between star and circle, twist, and vibrate in standing waves, with two closed strings orbiting
  and beads of light running along them. Each word onset from `WordPointer` raises its energy
  (deeper vibration, faster beads, a brighter heart), so it needs no audio analysis and works the
  same on iOS.
- **Colour:** night indigo `#0B1026` to deep green `#0E3B2E` for the sky, mushaf gold `#D4A84B` for
  light, warm paper `#F3EBDD` for text on dark. The light theme is a "dawn" sky (pale gold to
  paper) with the same structure.
- **Type:** Amiri Quran for Arabic (bundled already) and a humanist sans for the UI. Display sizes
  are used for surah names on the hero.
- **Motion:** spring-based shared-element transitions (surah row → reader header, mini player →
  full player). The lamp tilts on scroll.
- **Reduce motion and fallbacks:** "Reduce motion" (and the system setting) freezes the particles,
  stops the lamp turning and keeps a static glow. Below Android 13 (no AGSL), the sky is a Canvas
  gradient and the stars are drawn points. The look is the same, with fewer effects.
- **Performance budget:** effects draw in one `drawWithCache`/`graphicsLayer` per layer, never per
  item. Particle counts scale with the display. Effects stop when the app is in the background or
  the screen is under the full player. The target is 60 fps on a mid-range 2022 phone, checked with
  the existing Macrobenchmark.

## Target modules

```
:core:domain        KMP: jvm + iosArm64 + iosSimulatorArm64            (today: JVM; the move is nearly free)
:core:data          KMP: commonMain (parsers, Room, DataStore, settings, history, text)
                         androidMain (ExoPlayer, Media3 downloads, watch messages)
                         iosMain (AVQueuePlayer, URLSession downloads, MPNowPlayingInfoCenter)
:core:designsystem  Android: the Mushaf tokens Wear uses (kept for Wear; the phone no longer uses it)
:shared             KMP + Compose: Celestial design system (tokens, sky, stars, lamp, kit), screens +
                    ViewModels (onboarding, home, quran, you, reader, player), strings in
                    composeResources; produces the iOS framework "Shared"
:app                Android shell: MainActivity, playback service, Hilt graph, watch link, widgets
:wear               unchanged (Hilt, Wear Compose)
iosApp/             Xcode project: SwiftUI App hosting ComposeUIViewController, audio session, Info.plist
```

- **DI:** Hilt stays in `:app` and `:wear`. Shared ViewModels are plain classes that take ports.
  A small `QandeelGraph` interface exposes the ports. On Android a Hilt `@Singleton` implements it,
  and on iOS a Kotlin object does. ViewModels are created with `viewModel { … }` factories. No new
  DI library.
- **Assets:** the Quran text, timings and word meanings (about 12 MB of JSON) stay in
  `:core:data`'s Android assets; the iOS app bundles the same folder (Phase 5).
- **Strings:** the screens' strings move to `:shared`'s `composeResources/values/strings.xml` as
  each screen is rebuilt (same XML format). `R.string` becomes `Res.string`.
- **Desktop JVM target (tests only):** `:shared` adds `jvm()`, so
  shared UI screenshot tests run with Roborazzi on Compose Desktop. These are fast, need no
  Robolectric, and render the shaders through Skia, as on iOS.

## Phases

Each phase is one PR. It is done when `./gradlew qualityGate` is green and the phase's goldens are
reviewed.

### Phase 1: KMP foundation (no visible change)
The old screens are replaced in Phase 4, so they are not ported. New UI is written in KMP from the
start, and the Android-only code stays where it is until something replaces it.
- Convert `:core:domain` to KMP (`jvm` + iOS targets). The only JVM-only call is
  `String.format(Locale.ROOT, …)` in `QuranAudioUrls`, which becomes `padStart`. Android modules
  keep consuming its JVM variant, so `:core:data`, `:app` and `:wear` don't change.
- Create `:shared` (Compose Multiplatform: android, jvm for tests, iOS) with compose resources,
  the Amiri Quran font, and Roborazzi desktop screenshot tests. `:app` depends on it.
- The iOS sources compile on Linux (klib cross-compilation) in the quality gate.
- **Ask:** nothing expected.

### Phase 2: Celestial design system + 3D
- Tokens (colour, type, motion) and `CelestialTheme` in `:shared`. The old skins, glass/Haze and
  the style picker are deleted in Phase 4, with the screens that use them.
- `CelestialSky` (one shader with the nebula, glow and three star planes, and a drawn fallback),
  `QandeelLamp` (the string lantern, driven by `LampMotion(time, energy, tilt)`), the glass
  surface, the octagram badge, the play button, search, pill tabs, the floating tab bar and mini
  player, and Qandeel's own icons.
- Concept goldens of Home (night and dawn) and the Quran tab, and a frame recorder for reviewing
  the motion as a video.
- **Ask:** rendered screenshots go to the user for approval of the look before the screens are
  rebuilt.

### Phase 3: Onboarding
- `onboardingDone` and `reduceMotion` in `QuranSettings` (DataStore, migration-safe defaults).
- Five pages over the live sky: Welcome (the lamp lights up) → Listening (mode + Bangla voice with
  a 5 s sample per voice) → Reading (size slider with a live ayah, translation, word by word) →
  Look & motion (theme, Reduce motion, previewed on the lamp) → Offline & watch (download a starter
  set: Al-Fatiha + the last 10 surahs; link a watch if one is found; ask for notification
  permission on Android 13+).
- Skip goes to Home at any time, and every choice can be changed later in You › Settings.
- **Ask:** the copy for the welcome page, and whether the starter set is right.

### Phase 4: The new structure
- Move the ViewModels from `:app` to `:shared` (Hilt replaced by `QandeelGraph`), with their tests.
- Tabs (Home, Quran, You) with the floating player, the reader restyled (header on the sky, ayah
  cards, word pointer in gold light), and the immersive full player.
- Shared-element transitions. Delete the old screens, `:core:ui`, the skins and their goldens, and
  add new goldens. `:core:designsystem` keeps only what Wear uses.
- Update the baseline profile journey and re-measure startup.
- **Ask:** any trade-offs in the reader layout that come up (for example, the density of word by
  word).

Shipped in two PRs:

- **4a:** the three tabs (Home, Quran, You) with the floating mini player; Home, Quran, You and
  Progress rebuilt in `:shared`, with `SettingsViewModel` merging the reading and appearance
  settings.
- **4b:** the reader with lyrics mode, the immersive full player (the ayah, with the lantern as a
  soft glow), About, and the whole navigation in `:shared` (`QandeelApp`, `@Serializable` routes,
  `QandeelGraph`). `:app` is now a shell: `MainActivity` hosts `QandeelApp`, and Hilt's
  `AndroidQandeelGraph` provides the ports. The old screens, their ViewModels, tests and goldens,
  `:core:ui`, the four styles, the Appearance screen and wallpaper colors are deleted. The baseline
  profile journey skips onboarding and walks the tabs, the reader and the player.
- Not done: shared-element transitions (the full player slides up over the tabs), and trimming
  `:core:designsystem` (it still holds the old skins, used only by its own tests).

### Phase 5: iOS
- Convert `:core:data` to KMP: the parsers, Room (KMP, bundled SQLite), DataStore (KMP) and
  history go to commonMain; ExoPlayer, Media3 and watch messages stay in androidMain. The Quran
  JSON assets stay where they are for Android, and the Xcode build copies the same folder into the
  iOS bundle.
- iosMain adapters: `AvQuranPlayer` (AVQueuePlayer over the same `QueuePlan`, rate with pitch
  kept, MPRemoteCommandCenter next/previous by ayah, the Now Playing seek spanning the surah),
  `UrlSessionSurahDownloads` (background session, into Application Support, excluded from iCloud
  backup), and the settings and history paths.
- `iosApp/`: a SwiftUI shell, `AVAudioSession` `.playback`, background audio mode, app icon from
  `build_logo.py`, launch screen.
- The iOS framework is built in CI on a macOS runner (`linkDebugFrameworkIosSimulatorArm64`) and
  the Kotlin iOS sources compile on Linux (klib cross-compilation) in the quality gate.
- **Ask:** the Apple Developer team, the bundle id, and whether Apple Watch is wanted (not
  planned; Wear OS stays Android only).

## Risks

- **AGSL needs Android 13+** while minSdk is 26: the fallbacks above are part of each component,
  not an afterthought, and goldens cover both paths.
- **Compose Multiplatform on iOS** scrolls and renders text well, but Arabic shaping with Amiri
  Quran must be checked early, on an iOS simulator, as soon as the Phase 2 gallery exists.
- **Room KMP + DataStore KMP** migrations: the database schema and the DataStore file name stay
  the same, so existing users keep their history and settings. A migration test covers this.
- **The phone and watch share an applicationId.** Nothing in this plan changes it.
