import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishPluginTask
import org.jetbrains.intellij.tasks.RunIdeTask

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.mikejhill"
version = "2.1.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

intellij {
    pluginName.set("MoveTab")
    type.set("IC")
    version.set("232.10072.27")
    updateSinceUntilBuild.set(false) // Configure sinceBuild/untilBuild compatibility manually
}

val patchPluginXml by tasks.existing(PatchPluginXmlTask::class) {
    pluginId.set("com.mikejhill.intellij.movetab")
    sinceBuild.set("232")
    changeNotes.set(project.provider { project.file("docs/CHANGELOG.html").readText() })
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
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.mockk:mockk:1.13.8")
}
