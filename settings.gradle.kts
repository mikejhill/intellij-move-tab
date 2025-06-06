pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.19.2"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
