import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    val kotlinGradlePluginVersion = "1.9.10"
    val shadowPluginVersion = "7.0.0"
    kotlin("jvm") version kotlinGradlePluginVersion
    kotlin("plugin.noarg") version kotlinGradlePluginVersion
    kotlin("plugin.allopen") version kotlinGradlePluginVersion
    id("org.jetbrains.kotlin.plugin.jpa") version kotlinGradlePluginVersion
    id("com.github.johnrengelman.shadow") version shadowPluginVersion
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

val kotlinVersion = "1.9.10"
val kotlinCoroutinesVersion = "1.7.3"
val axonVersion = "4.7.4"
val kotlinAxonExtVersion = "4.8.0"
val inspectorAxonVersion = "0.1.9"
val typesafeConfigVersion = "1.4.2"
val jacksonVersion = "2.15.1"
val jacksonKotlinModuleVersion = "2.15.2"

val jakartaPersistenceVersion = "3.1.0"
val hibernateVersion = "6.2.2.Final"
val postgresVersion = "42.6.0"

val logbackVersion = "1.4.7"
val microstreamVersion = "08.00.00-MS-GA"

val angusMailVersion = "2.0.1"
val mockkVersion = "1.13.5"
val assertjVersion = "3.24.2"
val dotEnvVersion = "6.4.1"
val archunitVersion = "1.0.1"
val jupiterVersion = "5.9.3"

val h2Version = "2.2.220"
dependencies {
    // Dependency Management
    implementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))
    implementation(platform("org.axonframework:axon-bom:$axonVersion"))

    // Axon
    implementation("org.axonframework:axon-configuration")
    implementation("org.axonframework:axon-server-connector")

    // Axon Extras
    implementation("io.axoniq.inspector:inspector-axon:$inspectorAxonVersion")
    implementation("org.axonframework.extensions.kotlin:axon-kotlin:$kotlinAxonExtVersion")

    // Jackson serialization
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JPA
    implementation("jakarta.persistence:jakarta.persistence-api:$jakartaPersistenceVersion")
    implementation("org.hibernate:hibernate-core:$hibernateVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.jetbrains.kotlin.plugin.jpa:org.jetbrains.kotlin.plugin.jpa.gradle.plugin:$kotlinVersion")

    // Microstream
    implementation("one.microstream:microstream-storage-embedded:$microstreamVersion")

    // mail
    implementation("org.eclipse.angus:angus-mail:$angusMailVersion")

    // logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("io.netty:netty-resolver-dns-native-macos:4.1.96.Final")
    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonKotlinModuleVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")

    // ArchUnit
    implementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")
    implementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
    implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("org.axonframework:axon-test:$axonVersion")
    testImplementation("com.h2database:h2:$h2Version")

    tasks.named("jar") {
        enabled = false
    }

    tasks {

        val scsc by registering(ShadowJar::class) {
            archiveBaseName.set("app")
            configurations = listOf(project.configurations.getByName("runtimeClasspath"))
            from(sourceSets.main.get().output)
            manifest {
                attributes("Main-Class" to "demo.scsc.SCSCAppKt")
            }
        }

        val inventory by registering(ShadowJar::class) {
            archiveBaseName.set("inventory")
            configurations = listOf(project.configurations.getByName("runtimeClasspath"))
            from(sourceSets.main.get().output)
            manifest {
                attributes("Main-Class" to "demo.thirdparty.inventory.InventorySystemKt")
            }
        }

        val warehouse by registering(ShadowJar::class) {
            archiveBaseName.set("warehouse")
            configurations = listOf(project.configurations.getByName("runtimeClasspath"))
            from(sourceSets.main.get().output)
            manifest {
                attributes("Main-Class" to "demo.thirdparty.warehouse.WarehouseSystemKt")
            }
        }

        named("assemble") {
            dependsOn(scsc)
        }
    }
}
