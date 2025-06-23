/*
 * MIT License
 *
 * Copyright (c) 2025 Michael Hill
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import org.gradle.internal.deprecation.DeprecatableConfiguration
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    kotlin("jvm") version "2.2.0"
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
