# File Locations

Paths are relative to the repo root. Test files mirror the main packages, under `src/test/kotlin/`
or `src/test/java/` (Android/JVM modules) or `src/jvmTest/kotlin/` (`:core:domain`, `:shared`), and
end in `Test`.

## Domain — `core/domain/src/commonMain/kotlin/dev/sadakat/qandeel/core/domain/`
| File | Contents |
| --- | --- |
| `model/QuranMeta.kt` | `QuranMeta` — surah structure, global ayah numbering |
| `model/Surah.kt` | `Surah`, `Ayah`, `AyahRef`, `Revelation` |
| `model/Track.kt` | `Track`, `RecitationMode` |
| `audio/QuranAudioUrls.kt` | `QuranAudioUrls` (+ `AudioFile`) |
| `audio/QueuePlan.kt` | `QueuePlan`, `QueueItemId`, `QueueEntry` |
| `audio/DownloadAggregation.kt` | `DownloadAggregation`, `FileDownloadState` |
| `repository/QuranText.kt` | port |
| `repository/QuranSettings.kt` | port + `LastPosition` |
| `repository/SurahDownloads.kt` | port + `SurahDownloadState` + `stateOf` helpers |
| `repository/WatchConnection.kt` | port: the phone's link to the watch |
| `player/QuranPlayer.kt` | port + `NowPlaying` |

## Data — `core/data/src/main/kotlin/dev/sadakat/qandeel/core/data/`
| File | Contents |
| --- | --- |
| `text/AssetQuranText.kt` | `QuranText` adapter (assets + LRU cache) |
| `text/QuranTextParser.kt` | internal JSON → domain models |
| `settings/DataStoreQuranSettings.kt` | `QuranSettings` adapter |
| `audio/QuranCache.kt` | cache + download manager singleton |
| `audio/MediaSurahDownloads.kt` | `SurahDownloads` adapter (+ internal `fileDownloadStateOf`) |
| `audio/QuranDownloadService.kt` | foreground download service |
| `audio/QuranMediaItems.kt` | queue → media items |
| `player/ExoQuranPlayer.kt` | `QuranPlayer` adapter + ayah-aware session player |
| `link/QuranDownloadMessage.kt` | phone → watch payload |
| `link/WearPaths.kt` | `/quran/download`, capability names |

Assets: `core/data/src/main/assets/quran/surahs.json` and `quran/text/001..114.json`.
Manifest (merged into both apps): `core/data/src/main/AndroidManifest.xml`.

## Phone UI (shared with iOS) — `shared/src/commonMain/kotlin/dev/sadakat/qandeel/shared/`
| Path | Contents |
| --- | --- |
| `app/QandeelApp.kt` | root composable: onboarding or the `NavHost` in `CelestialTheme` |
| `app/QandeelGraph.kt` | `QandeelGraph` (the ports) + `QandeelPlatform` (share, notifications, version) |
| `app/ViewModels.kt` | `viewModel { }` factories built from the graph |
| `app/Routes.kt`, `app/Navigation.kt` | `@Serializable` routes; each destination's wiring |
| `app/MiniPlayerMapping.kt` | player state → mini player |
| `designsystem/` | `CelestialColors`, `CelestialType`, `QuranType`, `CelestialScale`, `CelestialTheme` |
| `designsystem/effects/` | `CelestialSky`, `SkyShader`, `RuntimeShader` (expect; actuals in `androidMain`, `skikoMain`) |
| `designsystem/lamp/` | `QandeelLamp` (+ `LampMotion`), `StringLantern`, `LampGeometry` |
| `designsystem/kit/` | `Glass`, `Octagram`, `Controls`, `FloatingBars`, `Inputs`, `Choices`, `CelestialIcons`, `Sheet` (`CelestialSheet`, `SheetAction`), `Toast` |
| `presentation/onboarding/` | `OnboardingScreen` (+`OnboardingRoute`), `OnboardingViewModel` |
| `presentation/main/MainTabs.kt` | the tabs + floating `MiniPlayerBar` |
| `presentation/home/` | `HomeTab`, `QuranTab`, `HomeViewModel`, `SurahSearch` |
| `presentation/you/` | `YouTab`, `SettingsViewModel` |
| `presentation/progress/` | `ProgressScreen`, `ProgressViewModel` |
| `presentation/reader/` | `ReaderScreen`, `ReaderSheets`, `SurahReaderViewModel`, `FollowAlong`, `AyahShareText` |
| `presentation/player/` | `PlayerScreen`, `PlayerSheets`, `PlayerViewModel`, `TimeBar`, `PlayerLabels` |
| `presentation/components/` | `RecitedArabic` (word pointer, word by word), `SurahRow`, `Common` |
| `presentation/about/` | `AboutScreen` |

Strings: `shared/src/commonMain/composeResources/values/strings*.xml` (`Res.string`); fonts:
`shared/src/commonMain/composeResources/font/`. Tests and Roborazzi goldens:
`shared/src/jvmTest/kotlin/...`, `shared/src/jvmTest/screenshots/`.

## Phone app (Android shell) — `app/src/main/java/dev/sadakat/qandeel/`
| Path | Contents |
| --- | --- |
| `QandeelApplication.kt` | `@HiltAndroidApp` |
| `MainActivity.kt` | hosts `QandeelApp`, implements `QandeelPlatform` |
| `AndroidQandeelGraph.kt` | Hilt `@Singleton` implementing `QandeelGraph` |
| `di/QuranModule.kt`, `di/MediaModule.kt`, `di/WatchModule.kt` | Hilt |
| `service/QuranPlaybackService.kt` | `MediaSessionService` |
| `watch/WatchLink.kt` | `WatchConnection` over the Wearable Data Layer |
| `watch/ListeningSync.kt`, `watch/ListeningSyncService.kt`, `watch/BanglaVoiceSync.kt` | sync with the watch |

## Design tokens for the watch — `core/designsystem/src/main/kotlin/dev/sadakat/qandeel/core/designsystem/`
`QandeelTheme.kt`, `ref/` (palettes), `color/QandeelColors.kt`, `scale/`, `type/`, `shape/`,
`component/WearTokens.kt`.

## Watch app — `wear/src/main/java/dev/sadakat/qandeel/wear/`
| Path | Contents |
| --- | --- |
| `WearApplication.kt`, `presentation/MainActivity.kt` | entry points |
| `di/QuranModule.kt`, `di/MediaModule.kt` | Hilt |
| `presentation/WearQuranApp.kt` | root composable (SwipeDismissableNavHost) |
| `presentation/home/` | `HomeScreen`, `WearHomeViewModel` |
| `presentation/surah/` | `SurahScreen`, `WearSurahViewModel` |
| `presentation/nowplaying/` | `NowPlayingScreen`, `NowPlayingViewModel` |
| `service/QuranMessageService.kt` | receives `/quran/download` |
| `service/QuranPlaybackService.kt` | `MediaSessionService` |
| `network/WifiForDownloads.kt` | Wi-Fi while downloads run |

## Testing
- `core/testing/src/main/kotlin/dev/sadakat/qandeel/core/testing/` — `FakeQuranText`,
  `FakeSurahDownloads`, `FakeQuranSettings`, `FakeQuranPlayer`, `FakeAudioTimings`,
  `FakeWordMeanings`, `FakeListeningHistory`, `FakeWatchConnection`, `TestQuran`,
  `MainDispatcherRule`
- `architecture-test/src/test/kotlin/dev/sadakat/qandeel/architecture/` — `DomainIsolationTest`,
  `PresentationIsolationTest`, `ViewModelArchitectureTest`, `UiStateArchitectureTest`,
  `TestNamingArchitectureTest`, `DesignTokenUsageTest`, `DesignSystemArchitectureTest`,
  `MaterialLibraryTest`
- `src/test/resources/robolectric.properties` (in the Android modules) pins sdk=36

## Scripts and config
| Path | Purpose |
| --- | --- |
| `scripts/build_quran_text.py` (+ `test_build_quran_text.py`) | build/test the text assets |
| `scripts/download_quran_audio.sh`, `verify_quran_audio.sh` | fetch/verify the audio set |
| `scripts/split_bangla_verses.py` | split Bangla surahs into verses |
| `scripts/install-git-hooks.sh`, `scripts/git-hooks/pre-commit` | quality pre-commit hook |
| `gradle/libs.versions.toml` | all dependency versions |
| `config/detekt/detekt.yml`, `.editorconfig` | static analysis / formatting |
| `docs/ARCHITECTURE.md`, `docs/QUALITY.md` | architecture & quality docs |
