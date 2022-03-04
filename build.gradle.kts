import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version VersionConstants.shadowVersion
    id("io.papermc.paperweight.userdev") version VersionConstants.userdevVersion
}

val name = "Insights"
group = "dev.frankheijden.insights"
val dependencyDir = "$group.dependencies"
version = "6.10.1-SNAPSHOT"

subprojects {
    apply(plugin = "java")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "io.papermc.paperweight.userdev")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/maven-public")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://libraries.minecraft.net")
    }

    dependencies {
        paperDevBundle(VersionConstants.minecraftVersion)
        implementation("com.github.FrankHeijden:MinecraftReflection:${VersionConstants.minecraftReflectionVersion}")
        implementation("io.papermc:paperlib:${VersionConstants.paperLibVersion}")
        implementation("org.bstats:bstats-bukkit:${VersionConstants.bStatsVersion}")
        implementation("net.kyori:adventure-api:${VersionConstants.adventureVersion}")
        implementation("net.kyori:adventure-platform-bukkit:${VersionConstants.adventurePlatformVersion}")
        implementation("net.kyori:adventure-text-minimessage:${VersionConstants.adventureVersion}")

        testImplementation("org.assertj:assertj-core:${VersionConstants.assertjVersion}")
        testImplementation("org.mockito:mockito-core:${VersionConstants.mockitoVersion}")
        testImplementation("org.junit.jupiter:junit-jupiter-api:${VersionConstants.jupiterVersion}")
        testImplementation("org.junit.jupiter:junit-jupiter-params:${VersionConstants.jupiterVersion}")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:${VersionConstants.jupiterVersion}")
    }

    tasks {
        build {
            dependsOn("checkstyleMain", "checkstyleTest", "test")
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
        relocate("dev.frankheijden.minecraftreflection", "$dependencyDir.minecraftreflection")
        relocate("io.papermc.lib", "$dependencyDir.paperlib")
        relocate("org.bstats", "$dependencyDir.bstats")
        relocate("net.kyori.adventure", "$dependencyDir.adventure")
        relocate("net.kyori.examination", "$dependencyDir.examination")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    paperDevBundle(VersionConstants.minecraftVersion)
    implementation(project(":Insights-API", "shadow"))
    implementation(project(":Insights", "shadow"))
}

tasks {
    clean {
        dependsOn("cleanJars")
    }

    build {
        dependsOn(reobfJar, "copyJars")
    }
}

tasks.register("cleanJars") {
    delete(file("jars"))
}

tasks.register<Copy>("copyJars") {
    from(tasks.findByPath("reobfJar"), {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    })
    into(file("jars"))
    rename("(.+)Parent(.+)", "$1$2")
}

val artifactFile = tasks.findByPath("reobfJar")!!.outputs.files.singleFile
val artifact = artifacts.add("archives", artifactFile) {
    type = "jar"
    name = name.replace("Parent", "")
    group = rootProject.group
    version = rootProject.version
    builtBy("reobfJar")
}

publishing {
    repositories {
        maven {
            name = "fvdh"
            url = if (version.toString().endsWith("-SNAPSHOT")) {
                uri("https://repo.fvdh.dev/snapshots")
            } else {
                uri("https://repo.fvdh.dev/releases")
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
