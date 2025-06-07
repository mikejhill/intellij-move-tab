import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version "4.0.2"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.jetbrains.intellij.platform.settings") version "2.6.0"
}

rootProject.name = "intellij-move-tab"

include("move-tab-plugin")

develocity {
    buildScan {
        capture { fileFingerprints.set(true) }
        uploadInBackground.set(true)
        termsOfUseUrl.set("https://gradle.com/terms-of-service")
        termsOfUseAgree.set("yes")
        publishing.onlyIf { true }
        obfuscation {
            hostname { "<obfuscated>" }
            ipAddresses { listOf("<obfuscated>") }
            username { "<obfuscated>" }
        }
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        intellijPlatform {
            defaultRepositories()
        }
    }
}
