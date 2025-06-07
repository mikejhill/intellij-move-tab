import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    kotlin("jvm") version "2.2.0-RC2"
    id("org.jetbrains.intellij.platform.module")
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

intellijPlatform {
    pluginConfiguration {
        changeNotes = project.provider { rootProject.file("docs/CHANGELOG.html").readText() }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1.2")
        testFramework(TestFrameworkType.JUnit5)
    }
    testImplementation("io.mockk:mockk:1.14.2")
    // Required for BasePlatformTestCase (see FAQ about JUnit5 tests referring to JUnit4
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-faq.html#junit5-test-framework-refers-to-junit4)
    testImplementation("junit:junit:4.13.2")
}
