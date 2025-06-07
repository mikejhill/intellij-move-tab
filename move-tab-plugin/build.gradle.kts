import org.gradle.internal.deprecation.DeprecatableConfiguration
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    kotlin("jvm") version "2.1.21"
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

tasks.withType<Test> {
    useJUnit()
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1.2")
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    // Required for BasePlatformTestCase (see FAQ about JUnit5 tests referring to JUnit4
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-faq.html#junit5-test-framework-refers-to-junit4)
    testImplementation("junit:junit:4.13.2")
}

tasks.register("resolveDependencies") {
    group = "custom"
    notCompatibleWithConfigurationCache("Dependency resolution must occur on each execution.")
    val dependencies =
        project.files(project.configurations.matching { it.isCanBeResolved && !(it is DeprecatableConfiguration && it.resolutionAlternatives.isNotEmpty()) })
    inputs.files(dependencies)
    doLast { println("Resolved all dependencies (${dependencies.files.size} files).") }
}
