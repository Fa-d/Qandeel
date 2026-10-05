# UI (Compose)

Phone: Compose Multiplatform in `:shared` (Android and iOS; no Material library, every control from
the Celestial kit), hosted by `:app`'s `MainActivity`. Watch: Compose for Wear OS with Wear
Material 3 (`:wear`). One pattern everywhere: **unidirectional data flow**.

## Conventions (enforced)
- ViewModels: one immutable `StateFlow<XxxUiState>` where `XxxUiState` is a data class, plain
  functions for user actions. No public `MutableStateFlow`, no `Context` in the constructor
  (`ViewModelArchitectureTest`, `UiStateArchitectureTest`). `:wear`'s are `@HiltViewModel`;
  `:shared`'s are plain classes over ports, built from `QandeelGraph` in `app/ViewModels.kt`
  (`viewModel { }`).
- Screens: stateless composables take `(state, actions, modifier: Modifier = Modifier)`; the
  wiring that collects state (`collectAsStateWithLifecycle`) and binds the ViewModel lives in
  `:shared`'s `app/Navigation.kt` (watch: a thin `XxxRoute`). Every composable that emits UI
  takes `modifier`.
- Presentation imports domain ports only — never `dev.sadakat.qandeel.core.data` or Media3
  (`PresentationIsolationTest`).
- State is usually built with `combine(...).stateIn(viewModelScope, WhileSubscribed(5_000), initial)`;
  one-shot loads are `flow { emit(...) }.catch { emit(failed) }`.

## Phone UI (`shared/src/commonMain/kotlin/dev/sadakat/qandeel/shared/`)

- `app/QandeelApp.kt` — root: nothing until the settings are read, then onboarding (new installs,
  `QuranSettings.onboardingDone`) or `CelestialTheme` around a `NavHost` (navigation-compose for
  Compose Multiplatform). `app/Routes.kt`: `@Serializable` `TabsRoute`, `ReaderRoute(surah, ayah)`,
  `PlayerRoute`, `ProgressRoute`, `AboutRoute`. One `PlayerViewModel` for the mini player, the full
  player and the playback-error toast.
- `app/QandeelGraph.kt` — `QandeelGraph` (the ports; on Android the Hilt `@Singleton`
  `AndroidQandeelGraph` in `:app`) and `QandeelPlatform` (version, share sheet, notification
  permission; `MainActivity` implements it).
- `presentation/onboarding/` — five pages over the sky; finishing saves the choices and starts the
  starter downloads; skip keeps the defaults.
- `presentation/main/MainTabs.kt` — the three tabs (Home, Quran, You) with the floating
  `MiniPlayerBar` and tab bar.
- `presentation/home/` — `HomeTab` (lamp, continue card, recently heard, progress glance),
  `QuranTab` (search via `SurahSearch`, Surah | Juz | Offline), `HomeViewModel`.
- `presentation/you/` — `YouTab` (progress card, settings: listening, reading, appearance = Theme
  Auto/Light/Dark + Reduce motion; About), `SettingsViewModel`.
- `presentation/reader/` — `ReaderScreen` (lyrics mode while its surah plays: the reciting ayah lit
  on a line in the upper third, others faded; `FollowAlong`), `ReaderSheets` (ayah actions, the
  "Aa" quick settings, send to watch, remove download), `SurahReaderViewModel`, `AyahShareText`.
- `presentation/player/` — `PlayerScreen` (full-screen, centred on the ayah, the lantern as a soft
  glow behind it), `PlayerSheets` (recitation, repeat, speed, sleep), `TimeBar`, `PlayerViewModel`
  (`init` restores the last position, paused).
- `presentation/progress/`, `presentation/about/`, `presentation/components/` (`RecitedArabicText`
  and `WordByWordText` draw the word pointer; `SurahRow`).
- `designsystem/` — the Celestial design system: tokens and `CelestialTheme` (read as
  `Celestial.colors`, `.type`, `.quran`, `.spacing`, `.shapes`, `.clock`), `effects/CelestialSky`
  (sky shader: AGSL on Android 13+, Skia on iOS/desktop, a drawn fallback), `lamp/QandeelLamp`
  (`LampMotion`), and `kit/` (glass, octagram badge, controls, floating bars, inputs, choices,
  icons, `CelestialSheet` + `SheetAction`, `Toast`).
- Strings: `composeResources/values/strings*.xml`, read as `Res.string.xxx`
  (`dev.sadakat.qandeel.shared.resources`). Fonts (Amiri Quran, Fraunces, Manrope) in
  `composeResources/font/`.

## Watch UI (`wear/src/main/java/dev/sadakat/qandeel/wear/presentation/`)

- `WearQuranApp.kt` — root: `SwipeDismissableNavHost` over routes `home`, `surah/{number}`,
  `nowplaying`
- `home/` — `HomeScreen` (+`HomeRoute`), `WearHomeViewModel` (`UiState`: surah rows with download
  state, mode, now-playing chip, continue chip). `cycleMode()` steps
  Arabic-only → Arabic+English → Arabic+Bangla; `continuePlaying()` restores the saved position
- `surah/` — `SurahScreen` (+`SurahRoute`), `WearSurahViewModel` (`UiState`: surah, mode,
  download state; actions play/download/remove)
- `nowplaying/` — `NowPlayingScreen` (+`NowPlayingRoute`), `NowPlayingViewModel` (`UiState`:
  surah name, current ayah and its Arabic text, play/pause/buffering, error). Ayah 0 (basmala)
  has no `QuranText` entry; the screen shows the basmala itself.

## Testing UI
Phone (`shared/src/jvmTest`): ViewModel tests with the `:core:testing` fakes (`runTest`, Turbine,
`MainDispatcherRule`), and Roborazzi screenshots on Compose Desktop (`phoneSnapshot(...)`, goldens
in `shared/src/jvmTest/screenshots/`; record with `./gradlew :shared:recordRoborazziJvm`, verify
with `:shared:verifyRoborazziJvm`). Watch: Compose UI tests and screenshots on Robolectric
(`createComposeRule()`, `wearSnapshot`), driving the stateless screens with hand-built `UiState`s;
test files mirror the main packages (`src/test/java/`).
