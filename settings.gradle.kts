import java.nio.file.Files

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "Insights"
include("Insights-API")
include("Insights-Core")
Files.list(rootProject.projectDir.toPath().resolve("Insights-NMS"))
    .filter { !it.fileName.toString().startsWith(".") }
    .forEach {
        val name = "Insights-NMS-${it.fileName}"
        include(name)
        project(":$name").apply {
            projectDir = it.toFile()
        }
    }
