import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL


group = "com.zacklukem"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("org.jetbrains.dokka") version "1.8.20"
    `java-library`
    `maven-publish`
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>() {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            // used as project name in the header
            moduleName.set("MCDSL")

            // contains descriptions for the module and the packages
            includes.from("Module.md")

            // adds source links that lead to this repository, allowing readers
            // to easily find source code for inspected declarations
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(
                    URL(
                        "https://github.com/zacklukem/mcdsl/tree/main/" +
                                "src/main/kotlin"
                    )
                )
                remoteLineSuffix.set("#L")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "mcdsl"

            from(components["java"])
        }
    }
}
