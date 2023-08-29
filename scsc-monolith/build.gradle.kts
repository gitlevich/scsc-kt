plugins {
    val kotlinGradlePluginVersion = "1.9.0"
    kotlin("jvm") version kotlinGradlePluginVersion
    kotlin("plugin.noarg") version kotlinGradlePluginVersion
    kotlin("plugin.allopen") version kotlinGradlePluginVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinGradlePluginVersion
}

noArg {
    invokeInitializers = true
}
allOpen {
    annotations(
        "jakarta.persistence.Entity",
        "jakarta.persistence.Embeddable",
        "org.axonframework.modelling.command.AggregateRoot"
    )
}
noArg {
    annotations(
        "jakarta.persistence.Entity",
        "jakarta.persistence.Embeddable"
    )
}

group = "io.axoniq.demo.scsc"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

val axonVersion = "4.7.4"
val inspectorAxonVersion = "0.1.5"
val jacksonVersion = "2.15.1"
val kotlinVersion = "1.9.0"
val mockkVersion = "1.13.5"
val assertjVersion = "3.24.2"
val dotEnvVersion = "6.4.1"

dependencies {
    // Dependency Management
    implementation(platform("org.axonframework:axon-bom:$axonVersion"))
    implementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))

    // Axon
    implementation("org.axonframework:axon-configuration")
    implementation("org.axonframework:axon-server-connector")
    implementation("io.axoniq.inspector:inspector-axon:$inspectorAxonVersion")

    implementation("org.axonframework.extensions.kotlin:axon-kotlin:4.8.0")

    // Jackson serialization
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JPA
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.hibernate:hibernate-core:6.2.2.Final")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin:1.9.0")

    // Microstream
    implementation("one.microstream:microstream-storage-embedded:08.00.00-MS-GA")

    // mail
    implementation("org.eclipse.angus:angus-mail:2.0.1")

    // logging
    implementation("ch.qos.logback:logback-classic:1.4.7")

    implementation("io.netty:netty-resolver-dns-native-macos:4.1.96.Final")
    implementation("com.typesafe:config:1.4.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ArchUnit
    implementation("com.tngtech.archunit:archunit-junit5:1.0.1")
    implementation("org.junit.jupiter:junit-jupiter:5.9.3")
    implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.axonframework.extensions.kotlin:axon-kotlin:4.8.0")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("org.axonframework:axon-test:$axonVersion")
    testImplementation("com.h2database:h2:2.2.220")
}
