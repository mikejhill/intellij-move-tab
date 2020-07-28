import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishTask

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath(kotlin("gradle-plugin", version = "1.3.72")) }
}

repositories {
    mavenCentral()
}

plugins {
    base
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.intellij") version "0.4.21"
}

group = "com.mikejhill"
version = "1.2.0"

intellij {
    pluginName = "MoveTab"
    type = "IC"
    version = "LATEST-EAP-SNAPSHOT"
}

val patchPluginXml by tasks.existing(PatchPluginXmlTask::class) {
    pluginId("com.mikejhill.intellij.movetab")
    sinceBuild("201.6668")
    changeNotes("<![CDATA[" + file("$projectDir/docs/CHANGELOG.html").readText() + "]]>")
}

val publishPlugin by tasks.existing(PublishTask::class) {
    setToken(getPropertyValue("publishToken"))
    setUsername(getPropertyValue("publishUsername"))
    setPassword(getPropertyValue("publishPassword"))
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
}


fun getPropertyValue(name: String): String? {
    return if (project.extra.has(name)) project.extra[name]?.toString() else System.getenv(name)
}
