import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.PublishPluginTask
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    id("java")
    kotlin("jvm") version "2.2.21"
    id("org.jetbrains.intellij.platform")
}

version = "2.3.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

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
    withType<Test> {
        useJUnit()
    }
    // Searchable options index is disabled; the settings UI is simple enough that indexing adds no value.
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
        testFramework(TestFrameworkType.Platform)
    }
    // Force Kotlin stdlib to 2.2.21+: IntelliJ 2025.3 requires APIs added in Kotlin 2.2.x.
    // mockito-kotlin transitively brings in kotlin-stdlib:2.1.20 which lacks them.
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.21")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
    testImplementation("junit:junit:4.13.2")
}


tasks.register("resolveDependencies") {
    group = "custom"
    notCompatibleWithConfigurationCache("Dependency resolution must occur on each execution.")
    doLast {
        val count = project.configurations
            .filter { it.isCanBeResolved }
            .sumOf { config -> runCatching { config.resolve().size }.getOrElse { 0 } }
        println("Resolved all dependencies ($count files).")
    }
}


/* Utility functions */

fun getPropertyValue(name: String): String? {
    return if (project.extra.has(name)) project.extra[name]?.toString() else System.getenv(name)
}
