import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    id("java")
    kotlin("jvm") version "2.4.20"
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.kotlinx.kover") version "0.9.11"
}

// Minimum supported IDE (matches since-build in plugin.xml); the plugin compiles against it.
val platformVersion = providers.gradleProperty("platformVersion").get()
// Newest released IDE; tests and verification also run against it.
val platformVersionLatest = providers.gradleProperty("platformVersionLatest").get()

// Version is set by the publish workflow from the git tag (e.g. -PpluginVersion=2.2.0).
// Local/CI builds use a dev placeholder; the real version is determined by go-semantic-release.
version = findProperty("pluginVersion")?.toString() ?: "0.0.0-dev"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        // Restrict stdlib API usage to what the minimum supported IDE (2025.3) bundles.
        // The plugin runs against the IDE-provided Kotlin stdlib, never its own copy.
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
    }
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xms4g", "-Xmx4g", "-Dcom.sun.management.jmxremote")
}

intellijPlatform {
    pluginConfiguration {
        changeNotes = project.provider { changelogToHtml() }
    }
    publishing {
        // Supplied by the release workflow as an environment variable (or locally via -PpublishToken).
        token = providers.gradleProperty("publishToken").orElse(providers.environmentVariable("publishToken"))
    }
    pluginVerification {
        ides {
            // Verify against both ends of the supported range: the minimum IDE the plugin builds
            // against and the newest release. recommended() would download many more IDEs.
            create(IntelliJPlatformType.IntellijIdea, platformVersion)
            create(IntelliJPlatformType.IntellijIdea, platformVersionLatest)
        }
        // The plugin ID predates the JetBrains rule against 'intellij' in plugin IDs.
        // This mutes the check so existing marketplace listings are not broken.
        freeArgs = listOf("-mute", "TemplateWordInPluginId")
    }
}

// Runs the full test suite against the newest released IDE, in addition to `test`
// (which runs against the minimum supported IDE).
intellijPlatformTesting {
    testIde {
        register("testLatestIde") {
            type = IntelliJPlatformType.IntellijIdea
            version = platformVersionLatest
            testFramework(TestFrameworkType.Platform)
            task {
                useJUnit()
            }
        }
    }
}

kover {
    reports {
        total {
            verify {
                rule {
                    minBound(90)
                }
            }
        }
    }
}

tasks {
    withType<Test> {
        useJUnit()
    }
    check {
        dependsOn("testLatestIde")
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
        intellijIdea(platformVersion)
        testFramework(TestFrameworkType.Platform)
    }
    // Pin the test-time Kotlin stdlib to the compiler version: the IntelliJ test runtime requires
    // Kotlin 2.2+ APIs, while test libraries can transitively pull in older stdlib versions.
    testImplementation(kotlin("stdlib"))
    // Workaround IJPL-157292: TestFrameworkType.Platform no longer provides opentest4j transitively.
    testImplementation("org.opentest4j:opentest4j:1.3.0")
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

/**
 * Converts CHANGELOG.md (Keep a Changelog format) to an HTML fragment for
 * the JetBrains Marketplace change-notes field. Skips the [Unreleased]
 * section and produces a nested `<ul>` matching the format JetBrains renders.
 */
fun changelogToHtml(): String {
    val file = rootProject.file("CHANGELOG.md")
    if (!file.exists()) return ""

    val html = StringBuilder("<ul>\n")
    var currentVersion = ""
    val items = mutableListOf<String>()
    var active = false

    fun flush() {
        if (currentVersion.isEmpty() || items.isEmpty()) {
            items.clear()
            return
        }
        if (items.size == 1) {
            html.appendLine("    <li>$currentVersion: ${items[0]}</li>")
        } else {
            html.appendLine("    <li>$currentVersion:")
            html.appendLine("        <ul>")
            items.forEach { html.appendLine("            <li>$it</li>") }
            html.appendLine("        </ul>")
            html.appendLine("    </li>")
        }
        items.clear()
    }

    for (line in file.readLines()) {
        when {
            line.startsWith("## [Unreleased]") -> active = false
            line.startsWith("## [") -> {
                flush()
                currentVersion = line.substringAfter("[").substringBefore("]")
                active = true
            }
            !active -> continue
            line.startsWith("### ") -> { /* skip category headings */ }
            line.startsWith("- ") -> items.add(markdownLineToHtml(line.removePrefix("- ")))
        }
    }
    flush()
    html.append("</ul>")
    return html.toString()
}

/** Converts inline Markdown (bold, links) to HTML with proper escaping. */
fun markdownLineToHtml(text: String): String {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace(Regex("\\*\\*(.+?)\\*\\*"), "<b>$1</b>")
        .replace(Regex("\\[(.+?)]\\((.+?)\\)"), "<a href=\"$2\">$1</a>")
}
