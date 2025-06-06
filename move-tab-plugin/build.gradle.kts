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

tasks.withType<Test> {
    useJUnitPlatform()
}

fun getPropertyValue(name: String): String? {
    return if (project.extra.has(name)) project.extra[name]?.toString() else System.getenv(name)
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.0")
    testImplementation("io.mockk:mockk:1.14.2")
}
