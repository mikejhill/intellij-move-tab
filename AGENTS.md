# AGENTS Instructions

* Gradle commands are time-consuming because they download large dependencies. Avoid running any Gradle commands (such as `gradle` or `./gradlew`) in this repository.
* If Gradle commands are necessary, ensure the Gradle Wrapper (`./gradlew`) is always used instead of `PATH`-based Gradle (`gradle`).
* There are no mandatory programmatic checks for this repository.
* `AGENTS.md` should be continually updated when new or helpful standards or conventions are brought up during interactions.
* Follow `.editorconfig` conventions when adding or modifying files.
* Code scanning with CodeQL is configured in `.github/workflows/codeql.yml` and uses Java 21. The CodeQL config file (`.github/codeql/codeql-config.yml`) is referenced by that workflow; keep the Java version in the workflow in sync with the build toolchain version in `build.gradle.kts`.

## Build Toolchain

* **Gradle**: 9.4.1 (via wrapper)
* **IntelliJ Platform Gradle Plugin**: 2.13.1 (requires Gradle >= 9.0)
* **Kotlin JVM**: 2.2.21 (must remain below 2.3.20 until CodeQL adds support for it)
* **Java toolchain**: 21 (temurin)
* **Target IDE**: IntelliJ IDEA 2025.3.4 (minimum supported: 2025.3 / build 253)
* **Plugin verification**: runs against 2025.3.4 only (single explicit IDE, not `recommended()`)

## IntelliJ 2025.3 / IC-IU Merger

Starting with IntelliJ IDEA 2025.3 (build 253), Community and Ultimate editions were merged into a single distribution:

* Use `intellijIdea("version")` — **not** `intellijIdeaCommunity()` or `intellijIdeaUltimate()`
* Use `IntelliJPlatformType.IntellijIdea` — **not** `IntelliJPlatformType.IntellijIdeaCommunity`
* The `ideaIC` Maven artifact no longer exists for 253+; use `idea` (`IU` product code)

## Kotlin Stdlib and Test Classpath

* `kotlin.stdlib.default.dependency = false` in `gradle.properties` prevents the Kotlin Gradle Plugin from auto-adding the stdlib (correct — IntelliJ provides it at runtime).
* `mockito-kotlin` transitively brings in `kotlin-reflect:2.1.20` → `kotlin-stdlib:2.1.20`. IntelliJ 2025.3+ requires `SequencesKt.sequenceOf(T)` (non-vararg, added in Kotlin 2.2.x). An explicit `testImplementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")` in the root `build.gradle.kts` forces Gradle to resolve the conflict to 2.2.21.

## Plugin Icon

* `pluginIcon.svg` lives in `docs/` (alongside `CHANGELOG.html`) as a project-level asset.
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

* `jackson-core` is a transitive dep supplied by IntelliJ; it cannot be updated independently. An `ignore` rule in `.github/dependabot.yml` prevents Dependabot from generating failing security-update jobs for it.

## Commit Messages

* Use the [semantic-release](https://github.com/semantic-release/semantic-release) style.
* Format each commit subject as `type(scope): message`.
* Allowed types are `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, and `revert`.
* Keep the message in the present tense and start it with a lowercase letter. Example: `feat(release): add predicted version`
