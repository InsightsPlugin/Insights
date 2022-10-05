import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.file.Files

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version VersionConstants.shadowVersion
}

val name = "Insights"
group = "dev.frankheijden.insights"
val dependencyDir = "$group.dependencies"
version = "6.12.3-SNAPSHOT"

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.johnrengelman.shadow")

    version = rootProject.version

    val groupParts = project.name.split('-').drop(1)
    val nms = groupParts.isNotEmpty() && groupParts.first() == "NMS"
    val nmsImpl = nms && groupParts.last().startsWith("v")
    group = if (groupParts.isEmpty()) {
        rootProject.group
    } else {
        rootProject.group.toString() + groupParts.joinToString(".", ".") {
            if (it.startsWith("v")) {
                it
            } else {
                it.toLowerCase()
            }
        }
    }

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/maven-public")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://libraries.minecraft.net")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:${VersionConstants.minecraftVersion}")
        implementation("com.github.FrankHeijden:MinecraftReflection:${VersionConstants.minecraftReflectionVersion}")
        implementation("io.papermc:paperlib:${VersionConstants.paperLibVersion}")
        implementation("org.bstats:bstats-bukkit:${VersionConstants.bStatsVersion}")
        implementation("net.kyori:adventure-api:${VersionConstants.adventureVersion}")
        implementation("net.kyori:adventure-platform-bukkit:${VersionConstants.adventurePlatformVersion}")
        implementation("net.kyori:adventure-text-minimessage:${VersionConstants.adventureVersion}")
        if (!nms || nmsImpl) {
            compileOnly(project(":Insights-NMS-Core"))
        }

        testImplementation("io.papermc.paper:paper-api:${VersionConstants.minecraftVersion}")
        testImplementation("org.assertj:assertj-core:${VersionConstants.assertjVersion}")
        testImplementation("org.mockito:mockito-core:${VersionConstants.mockitoVersion}")
        testImplementation("org.junit.jupiter:junit-jupiter-api:${VersionConstants.jupiterVersion}")
        testImplementation("org.junit.jupiter:junit-jupiter-params:${VersionConstants.jupiterVersion}")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:${VersionConstants.jupiterVersion}")
    }

    tasks {
        build {
            dependsOn("shadowJar", "checkstyleMain", "checkstyleTest", "test")
        }

        compileJava {
            options.encoding = Charsets.UTF_8.name()
            options.isDeprecation = true
            sourceCompatibility = JavaVersion.VERSION_17.majorVersion
            targetCompatibility = JavaVersion.VERSION_17.majorVersion
        }

        javadoc {
            options.encoding = Charsets.UTF_8.name()
        }

        processResources {
            filteringCharset = Charsets.UTF_8.name()
        }

        test {
            useJUnitPlatform()
        }
    }

    tasks.withType<Checkstyle>().configureEach {
        configFile = file("$rootDir/config/checkstyle/checkstyle.xml")
        ignoreFailures = false
        maxErrors = 0
        maxWarnings = 0
    }

    tasks.withType<ShadowJar> {
        if (nmsImpl) {
            finalizedBy("reobfJar")
        }
        relocate("dev.frankheijden.minecraftreflection", "$dependencyDir.minecraftreflection")
        relocate("io.papermc.lib", "$dependencyDir.paperlib")
        relocate("org.bstats", "$dependencyDir.bstats")
        relocate("net.kyori.adventure", "$dependencyDir.adventure")
        relocate("net.kyori.examination", "$dependencyDir.examination")
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${VersionConstants.minecraftVersion}")
    implementation(project(":Insights-API", "shadow"))
    implementation(project(":Insights", "shadow"))
    Files
        .list(rootProject.projectDir.toPath().resolve("Insights-NMS"))
        .filter {
            !it.fileName.toString().startsWith(".")
        }
        .forEach {
            implementation(project(":Insights-NMS-${it.fileName}", "shadow"))
        }
}

tasks {
    build {
        dependsOn("shadowJar")
    }
}
