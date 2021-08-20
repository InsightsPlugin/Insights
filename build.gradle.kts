import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.papermc.paperweight.userdev") version "1.1.11"
}

val name = "Insights"
group = "dev.frankheijden.insights"
val dependencyDir = "${group}.dependencies"
version = "6.7.0-SNAPSHOT"

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
        paperDevBundle("1.17.1-R0.1-SNAPSHOT")
        implementation("com.github.FrankHeijden:MinecraftReflection:123e2f546c")
        implementation("io.papermc:paperlib:1.0.6")
        implementation("org.bstats:bstats-bukkit:1.8")
        implementation("net.kyori:adventure-api:4.8.1")
        implementation("net.kyori:adventure-platform-bukkit:4.0.0-SNAPSHOT")
    }

    tasks {
        build {
            dependsOn("checkstyleMain", "checkstyleTest", "test")
        }

        compileJava {
            options.encoding = Charsets.UTF_8.name()
            options.release.set(16)
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
        configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
        ignoreFailures = false
        maxErrors = 0
        maxWarnings = 0
    }

    tasks.withType<ShadowJar> {
        relocate("dev.frankheijden.minecraftreflection", dependencyDir + ".minecraftreflection")
        relocate("io.papermc.lib", dependencyDir + ".paperlib")
        relocate("org.bstats", dependencyDir + ".bstats")
        relocate("net.kyori.adventure", dependencyDir + ".adventure")
        relocate("net.kyori.examination", dependencyDir + ".examination")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    paperDevBundle("1.17.1-R0.1-SNAPSHOT")
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

fun outputTasks(): List<Task?> {
    return listOf("reobfJar").map { tasks.findByPath(it) }
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
