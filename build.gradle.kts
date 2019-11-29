buildscript {
    repositories {
        maven {
            setUrl("https://dl.bintray.com/jetbrains/intellij-plugin-service")
        }
    }
}

repositories {
    jcenter()
}

plugins {
    base
    `kotlin-dsl`
    id("org.jetbrains.intellij") version "0.4.14"
}

group = "com.mikejhill"
version = "1.0.0"

intellij {
    version = "2019.3"
    type = "IU"
}

tasks.withType<Wrapper> {
    distributionType = Wrapper.DistributionType.ALL
}
