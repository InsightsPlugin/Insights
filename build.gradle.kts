import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.z4kn4fein.semver.toVersion
import java.nio.file.Files

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.userdev) apply false
}

val name = "Insights"
group = "dev.frankheijden.insights"
val dependencyDir = "$group.dependencies"
version = "6.19.3"

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "com.gradleup.shadow")

    version = rootProject.version

    val groupParts = project.name.split('-').drop(1)
    val nms = groupParts.isNotEmpty() && groupParts.first() == "NMS"
    val nmsImpl = nms && groupParts.last() != "Core"
    if (nmsImpl) {
        apply(plugin = "io.papermc.paperweight.userdev")
    }

    group = if (groupParts.isEmpty()) {
        rootProject.group
    } else {
        rootProject.group.toString() + groupParts.joinToString(".", ".") {
            if (it.startsWith("v")) {
                it
            } else {
                it.lowercase()
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

    val libs = rootProject.libs
    dependencies {
        compileOnly(libs.paperApi)
        implementation(libs.paperLib)
        implementation(libs.bStatsBukkit)
        implementation(libs.adventureApi)
        implementation(libs.adventureMiniMessage)
        implementation(libs.adventurePlatformBukkit)

        if (!nms || nmsImpl) {
            compileOnly(project(":Insights-NMS-Core"))
        }

        testImplementation(libs.paperApi)
        testImplementation(libs.assertj)
        testImplementation(libs.mockitoCore)
        testImplementation(libs.jupiterApi)
        testImplementation(libs.jupiterParams)
        testImplementation(libs.jupiterEngine)
    }

    tasks {
        build {
            dependsOn("shadowJar", "checkstyleMain", "checkstyleTest", "test")
        }

        compileJava {
            options.encoding = Charsets.UTF_8.name()
            options.isDeprecation = true
            sourceCompatibility = JavaVersion.VERSION_21.majorVersion
            targetCompatibility = JavaVersion.VERSION_21.majorVersion
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
        relocate("dev.frankheijden.minecraftreflection", "$dependencyDir.minecraftreflection")
        relocate("io.papermc.lib", "$dependencyDir.paperlib")
        relocate("org.bstats", "$dependencyDir.bstats")
        relocate("net.kyori.adventure", "$dependencyDir.adventure")
        relocate("net.kyori.examination", "$dependencyDir.examination")
        relocate("net.kyori.option", "$dependencyDir.option")
        if (nmsImpl) {
            relocate(project.group.toString().replaceAfterLast('.', "impl"), project.group.toString())
        }
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":Insights-API", "shadow"))
    implementation(project(":Insights", "shadow"))
    Files
        .list(rootProject.projectDir.toPath().resolve("Insights-NMS"))
        .filter { !it.fileName.toString().startsWith(".") }
        .forEach {
            val configuration = if (it.fileName.toString() == "Core") "shadow" else "reobf"
            implementation(project(":Insights-NMS-${it.fileName}", configuration))
        }
}

tasks {
    clean {
        dependsOn("cleanJars")
    }

    build {
        dependsOn("shadowJar")
        finalizedBy("copyJars")
    }
}

tasks.register("cleanJars") {
    delete(file("jars"))
}

tasks.register<Copy>("copyJars") {
    from(tasks.findByPath("shadowJar")!!) {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    into(file("jars"))
    rename("(.+)Parent(.+)-all(.+)", "$1$2$3")
}

buildscript {
    dependencies {
        classpath("io.github.z4kn4fein:semver:2.0.0")
    }
}

val artifactFile: File = tasks.shadowJar.get().archiveFile.get().asFile
val artifact: PublishArtifact = artifacts.add("archives", artifactFile) {
    type = "jar"
    name = "Insights"
    group = rootProject.group
    version = rootProject.version
    classifier = ""
    builtBy("shadowJar")
}

fun Project.isRelease(): Boolean {
    return version.toString().toVersion().preRelease == null
}

task("printIsRelease") {
    doLast {
        println(isRelease())
    }
}

task("printVersion") {
    doLast {
        println(version.toString())
    }
}

publishing {
    repositories {
        maven {
            name = "fvdh"
            url = if (version.toString().toVersion().preRelease == null) {
                uri("https://repo.fvdh.dev/releases")
            } else {
                uri("https://repo.fvdh.dev/snapshots")
            }

            credentials {
                username = System.getenv("FVDH_USERNAME")
                password = System.getenv("FVDH_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("Insights") {
            artifact(artifact)
            artifactId = "Insights"
        }
    }
}
