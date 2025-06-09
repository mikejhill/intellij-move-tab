import org.jetbrains.intellij.platform.gradle.tasks.PublishPluginTask
import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    id("org.jetbrains.intellij.platform")
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
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1.2")
        pluginModule(implementation(project(":move-tab-plugin")))
    }
}


/* Utility functions */

fun getPropertyValue(name: String): String? {
    return if (project.extra.has(name)) project.extra[name]?.toString() else System.getenv(name)
}
