# AGENTS Instructions

* Gradle commands are time-consuming because they download large dependencies (two full IDEs for testing and verification). Avoid running Gradle commands unless needed to validate a change; when you do, prefer targeted tasks (e.g. `./gradlew test`).
* If Gradle commands are necessary, ensure the Gradle Wrapper (`./gradlew`) is always used instead of `PATH`-based Gradle (`gradle`).
* Before pushing, lint Markdown files with `npx markdownlint-cli2 "**/*.md"`. CI enforces this via the `DavidAnson/markdownlint-cli2-action` in `verify-plugin.yml`.
* `AGENTS.md` should be continually updated when new or helpful standards or conventions are brought up during interactions.
* Follow `.editorconfig` conventions when adding or modifying files.
* Code scanning with CodeQL is configured in `.github/workflows/codeql.yml` and uses Java 21. The CodeQL config file (`.github/codeql/codeql-config.yml`) is referenced by that workflow; keep the Java version in the workflow in sync with the build toolchain version in `build.gradle.kts`.

## Build Toolchain

* **Gradle**: 9.8.1 (via wrapper, distribution checksum pinned)
* **IntelliJ Platform Gradle Plugin**: 2.19.0
* **Kotlin JVM**: 2.4.20 (keep within the range CodeQL supports, currently up to 2.4.20; check the [CodeQL supported compilers](https://codeql.github.com/docs/codeql-overview/supported-languages-and-frameworks/) page before bumping)
* **Kotlin `apiVersion`/`languageVersion`**: 2.2, matching the stdlib bundled with the minimum supported IDE (2025.3). Raise only when `since-build` is raised.
* **Java toolchain**: 21 (temurin); bytecode must stay at 21 while 2025.3 is supported
* **IDE versions** (`gradle.properties`): `platformVersion` is the minimum supported IDE the plugin compiles and tests against (keep in sync with `since-build`); `platformVersionLatest` is the newest released IDE, used by `testLatestIde` and plugin verification. Bump `platformVersionLatest` when JetBrains ships a release.
* **Plugin verification**: runs against `platformVersion` and `platformVersionLatest` (explicit IDEs, not `recommended()`)
* **Coverage**: Kover; `check` fails below 90% line coverage across both test runs

## IntelliJ 2025.3 / IC-IU Merger

Starting with IntelliJ IDEA 2025.3 (build 253), Community and Ultimate editions were merged into a single distribution:

* Use `intellijIdea("version")` — **not** `intellijIdeaCommunity()` or `intellijIdeaUltimate()`
* Use `IntelliJPlatformType.IntellijIdea` — **not** `IntelliJPlatformType.IntellijIdeaCommunity`
* The `ideaIC` Maven artifact no longer exists for 253+; use `idea` (`IU` product code)

## Kotlin Stdlib and Test Classpath

* `kotlin.stdlib.default.dependency = false` in `gradle.properties` prevents the Kotlin Gradle Plugin from auto-adding the stdlib (correct — IntelliJ provides it at runtime).
* The IntelliJ test runtime requires Kotlin 2.2+ stdlib APIs (e.g. non-vararg `SequencesKt.sequenceOf(T)`). `testImplementation(kotlin("stdlib"))` pins the test-time stdlib to the compiler version so older transitive stdlib versions cannot win.

## Testing

* `test` runs against `platformVersion`; `testLatestIde` (an `intellijPlatformTesting.testIde` task) runs the same tests against `platformVersionLatest`. `check` depends on both.
* Custom `testIde` tasks must declare `testFramework(TestFrameworkType.Platform)` inside their own block; it is not inherited.
* `org.jetbrains.intellij.platform.testIdeBundledPluginsClasspathEnabled = false` (in `gradle.properties`) is required: for IDE 2026.2+ IJPGP otherwise adds all bundled plugins to the test classpath, including the licensed Ultimate module, which fails test-application startup.
* Keep reordering logic in `TabReorder` (pure Kotlin) and cover it with plain JUnit tests; use platform tests only for behavior that needs the IDE.

## Plugin Icon

* `pluginIcon.svg` lives in `docs/` as a project-level asset.
* A `processResources` block in the root `build.gradle.kts` copies it into `META-INF/` at build time — JetBrains requires it there in the final plugin JAR.

## Project Structure

This is a single-module project. All Kotlin sources live in `src/main/kotlin/` and tests in `src/test/kotlin/` at the root level.

> **Why not multi-module?** IJPGP 2.x multi-module builds place sub-module classes in `lib/modules/*.jar`. IntelliJ 2025.3+ does not auto-load those JARs without a `<content>` declaration in `plugin.xml`. However, the IntelliJ test framework (`HeavyPlatformTestCase`) does not process `<content>` elements, so the module's classes are not found at test time. A single-module layout avoids this classloading mismatch entirely.

## Plugin Structure

* **Actions**: `MoveTabLeft`, `MoveTabRight`, `MoveTabToStart`, `MoveTabToEnd` — all extend `MoveTab` in `src/main/kotlin/com/mikejhill/intellij/movetab/actions/`
* **Settings**: `MoveTabSettings` (singleton backed by `PropertiesComponent`) and `MoveTabConfigurable` (settings UI) in `.../settings/`
* **plugin.xml**: Lives at `src/main/resources/META-INF/plugin.xml` in the root project. `patchPluginXml` operates on this file. All `<actions>` and `<extensions>` are declared here directly (no `<content>` module split).
* **Default shortcuts**: Move Left = `Ctrl+Shift+Page Up`, Move Right = `Ctrl+Shift+Page Down`. Move to Start and Move to End have no defaults (users assign via Keymap).

## Dependabot

* Dependabot alerts with manifest `settings.gradle.kts` are build-classpath dependencies of the settings plugins (mostly IJPGP), not plugin runtime dependencies. Fix them by upgrading the plugins first; if still vulnerable, add a floor in the `buildscript` block of `settings.gradle.kts` (the `jackson-bom` platform and `jsoup` constraint live there). Inspect the settings classpath with an init script using `settingsEvaluated { buildscript.configurations.getByName("classpath") ... }`; `./gradlew buildEnvironment` only shows the project classpath.
* Test-only libraries pulled in by the IntelliJ test framework (Jackson 2/3, OpenTelemetry) are floored by the `configurations.configureEach { resolutionStrategy.eachDependency { ... } }` block in `build.gradle.kts`, and Kover's FreeMarker by the `buildscript` constraint there. Find offenders with `./gradlew -q dependencies` (includes `intellijPlatformTest*` configurations) and `./gradlew buildEnvironment`.
* `ignore` rules in `.github/dependabot.yml` suppress unapplicable security-update jobs for individual transitive modules (e.g. `jackson-core`); version updates are grouped into minor/patch PRs per ecosystem.

## Versioning and Release

* Versioning is fully automated via [go-semantic-release](https://github.com/go-semantic-release/action), which computes the next version from conventional commits since the last git tag.
* `build.gradle.kts` uses `findProperty("pluginVersion") ?: "0.0.0-dev"` — the real version is injected by the publish workflow from the git tag.
* **Do not hardcode versions** in `build.gradle.kts`. The release workflow determines the version via dry-run, validates the build, then creates a `vX.Y.Z` tag and passes it to Gradle via `-PpluginVersion=X.Y.Z`.
* `release.yml` (triggered by `workflow_dispatch` on `master`) has two jobs: **Release** (version, build, tag, GitHub Release, changelog sync) and **Publish** (pushes to JetBrains Marketplace using the `jetbrains-marketplace` environment, which requires reviewer approval). A `dry-run` input validates the full pipeline without creating tags or publishing.
* `master` is protected (PR + `ci-pass` required), so the release workflow never pushes to it. It pushes only the `vX.Y.Z` tag (on a commit containing the promoted CHANGELOG), then opens a `release/vX.Y.Z` PR (built from current `master`) that a maintainer must merge. Bot-opened PRs need their CI run approved (or the PR closed and reopened) before `ci-pass` reports; `workflow_dispatch` runs do not count toward a PR's required checks. The next release refuses to start while a `release/v*` PR is open. Because the tagged commit is not on `master`'s history, go-semantic-release runs with `--provider-opt github_use_compare_commits=true` (in both workflows).
* If a release fails after its tag is pushed, re-run the workflow with `resume-tag: vX.Y.Z` to build that tag, create the GitHub Release, sync the CHANGELOG, and publish.
* The changelog follows [Keep a Changelog](https://keepachangelog.com/) format in `CHANGELOG.md` at the project root. Add entries under `## [Unreleased]`. The release workflow promotes `[Unreleased]` to the new version heading in the tagged commit and in the `release/vX.Y.Z` sync PR. The `changelogToHtml()` function in `build.gradle.kts` converts it to HTML for the JetBrains Marketplace `change-notes` field.
* Tag protection rulesets: tag-create protection is **disabled** (the release workflow needs to create tags); tag-delete protection remains **active** to prevent accidental deletion.
* The CI workflow (`verify-plugin.yml`) uses a `ci-pass` aggregator job as the single required status check for branch protection. Update branch protection to require `ci-pass` (not `Build and Verify Plugin`).

## Commit Messages

* Use the [semantic-release](https://github.com/semantic-release/semantic-release) style.
* Format each commit subject as `type(scope): message`.
* Allowed types are `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, and `revert`.
* Keep the message in the present tense and start it with a lowercase letter. Example: `feat(release): add predicted version`
