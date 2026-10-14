# Architecture

This document describes the design and architecture of the IntelliJ MoveTab
plugin.

## Overview

MoveTab is a lightweight IntelliJ IDEA plugin that adds keyboard-driven editor
tab reordering. The plugin registers four editor actions and a single settings
panel. It has no external runtime dependencies beyond the IntelliJ Platform SDK.

## Components

### Actions (`src/main/kotlin/.../actions/`)

All tab movement logic is encapsulated in a small action hierarchy:

- **`MoveTab`** — Abstract base class extending `AnAction`. It locates the
  active editor tab within its `EditorWindow`, asks `TabReorder` for the target
  index, and reorders the `JBEditorTabs` component. The action is enabled only
  when a project is available and updates on a background thread (`BGT`).
- **`TabReorder`** — Pure, UI-independent reordering rules (target index for a
  direction and wrap-around setting, plus list reordering). Kept free of
  platform types so it can be unit tested exhaustively without an IDE.
- **`MoveTabLeft`** / **`MoveTabRight`** — Concrete actions that move the active
  tab one position left or right. These are the primary actions with default
  keyboard shortcuts.
- **`MoveTabToStart`** / **`MoveTabToEnd`** — Concrete actions that move the
  active tab to the first or last position. These have no default shortcuts and
  must be assigned by the user.

Each concrete action only passes its direction to the `MoveTab` constructor.
The actions are registered in `plugin.xml` (not added to any menu group) and
are invoked through keyboard shortcuts.

### Settings (`src/main/kotlin/.../settings/`)

- **`MoveTabSettings`** — Singleton that persists user preferences via
  `PropertiesComponent`. Currently exposes a single setting: whether Move Tab
  Left/Right should wrap around at the edges of the tab bar.
- **`MoveTabConfigurable`** — A `BoundConfigurable` built with the Kotlin UI
  DSL that provides the settings page at **Settings > Editor > Move Tab**.

### Plugin Descriptor (`src/main/resources/META-INF/plugin.xml`)

Declares all actions, their default keyboard shortcuts, and the settings
extension point. The plugin depends only on `com.intellij.modules.platform`,
making it compatible with all IntelliJ-based IDEs.

## Build and Packaging

The project uses the
[IntelliJ Platform Gradle Plugin](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html)
(IJPGP 2.x) for building, testing, and publishing.

Key build details:

- **Single-module layout** — Avoids classloading issues with IJPGP 2.x
  multi-module builds (see the
  [Project Structure section in the README](../README.md#project-structure)).
- **Plugin icon** — `docs/pluginIcon.svg` is copied into `META-INF/` at build
  time via a `processResources` configuration in `build.gradle.kts`.
- **Searchable options** — Disabled; the settings UI is simple enough that
  indexing adds no value.
- **Kotlin stdlib** — Excluded from the plugin bundle
  (`kotlin.stdlib.default.dependency = false`). IntelliJ provides the stdlib at
  runtime.
- **Changelog** — `CHANGELOG.md` (Keep a Changelog format) is the single source
  of truth. The `changelogToHtml()` function in `build.gradle.kts` converts it
  to an HTML fragment for the JetBrains Marketplace `change-notes` field at
  build time. The `[Unreleased]` section is skipped; only released versions
  appear in the plugin.

## Testing

Tests run twice: `test` targets the minimum supported IDE (`platformVersion`)
and `testLatestIde` targets the newest release (`platformVersionLatest`); both
are wired into `check`. Kover measures coverage across both runs and `check`
fails if line coverage drops below 90%.

- **Unit** — `TabReorderTest` checks every tab-bar size up to 8, every position,
  direction, and wrap setting against an independent reference model, plus
  invariants (no tabs lost, relative order preserved, full-cycle identity).
- **Integration** — `MoveTabTest` opens real editors via `FileEditorManagerImpl`
  and invokes the registered actions, asserting tab order and selection.
- **Settings** — `MoveTabSettingsTest` and `MoveTabConfigurableTest` cover
  persistence, the stable storage key, and the settings page bindings.
- **Descriptor** — `PluginDescriptorTest` asserts action IDs, texts, default
  shortcuts, and that the running IDE satisfies `since-build`.

## CI/CD Pipeline

Three GitHub Actions workflows automate the development lifecycle:

| Workflow | Trigger | Purpose |
| --- | --- | --- |
| **Build and Verify Plugin** | Push, PR, merge group | Lint markdown, validate PR titles, build, test on minimum and latest IDEs, coverage, verify plugin, predict next version |
| **Release and Publish** | Manual (`workflow_dispatch`) | Determines version (dry-run), validates build, creates tag, GitHub Release, publishes to JetBrains Marketplace |
| **CodeQL Analysis** | Push, PR, weekly schedule | Static security analysis for Java/Kotlin |

The CI workflow runs parallel jobs (`commit-lint`, `markdown`, `build`) with a
`ci-pass` aggregator job serving as the single required status check for branch
protection. The release workflow validates the build before creating any tags
and publishes via an environment-gated job (`jetbrains-marketplace`).

## Design Decisions

Architecture Decision Records (ADRs) are stored in [`decisions/`](decisions/).
New significant design decisions should be documented there using the standard
ADR format.
