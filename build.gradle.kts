plugins {
    id("org.jetbrains.intellij") version "1.16.1"
    kotlin("jvm")
}

group = "de.jonihoffi"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

intellij {
    version.set("2024.1.1")
    type.set("PS") // IC = IntelliJ IDEA Community Edition, IU = IntelliJ IDEA Ultimate Edition
    plugins.set(listOf("org.jetbrains.plugins.yaml"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("241.*")
    }
}
