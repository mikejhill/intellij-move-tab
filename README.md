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
- **Gradle 9.4+** is provided via the Gradle Wrapper — always use `./gradlew`
  (or `gradlew.bat` on Windows)

### Build and Verify

```shell
./gradlew check verifyPlugin
```

This compiles the plugin, runs unit tests, and verifies the plugin descriptor
against IntelliJ IDEA 2025.3.4.

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
├── CHANGELOG.html    # Changelog shown on the JetBrains Marketplace
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
2. Update `docs/CHANGELOG.html` with the predicted version and a description of
   the changes.
3. Trigger the **Release and Publish** workflow manually from the `master` branch
   via GitHub Actions.
4. The workflow computes the next `vX.Y.Z` tag, creates a GitHub release with
   generated release notes, builds and verifies the plugin, and publishes it to
   the [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/13087-movetab).

> **Note:** The release and publish steps are combined in a single workflow
> (`release.yml`). This avoids the `GITHUB_TOKEN` limitation where tags created
> by GitHub Actions do not trigger other workflows.

## Contributing

This project uses
[Conventional Commits](https://www.conventionalcommits.org/). Format each commit
subject as `type(scope): message` — for example, `feat(actions): add move to
start`. Allowed types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`,
`test`, `build`, `ci`, `chore`, `revert`.

## Acknowledgments

Based on the original plugin by
[momomo.com](https://plugins.jetbrains.com/plugin/8443-a-move-tab-left-and-right-using-the-keyboard-plugin--by-momomo-com).

## License

[MIT](LICENSE) © Michael Hill
