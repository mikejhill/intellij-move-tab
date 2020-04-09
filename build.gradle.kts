import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.tasks.PublishTask

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath(kotlin("gradle-plugin", version = "1.3.61")) }
}

repositories {
    mavenCentral()
}

plugins {
    base
    kotlin("jvm") version "1.3.61"
    id("org.jetbrains.intellij") version "0.4.14"
}

group = "com.mikejhill"
version = "1.1.0"

intellij {
    pluginName = "MoveTab"
    type = "IU"
    version = "2020.1"
}

val patchPluginXml by tasks.existing(PatchPluginXmlTask::class) {
    pluginId("com.mikejhill.intellij.movetab")
    sinceBuild("201.6668")
    changeNotes(file("$projectDir/docs/CHANGELOG.html").readText())
}

val publishPlugin by tasks.existing(PublishTask::class) {
    val publishToken: String? = project.extra["publishToken"]?.toString() ?: System.getenv("publishToken")
    val publishUsername: String? = project.extra["publishUsername"]?.toString() ?: System.getenv("publishUsername")
    val publishPassword: String? = project.extra["publishPassword"]?.toString() ?: System.getenv("publishPassword")
    setToken(publishToken)
    setUsername(publishUsername)
    setPassword(publishPassword)
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
}
