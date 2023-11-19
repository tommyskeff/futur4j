import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "dev.tommyjs"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.1.0")
    implementation(project(mapOf("path" to ":futur-api")))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    withType<ShadowJar> {
        exclude("META-INF/**")
    }
}

tasks.test {
    useJUnitPlatform()
}