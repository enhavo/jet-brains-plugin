plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "de.jonihoffi.plugins"
version = "0.0.1"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.1.1")
    type.set("PS") // IC = IntelliJ IDEA Community Edition, IU = IntelliJ IDEA Ultimate Edition
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

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("241.*")
    }
}
