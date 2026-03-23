# AGENTS Instructions

* Gradle commands are time-consuming because they download large dependencies. Avoid running any Gradle commands (such as `gradle` or `./gradlew`) in this repository.
* If Gradle commands are necessary, ensure the Gradle Wrapper (`./gradlew`) is always used instead of `PATH`-based Gradle (`gradle`).
* There are no mandatory programmatic checks for this repository.
* Use common prefixes across all modules within multi-module projects. The prefix for this project is `move-tab`; modules should be named using this prefix (e.g. `move-tab-plugin`).
* `AGENTS.md` should be continually updated when new or helpful standards or conventions are brought up during interactions.
* Follow `.editorconfig` conventions when adding or modifying files.
* Code scanning with CodeQL is configured in `.github/workflows/codeql.yml` and uses Java 21. The CodeQL config file (`.github/codeql/codeql-config.yml`) is referenced by that workflow; keep the Java version in the workflow in sync with the build toolchain version in `move-tab-plugin/build.gradle.kts`.

## Build Toolchain

* **Gradle**: 9.4.1 (via wrapper)
* **IntelliJ Platform Gradle Plugin**: 2.13.1 (requires Gradle >= 9.0)
* **Kotlin JVM**: 2.2.21 (must remain below 2.3.20 until CodeQL adds support for it)
* **Java toolchain**: 21 (temurin)
* **Target IDE**: IntelliJ IDEA Community 2025.1.2 (minimum supported: 2025.1 / build 251)
* **Plugin verification**: runs against 2025.1.2 only (single explicit IDE, not `recommended()`)

## Dependency Notes

* `org.gradle.internal.deprecation.DeprecatableConfiguration` was removed in Gradle 9. Do not use internal Gradle APIs; use only stable public Gradle APIs.
* The `resolveDependencies` task in `move-tab-plugin/build.gradle.kts` resolves all configurations via `doLast` (it is intentionally incompatible with the configuration cache).

## Commit Messages

* Use the [semantic-release](https://github.com/semantic-release/semantic-release) style.
* Format each commit subject as `type(scope): message`.
* Allowed types are `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, and `revert`.
* Keep the message in the present tense and start it with a lowercase letter. Example: `feat(release): add predicted version`