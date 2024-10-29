plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "de.jonihoffi.plugins"
version = "0.0.7"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.2")
    type.set("PS")
    plugins.set(listOf("org.jetbrains.plugins.yaml"))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.compilerArgs = mutableListOf("-Xlint:deprecation")
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
