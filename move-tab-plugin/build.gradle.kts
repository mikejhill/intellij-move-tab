import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishPluginTask
import org.jetbrains.intellij.tasks.RunIdeTask

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "2.2.0-RC2"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.mikejhill"
version = "2.2.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

intellij {
    pluginName.set("MoveTab")
    type.set("IC")
    version.set("251.26094.121")
    updateSinceUntilBuild.set(false) // Configure sinceBuild/untilBuild compatibility manually
}

val patchPluginXml by tasks.existing(PatchPluginXmlTask::class) {
    pluginId.set("com.mikejhill.intellij.movetab")
    sinceBuild.set("251")
    changeNotes.set(project.provider { rootProject.file("docs/CHANGELOG.html").readText() })
}

val publishPlugin by tasks.existing(PublishPluginTask::class) {
    token.set(getPropertyValue("publishToken"))
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xms4g", "-Xmx4g", "-Dcom.sun.management.jmxremote")
}


fun getPropertyValue(name: String): String? {
    return if (project.extra.has(name)) project.extra[name]?.toString() else System.getenv(name)
}

dependencies {
    testImplementation("io.mockk:mockk:1.14.2")
    // Required for BasePlatformTestCase (see FAQ about JUnit5 tests referring to JUnit4
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-faq.html#junit5-test-framework-refers-to-junit4)
    testImplementation("junit:junit:4.13.2")
}
