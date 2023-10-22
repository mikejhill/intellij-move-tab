pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.15.1"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "1.11.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "intellij-move-tab"

gradleEnterprise {
    buildScan {
        capture.isTaskInputFiles = true
        isUploadInBackground = true
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishAlways()
        obfuscation {
            hostname { "<obfuscated>" }
            ipAddresses { listOf("<obfuscated>") }
            username { "<obfuscated>" }
        }
    }
}
