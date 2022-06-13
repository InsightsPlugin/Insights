import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("net.minecrell.plugin-yml.bukkit") version VersionConstants.pluginYmlVersion
}

group = rootProject.group
val dependencyDir = "$group.dependencies"
version = rootProject.version

repositories {
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly("com.mojang:brigadier:${VersionConstants.brigadierVersion}")
    compileOnly("me.clip:placeholderapi:${VersionConstants.placeholderapiVersion}")
    implementation("me.lucko:commodore:${VersionConstants.commodoreVersion}")
    implementation("cloud.commandframework:cloud-paper:${VersionConstants.cloudVersion}")
    implementation("cloud.commandframework:cloud-annotations:${VersionConstants.cloudVersion}")
    implementation(project(":Insights-API"))
}

base {
    archivesName.set("Insights")
}

tasks.withType<ShadowJar> {
    exclude("com/mojang/**")
    relocate("cloud.commandframework", "$dependencyDir.cloud")
    relocate("io.leangen.geantyref", "$dependencyDir.typetoken")
    relocate("me.lucko.commodore", "$dependencyDir.commodore")
}

bukkit {
    main = "dev.frankheijden.insights.Insights"
    description = "Insights about your server and regional block limits"
    apiVersion = "1.19"
    website = "https://github.com/InsightsPlugin/Insights"
    softDepend = listOf("PlaceholderAPI")
    authors = listOf("FrankHeijden")
    permissions {
        register("insights.info") {
            description = "Allows you to see information about insights"
            default = BukkitPluginDescription.Permission.Default.TRUE
        }
    }
}
