import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.zacklukem"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.6.21"
    `java-library`
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
