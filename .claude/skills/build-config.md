# Build Config

## Toolchain
- Kotlin 2.3.21, AGP 9.4.1, Gradle 9.7.1; JVM target 17 (the Gradle daemon runs on JDK 21)
- minSdk 26, compileSdk 37 (Android modules)
- Versions centralized in `gradle/libs.versions.toml`; modules in `settings.gradle.kts`:
  `:app`, `:wear`, `:core:domain`, `:core:designsystem`, `:core:data`, `:core:testing`, `:shared`,
  `:architecture-test`, `:baselineprofile`
- Key libraries: Compose Multiplatform 1.12.1 with its navigation-compose and lifecycle (phone UI in
  `:shared`), Compose BOM + Wear Compose Material 3 (watch), Hilt, Media3, DataStore,
  kotlinx-serialization, play-services-wearable

## Module kinds
- `:core:domain` — Kotlin Multiplatform (jvm, iosArm64, iosSimulatorArm64), no Android
- `:shared` — Kotlin Multiplatform + Compose Multiplatform (android, jvm for tests, iosArm64,
  iosSimulatorArm64; iOS framework `Shared`)
- `:core:testing`, `:architecture-test` — Kotlin/JVM, no Android
- `:core:data` — Android library; merged manifest carries INTERNET/POST_NOTIFICATIONS/foreground
  permissions and declares `QuranDownloadService`
- `:core:designsystem` — Android library (Compose UI only), the watch's tokens
- `:app`, `:wear` — Android applications (`:app` is the shell hosting `:shared`)

## Common commands
```bash
./gradlew qualityGate               # spotless + detekt + lint + all tests + kover (run before pushing)
./gradlew :app:installDebug         # install phone app
./gradlew :wear:installDebug        # install watch app
./gradlew :core:domain:jvmTest      # one module's tests
./gradlew :shared:jvmTest           # shared ViewModel + screenshot tests
./gradlew :shared:recordRoborazziJvm   # re-record the phone UI goldens
./gradlew :core:data:testDebugUnitTest
./gradlew spotlessApply             # fix formatting (ktlint + compose rules)
./gradlew :koverHtmlReport          # coverage report in build/reports/kover/html/
./gradlew :architecture-test:test --rerun-tasks   # Konsist reads all modules' sources
python3 -m unittest scripts/test_build_quran_text.py -v   # text-builder tests
```

Build only the modules you need; never `clean` other modules. If the network is flaky, add
`--offline`.

## Quality gate
`./gradlew qualityGate` (root task in `build.gradle.kts`) depends on:
- `spotlessCheck` + `detekt` on every subproject — ktlint 1.5.0 (`intellij_idea` style, 120 cols,
  config in `.editorconfig`) with Compose rules (`io.nlopez.compose.rules`); detekt config in
  `config/detekt/detekt.yml` (leftover `TODO:`/`FIXME:` fail)
- `lintDebug` on `:app`, `:wear`, `:core:data`, `:core:designsystem` (abortOnError)
- Unit tests: `:core:domain:jvmTest`, `:shared:jvmTest`, `testDebugUnitTest` on `:core:data`,
  `:core:designsystem`, `:app`, `:wear`, and `:architecture-test:test`
- iOS compiles: `compileKotlinIosSimulatorArm64` on `:core:domain` and `:shared`
- Kover: each module's line and branch floors (`coverageFloors` in the root `build.gradle.kts`)
  plus the root aggregate `:koverVerify`; the numbers are in `docs/QUALITY.md`

CI (`.github/workflows/ci.yml`) runs `./gradlew qualityGate assembleDebug` on every push and PR.

## Pre-commit hook
```bash
scripts/install-git-hooks.sh    # once per clone: git config core.hooksPath scripts/git-hooks
```
Runs the fast gates (`spotlessCheck detekt`) before each commit; bypass WIP commits with
`SKIP_QUALITY_HOOK=1 git commit ...`. See `docs/QUALITY.md`.

## Test stack (per module build files)
JUnit4; `kotlinx-coroutines-test` + Turbine + `MainDispatcherRule` (from `:core:testing`);
Robolectric pinned to sdk 36 via `src/test/resources/robolectric.properties`; Compose UI
tests (`createComposeRule()`) and Roborazzi on Robolectric for the watch; Roborazzi on Compose
Desktop for `:shared`; Media3 `media3-test-utils(-robolectric)` in `:core:data`
(`TestExoPlayerBuilder`, `TestPlayerRunHelper`, `FakeMediaSource`); Konsist in `:architecture-test`.

## Generated assets
The Quran text assets under `core/data/src/main/assets/quran/` are generated — never edit by hand:
```bash
python3 scripts/build_quran_text.py   # fetches alquran.cloud (quran-uthmani, en.sahih, bn.bengali)
```
