# Quality guardrails

One command proves the codebase is formatted, statically clean, lint-clean, tested, visually
unchanged, covered and architecturally sound:

```
./gradlew qualityGate
```

Run it locally before pushing; CI (`.github/workflows/ci.yml`) runs the same command plus
`assembleDebug` on every push and pull request.

## What each gate checks

| Gate | Task(s) | What it proves | How to fix failures |
| --- | --- | --- | --- |
| Formatting | `spotlessCheck` (every subproject) | Kotlin + `.gradle.kts` follow ktlint (`intellij_idea` style, 120 cols) plus Compose rules (`io.nlopez.compose.rules`). Config lives in `.editorconfig`. | `./gradlew spotlessApply` |
| Static analysis | `detekt` (every subproject) | No code smells / leftover `TODO:`/`FIXME:` comments. Overrides in `config/detekt/detekt.yml`. | Read `<module>/build/reports/detekt/detekt.txt`, fix the code (never suppress wholesale). |
| Android Lint | `lintDebug` on `:app`, `:wear`, `:core:data`, `:core:designsystem` | Lint errors (`abortOnError = true`; version-nag checks disabled). | `<module>/build/reports/lint-results-debug.html` |
| Unit, UI and screenshot tests | `:core:domain:jvmTest`, `:shared:jvmTest`, `testDebugUnitTest` on `:core:data`, `:core:designsystem`, `:app`, `:wear` | All JVM/Robolectric tests pass, every screenshot matches its golden, and the screenshot tests' accessibility checks find no errors. | `<module>/build/reports/tests/...`; for screenshots see below. |
| Architecture | `:architecture-test:test` | Konsist rules: domain purity, presentation isolation (no data layer, no Media3, in `:shared` and `:wear`), ViewModel/UiState shape (in a presentation package, `@HiltViewModel` in the Android apps, no `Context`, no public mutable flows), `*Test` naming, design tokens only in `:app` and `:wear` (no color or dp/sp literals outside their theme packages), `:core:designsystem` free of Material and our own layers, its reference palettes read by theme code only, no fonts in the apps' resources, Wear Material 3 only on the watch and no Wear libraries on the phone. | `architecture-test/src/test/kotlin/...` states each rule. |
| iOS compiles | `compileKotlinIosSimulatorArm64` on `:core:domain` and `:shared` | The multiplatform code compiles for iOS (klibs cross-compile on Linux; linking the framework needs macOS). | The compiler names the JVM-only API used in common code. |
| Coverage | `koverVerify` per module + root `:koverVerify` | Line **and** branch floors for every module and for the aggregate (see below). | `./gradlew :koverHtmlReport` then open `build/reports/kover/html/index.html`. |

### Coverage floors

Set in `coverageFloors` in the root `build.gradle.kts` (lines / branches, %):

| Scope | Floor |
| --- | --- |
| `:core:domain` | 96 / 92 |
| `:shared` | 90 / 45 |
| `:core:data` | 91 / 76 |
| `:core:designsystem` | 94 / 45 |
| `:app` | 75 / 35 |
| `:wear` | 80 / 50 |
| aggregate | 87 / 64 |

Floors sit a little under the measured values so a change can't quietly drop coverage; raise them as
tests grow. The UI modules' branch numbers include the Compose compiler's generated recomposition
branches, hence their lower branch floors.

Notes:

- One Kover setup in the root build applies to every module: generated/DI glue (`*_Factory*`,
  `Hilt_*`, `*.di.*`, R/BuildConfig, `@Preview` composables) and Android entry points (matched by
  `@AndroidEntryPoint`/`@HiltAndroidApp`, which Hilt-rewritten classes keep) are excluded.
- Coverage is measured on the debug variant; release unit tests stay uninstrumented.
- The Konsist tests read all modules' sources; Gradle doesn't know that, so if you only changed
  sources of another module, force a re-run: `./gradlew :architecture-test:test --rerun-tasks`.

## Screenshot tests

Two kinds, both Roborazzi, both compared with committed goldens so the gate fails when pixels
change.

**The phone UI (`:shared`)** renders on Compose Desktop, through Skia as iOS does, so it needs no
Robolectric. Goldens live in `shared/src/jvmTest/screenshots/`.

- Write one in `src/jvmTest` with `phoneSnapshot("feature_state", night = true) { … }`: it runs
  `runDesktopComposeUiTest` in a phone-sized window inside `CelestialTheme`, holds the sky's clock
  still (so the stars and the lamp are the same on every run) and captures the root
  (`onRoot().captureRoboImage(...)`). Snapshot the stateless screen with a hand-built `UiState`.
- Record after an intended UI change, review the PNGs, then commit:
  `./gradlew :shared:recordRoborazziJvm`. Verify with `./gradlew :shared:verifyRoborazziJvm`.
- `LampFramesRecorderTest` is not a check: with `LAMP_FRAMES` set it writes the sky and the lamp's
  frames to `shared/build/lamp-frames` for reviewing the motion as a video
  (`LAMP_FRAMES=1 ./gradlew :shared:jvmTest --tests '*LampFramesRecorder*'`).

**The watch (`:wear`) and `:core:designsystem`'s specimens** render under Robolectric, with goldens
in `<module>/src/test/screenshots/`, verified on every test run (`roborazzi.test.verify=true` in
`gradle.properties`). `:app` has no screenshot tests: it only hosts the shared UI.

- Write one: in a Robolectric test with `@GraphicsMode(GraphicsMode.Mode.NATIVE)` and a round
  watch `@Config(qualifiers = RobolectricDeviceQualifiers.WearOSLargeRound)` (or
  `WearOSSmallRound`), call `composeRule.wearSnapshot("feature_state") { … }`. It also runs the
  accessibility checks (touch targets, contrast, labels) and fails on errors.
- Re-record after an intended change: `./gradlew recordRoborazziDebug` (or
  `:wear:recordRoborazziDebug --tests '*WearScreensScreenshotTest*'`). A failed comparison leaves
  compare images under `<module>/build/outputs/roborazzi/`.

Small anti-aliasing differences between machines are tolerated (1 %). If CI's Linux rendering
drifts further, run the CI workflow by hand with **record** checked and commit the goldens from its
artifact.

## Pre-commit hook

The hook runs the two fast gates (`spotlessCheck detekt`) before every commit:

```
scripts/install-git-hooks.sh     # once per clone (sets git config core.hooksPath)
```

On failure it prints the fix (`./gradlew spotlessApply`); bypass with
`SKIP_QUALITY_HOOK=1 git commit ...` for WIP commits.
