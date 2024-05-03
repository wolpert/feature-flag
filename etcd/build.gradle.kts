/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.7/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    checkstyle
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    //api(libs.commons.math3)
    api(project(":ff"))

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.slf4j.api)
    implementation(libs.jetcd)

    testImplementation(libs.bundles.logback)
    testImplementation(libs.bundles.testing)
    testImplementation(libs.jetcd.test)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
