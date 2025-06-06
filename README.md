# IntelliJ Move Tab

![Build and Verify Plugin](https://github.com/mikejhill/intellij-move-tab/workflows/Build%20and%20Verify%20Plugin/badge.svg)
![Publish Plugin](https://github.com/mikejhill/intellij-move-tab/workflows/Publish%20Plugin/badge.svg)

Quickly reorder your editor tabs with simple keyboard shortcuts.

<img src="src/main/resources/META-INF/plugin-icon.svg" alt="Plugin icon" width="80"/>

Plugin page: https://plugins.jetbrains.com/plugin/13087-movetab

### Usage
* **Ctrl+Shift+Page Up** – move the current tab to the left
* **Ctrl+Shift+Page Down** – move the current tab to the right

```mermaid
flowchart LR
    A[Key Press] --> B{Direction}
    B --> |Page Up| C[Move Left]
    B --> |Page Down| D[Move Right]
```

Based on the original plugin by [momomo.com](https://plugins.jetbrains.com/plugin/8443-a-move-tab-left-and-right-using-the-keyboard-plugin--by-momomo-com).

### Build

Run `./gradlew :move-tab-plugin:verifyPlugin` to build and test the plugin.
