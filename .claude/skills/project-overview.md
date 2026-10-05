# Qandeel Project Overview

## What is Qandeel?
Qandeel is a Quran player for phone and Wear OS, built with clean architecture (ports and adapters).
The phone app (onboarding, then three tabs — Home, Quran, You — with a floating mini player; the
reader and the full player open over them) offers search, a reader (Arabic + English/Bangla text,
lyrics mode while playing) and ayah-by-ayah recitation. Its UI is written once in `:shared` (Compose
Multiplatform, Android + iOS) in one look, Celestial. The watch app mirrors playback and downloads. Surah audio can be
downloaded per surah for offline listening.

## Tech Stack
- Kotlin 2.3 (Multiplatform for `:core:domain` and `:shared`), AGP 9, Gradle 9; minSdk 26,
  compileSdk 37, JVM target 17
- Phone: Compose Multiplatform (no Material; the Celestial kit) with navigation-compose and
  lifecycle ViewModels; watch: Compose for Wear OS with Wear Material 3
- Hilt (Android apps only), Media3 (ExoPlayer + session + offline downloads)
- kotlinx.serialization (phone→watch messages, asset parsing), DataStore (settings)
- JUnit4, kotlinx-coroutines-test, Turbine, Robolectric, Roborazzi, Konsist, Kover, Spotless, Detekt

## Module Structure

```
Qandeel/
├── app/                 # Phone app: Android shell hosting :shared (Android application)
├── shared/              # Phone UI for Android + iOS: design system, screens, ViewModels (KMP + Compose)
├── wear/                # Wear OS app (Android application)
├── core/domain/         # Models, pure logic, ports (Kotlin Multiplatform: JVM, iOS)
├── core/data/           # Adapters implementing the ports (Android library)
├── core/designsystem/   # The watch's design tokens (Android library, Compose UI only)
├── core/testing/        # Fakes + sample data for tests (Kotlin/JVM)
├── architecture-test/   # Konsist architecture rules (Kotlin/JVM)
├── baselineprofile/     # Baseline profile generator + startup benchmark
├── docs/                # ARCHITECTURE.md, QUALITY.md, REDESIGN_PLAN.md
└── scripts/             # Audio/text sourcing scripts, git hooks
```

## Module Responsibilities

### `:core:domain` (package `dev.sadakat.qandeel.core.domain`)
- Kotlin Multiplatform (JVM, iOS), no Android imports (enforced by `DomainIsolationTest`)
- Models: `QuranMeta`, `Surah`, `Ayah`, `AyahRef`, `Track`, `RecitationMode`
- Pure logic: `QuranAudioUrls`, `QueuePlan`, `DownloadAggregation`
- Ports: `QuranText`, `QuranSettings`, `SurahDownloads`, `QuranPlayer`, `AudioTimings`,
  `WordMeanings`, `ListeningHistory`, `WatchConnection`

### `:core:data` (package `dev.sadakat.qandeel.core.data`)
- Adapters: `AssetQuranText`, `DataStoreQuranSettings`, `MediaSurahDownloads`, `ExoQuranPlayer`
- Media3 plumbing: `QuranCache` (cache + download manager), `QuranDownloadService`,
  `QuranMediaItems`
- Phone→watch message: `QuranDownloadMessage`, `WearPaths`

### `:shared` (package `dev.sadakat.qandeel.shared`)
- `designsystem/` — Celestial: tokens + `CelestialTheme`, the sky shader (`effects/`), the lamp
  (`lamp/`), the kit (`kit/`: glass, controls, floating bars, inputs, choices, icons, sheet, toast)
- `presentation/` — onboarding, home (Home + Quran tabs), you, progress, main (tabs + mini player),
  reader, player, about, components; each screen with its ViewModel
- `app/` — `QandeelApp` (root + `NavHost`), routes, `QandeelGraph` (the ports) + `QandeelPlatform`,
  `viewModel { }` factories
- Strings and fonts in `src/commonMain/composeResources/`; tests + goldens in `src/jvmTest/`

### `:app` (package `dev.sadakat.qandeel`)
- Android shell: `MainActivity` hosts `QandeelApp` and implements `QandeelPlatform`
- Hilt modules (`di/`) and `AndroidQandeelGraph`, `QuranPlaybackService`, the watch link and
  listening sync (`watch/`)

### `:wear` (package `dev.sadakat.qandeel.wear`)
- Watch UI: home (surah list, chips), surah, now playing (`presentation/`)
- `QuranMessageService` (receives download requests), `QuranPlaybackService`
- `WifiForDownloads` (binds to Wi-Fi while downloads run)

## Audio and Text Sources
- Arabic (Alafasy 128k) and English (Saheeh Intl read by Ibrahim Walk, 192k) verse files stream
  from `cdn.islamic.network`; Bangla verse files from the Hugging Face dataset
  `faddy001/quran_audio` (mirrors the local `quran_audio/` folder produced by `scripts/`).
- Text assets (`core/data/src/main/assets/quran/`) are generated from alquran.cloud editions
  (`quran-uthmani`, `en.sahih`, `bn.bengali`) by `scripts/build_quran_text.py`.

## See Also
`docs/ARCHITECTURE.md` (full architecture), `docs/QUALITY.md` (quality gate),
`docs/REDESIGN_PLAN.md` (the redesign and its phases), `README.md`.
