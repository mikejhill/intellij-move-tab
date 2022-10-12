import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishPluginTask
import org.jetbrains.intellij.tasks.RunIdeTask

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath(kotlin("gradle-plugin", version = "1.7.20")) }
}

repositories {
    mavenCentral()
}

plugins {
    base
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.intellij") version "1.9.0"
}

group = "com.mikejhill"
version = "1.4.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

intellij {
    pluginName.set("MoveTab")
    type.set("IU")
    version.set("LATEST-EAP-SNAPSHOT")
}

val patchPluginXml by tasks.existing(PatchPluginXmlTask::class) {
    pluginId.set("com.mikejhill.intellij.movetab")
    sinceBuild.set("203")
    /*
     * Support in perpetuity so that the "since-build" attribute does not need to be updated for every major IDE
     * release. Due to the simple nature of this plugin, it is rare for IDE updates to break functionality. To reduce
     * maintenance costs, we unset this property so that the "until-build" attribute is excluded, preventing users from
     * having to wait for a plugin update each time that a new IDE major version is released.
     */
    untilBuild(null)
    changeNotes.set(project.provider { project.file("docs/CHANGELOG.html").readText() })
}

val publishPlugin by tasks.existing(PublishPluginTask::class) {
    token.set(getPropertyValue("publishToken"))
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
}

tasks.withType<RunIdeTask> {
    jvmArgs("-Xms4g", "-Xmx4g", "-Dcom.sun.management.jmxremote")
}

fun getPropertyValue(name: String): String? {
    return if (project.extra.has(name)) project.extra[name]?.toString() else System.getenv(name)
}
