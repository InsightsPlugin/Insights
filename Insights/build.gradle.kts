import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("net.minecrell.plugin-yml.bukkit") version "0.4.0"
}

group = rootProject.group
val dependencyDir = "${group}.dependencies"
version = rootProject.version

repositories {
    maven("https://repo.incendo.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("com.mojang:brigadier:1.0.17")
    compileOnly("me.clip:placeholderapi:2.10.9")
    implementation("me.lucko:commodore:1.10")
    implementation("cloud.commandframework:cloud-paper:1.5.0-SNAPSHOT")
    implementation("cloud.commandframework:cloud-annotations:1.5.0-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:1.8")
    implementation(project(":Insights-API"))
}

base {
    archivesName.set("Insights")
}

tasks.withType<ShadowJar> {
    exclude("com/mojang/**")
    relocate("org.bstats.bukkit", "${dependencyDir}.bstats")
    relocate("cloud.commandframework", "${dependencyDir}.cloud")
    relocate("io.leangen.geantyref", "${dependencyDir}.typetoken")
    relocate("me.lucko.commodore", "${dependencyDir}.commodore")
}

bukkit {
    main = "dev.frankheijden.insights.Insights"
    description = "Insights about your server and regional block limits"
    apiVersion = "1.17"
    website = "https://github.com/InsightsPlugin/Insights"
    softDepend = listOf("PlaceholderAPI")
    authors = listOf("FrankHeijden")
}
