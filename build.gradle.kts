import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.PublishPluginTask
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    id("org.jetbrains.intellij.platform")
}

version = "2.2.0"

val publishPlugin by tasks.existing(PublishPluginTask::class) {
    token.set(getPropertyValue("publishToken"))
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xms4g", "-Xmx4g", "-Dcom.sun.management.jmxremote")
}

intellijPlatform {
    pluginConfiguration {
        changeNotes = project.provider { rootProject.file("docs/CHANGELOG.html").readText() }
    }
    pluginVerification {
        ides {
            // Verify against only the IDE version used to build. Using recommended() downloads
            // multiple IDEs across the compatibility range, making CI significantly slower.
            create(IntelliJPlatformType.IntellijIdea, "2025.3.4")
        }
        // The plugin ID predates the JetBrains rule against 'intellij' in plugin IDs.
        // This mutes the check so existing marketplace listings are not broken.
        freeArgs = listOf("-mute", "TemplateWordInPluginId")
    }
}

tasks {
    // This plugin has no configurable settings, so searchable options are unnecessary.
    buildSearchableOptions {
        enabled = false
    }
    prepareJarSearchableOptions {
        enabled = false
    }
    // The plugin icon lives in docs/ alongside other project-level assets.
    // Copy it into the plugin JAR's META-INF at build time.
    processResources {
        from(layout.projectDirectory.dir("docs")) {
            into("META-INF")
            include("pluginIcon.svg")
        }
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.3.4")
        pluginModule(implementation(project(":move-tab-plugin")))
    }
}


/* Utility functions */

fun getPropertyValue(name: String): String? {
    return if (project.extra.has(name)) project.extra[name]?.toString() else System.getenv(name)
}
