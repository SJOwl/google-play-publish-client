import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.50"
}

group = "google-play-client"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("com.google.apis:google-api-services-androidpublisher:v3-rev103-1.25.0")
    implementation(group = "com.google.api-client", name = "google-api-client-extensions", version = "1.6.0-beta")
    implementation(group = "com.google.oauth-client", name = "google-oauth-client-java6", version = "1.11.0-beta")
    implementation(group = "com.google.oauth-client", name = "google-oauth-client-jetty", version = "1.11.0-beta")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


val fatJar = task("fatJar", type = Jar::class) {
    baseName = project.name
    manifest {
        attributes["Implementation-Title"] = "Google Play Client"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "com.google.play.developerapi.samples.UploadApk"
    }
    from(configurations.runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })
    with(tasks["jar"] as CopySpec)
}

tasks {
    "build" {
        dependsOn(fatJar)
    }
}