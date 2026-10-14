# IntelliJ Move Tab

[![Build and Verify Plugin](https://github.com/mikejhill/intellij-move-tab/actions/workflows/verify-plugin.yml/badge.svg)](https://github.com/mikejhill/intellij-move-tab/actions/workflows/verify-plugin.yml)
[![Release and Publish](https://github.com/mikejhill/intellij-move-tab/actions/workflows/release.yml/badge.svg)](https://github.com/mikejhill/intellij-move-tab/actions/workflows/release.yml)

<img src="docs/pluginIcon.svg" alt="Plugin icon" width="80"/>

**MoveTab** is an IntelliJ IDEA plugin that lets you quickly reorder editor tabs
using keyboard shortcuts. Rearrange your open files without reaching for the
mouse.

**Plugin page:** <https://plugins.jetbrains.com/plugin/13087-movetab>

## Features

- **Move Tab Left / Right** — Shift the active editor tab one position in either direction.
- **Move Tab to Start / End** — Jump the active tab to the first or last position.
- **Wrap-around** — Optionally wrap tabs around when moving past the edges (enabled by default).
- **Configurable shortcuts** — All four actions can be remapped in **Settings > Keymap**.

## Installation

Install from the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/13087-movetab)
or search for **MoveTab** in **Settings > Plugins > Marketplace** within your IDE.

**Compatibility:** IntelliJ IDEA 2025.3 and later (build 253+). The plugin
targets the merged IC/IU distribution introduced in 2025.3.

## Usage

| Action | Default Shortcut |
| --- | --- |
| Move Tab Left | `Ctrl+Shift+Page Up` |
| Move Tab Right | `Ctrl+Shift+Page Down` |
| Move Tab to Start | *(none — assign via Settings > Keymap)* |
| Move Tab to End | *(none — assign via Settings > Keymap)* |

Wrap-around behavior for Move Tab Left/Right is configurable via
**Settings > Editor > Move Tab**.

> **Note:** The default shortcuts (`Ctrl+Shift+Page Up` / `Ctrl+Shift+Page Down`)
> conflict with IntelliJ's built-in **Move Caret to Page Top/Bottom with Selection**
> actions. Installing this plugin overrides those bindings when the editor is
> active. You can reassign either action in **Settings > Keymap** to restore them.

## Development

### Prerequisites

- **Java 21** (Eclipse Temurin recommended)
- **Gradle 9.8+** is provided via the Gradle Wrapper — always use `./gradlew`
  (or `gradlew.bat` on Windows)

### Build and Verify

```shell
./gradlew check verifyPlugin
```

This compiles the plugin, runs the full test suite against both the minimum
supported IDE (IntelliJ IDEA 2025.3) and the newest release (2026.2), enforces
a code-coverage floor with [Kover](https://github.com/Kotlin/kotlinx-kover), and
runs the IntelliJ Plugin Verifier against both IDEs. IDE versions are set in
`gradle.properties` (`platformVersion` and `platformVersionLatest`).

### Testing

| Layer | Location | What it covers |
| --- | --- | --- |
| Pure unit tests | `actions/TabReorderTest` | Exhaustive reordering rules for every size, position, direction, and wrap setting; no IDE needed |
| IDE integration tests | `actions/MoveTabTest` | Real editor tabs moved through the registered actions (`HeavyPlatformTestCase`) |
| Settings tests | `settings/*Test` | Persistence, storage-key stability, and the settings page bindings |
| Descriptor tests | `PluginDescriptorTest` | Action IDs, texts, default shortcuts, and settings registration from `plugin.xml` |

Useful tasks:

```shell
./gradlew test             # tests against the minimum supported IDE
./gradlew testLatestIde    # the same tests against the newest IDE release
./gradlew koverHtmlReport  # coverage report in build/reports/kover/html
```

### Project Structure

This is a single-module Gradle project. All production code lives under
`src/main/kotlin/` and tests under `src/test/kotlin/`.

```text
src/main/kotlin/com/mikejhill/intellij/movetab/
├── actions/          # Tab movement actions (MoveTab base + directional subclasses)
└── settings/         # Plugin settings state and configuration UI
src/main/resources/META-INF/
└── plugin.xml        # Plugin descriptor (actions, extensions, shortcuts)
docs/
├── architecture.md   # Design and architecture notes
├── decisions/        # Architecture Decision Records (ADRs)
└── pluginIcon.svg    # Plugin icon (copied into META-INF at build time)
```

> **Why single-module?** IntelliJ Platform Gradle Plugin 2.x multi-module builds
> place sub-module classes in `lib/modules/*.jar`. IntelliJ 2025.3+ does not
> auto-load those JARs without a `<content>` declaration in `plugin.xml`, and
> the IntelliJ test framework does not process `<content>` elements. A
> single-module layout avoids this classloading mismatch.

For a deeper look at how the plugin is structured, see
[docs/architecture.md](docs/architecture.md).

## Releasing

Versioning is fully automated with
[go-semantic-release](https://github.com/go-semantic-release/action), which
computes the next version from
[Conventional Commits](https://www.conventionalcommits.org/) since the last git
tag.

### How it works

1. Every push runs the **Build and Verify Plugin** workflow, which includes a
   "Predict Next Version" step showing what the next release version would be.
2. Add entries under `## [Unreleased]` in `CHANGELOG.md` as you work (follows
   [Keep a Changelog](https://keepachangelog.com/) format).
3. Trigger the **Release and Publish** workflow manually from the `master` branch
   via GitHub Actions. Use the **dry-run** option to validate the full pipeline
   without creating tags or publishing.
4. The workflow determines the next version, promotes `[Unreleased]` in the
   changelog, runs the full build and test suite, then pushes a `vX.Y.Z` tag and
   creates a GitHub Release with the plugin artifact attached. Because `master`
   is protected, it syncs the changelog back through a `release/vX.Y.Z` pull
   request that you merge (approve its CI run first). A separate environment-gated job
   publishes the plugin to the
   [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/13087-movetab).
   If a release fails after its tag is pushed, re-run the workflow with the
   **resume-tag** input (e.g. `v2.3.0`) to finish it.

> **Note:** The release workflow validates the build **before** creating any tags.
> If the build fails, no tag or release is created. `CHANGELOG.md` is
> automatically converted to HTML for the JetBrains Marketplace by the
> `changelogToHtml()` function in `build.gradle.kts`.

## Contributing

This project uses
[Conventional Commits](https://www.conventionalcommits.org/). Format each commit
subject as `type(scope): message` — for example, `feat(actions): add move to
start`. Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`,
`test`, `build`, `ci`, `chore`, `revert`. PR titles are validated automatically
by CI.

## Acknowledgments

Based on the original plugin by
[momomo.com](https://plugins.jetbrains.com/plugin/8443-a-move-tab-left-and-right-using-the-keyboard-plugin--by-momomo-com).

## License

[MIT](LICENSE) © Michael Hill
