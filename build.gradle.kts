import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.20-Beta2"
    id("fabric-loom") version "1.15-SNAPSHOT"
}

val modVersion: String by project
val mavenGroup: String by project
val archivesBaseName: String by project
val minecraftVersion: String by project
val loaderVersion: String by project
val fabricVersion: String by project

version = modVersion
group = mavenGroup

base {
    archivesName.set(archivesBaseName)
}

loom {
    accessWidenerPath.set(file("src/main/resources/miohr.accesswidener"))
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://api.modrinth.com/maven") }
    maven { url = uri("https://maven.lenni0451.net/everything") }
    maven { url = uri("https://repo.viaversion.com/") }
    maven { url = uri("https://repo.opencollab.dev/maven-snapshots/") }
    maven { url = uri("https://maven.terraformersmc.com/") }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    include("io.github.llamalad7:mixinextras-fabric:0.4.1")
    annotationProcessor("io.github.llamalad7:mixinextras-fabric:0.4.1")

    modRuntimeOnly("com.viaversion:viafabricplus:4.4.5")
    modRuntimeOnly("maven.modrinth:sodium:mc1.21.11-0.8.4-fabric")
    modRuntimeOnly("maven.modrinth:iris:1.10.5+1.21.11-fabric")
    modRuntimeOnly("maven.modrinth:sodium-extra:mc1.21.11-0.8.3+fabric")
    modRuntimeOnly("maven.modrinth:lithium:mc1.21.11-0.21.2-fabric")
    modImplementation("maven.modrinth:in-game-account-switcher:9.0.6-alpha.2+1.21.11-fabric")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.9+kotlin.2.3.10")

    val skijaVersion = "0.119.3"
    implementation("io.github.humbleui:skija-windows-x64:$skijaVersion")
    include("io.github.humbleui:skija-windows-x64:$skijaVersion")

    implementation("io.github.humbleui:skija-shared:$skijaVersion")
    include("io.github.humbleui:skija-shared:$skijaVersion")

    implementation("io.github.humbleui:types:0.2.0")
    include("io.github.humbleui:types:0.2.0")
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}" }
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/kotlin"))
        }
    }
}