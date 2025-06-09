# AGENTS Instructions

* Gradle commands are time-consuming because they download large dependencies. Avoid running any Gradle commands (such as `gradle` or `./gradlew`) in this repository.
* If Gradle commands are necessary, ensure the Gradle Wrapper (`./gradlew`) is always used instead of `PATH`-based Gradle (`gradle`).
* There are no mandatory programmatic checks for this repository.
* Use common prefixes across all modules within multi-module projects. The prefix for this project is `move-tab`; modules should be named using this prefix (e.g. `move-tab-plugin`).
* `AGENTS.md` should be continually updated when new or helpful standards or conventions are brought up during interactions.
* Follow `.editorconfig` conventions when adding or modifying files.
* Code scanning with CodeQL requires Java 21. A CodeQL configuration file (`.github/codeql/codeql-config.yml`) sets this version, so keep it in sync with the build.

## Commit Messages

* Use the [semantic-release](https://github.com/semantic-release/semantic-release) style.
* Format each commit subject as `type(scope): message`.
* Allowed types are `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, and `revert`.
* Keep the message in the present tense and start it with a lowercase letter. Example: `feat(release): add predicted version`
