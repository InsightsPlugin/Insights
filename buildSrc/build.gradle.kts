plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

tasks {
    compileJava {
        sourceCompatibility = JavaVersion.VERSION_17.majorVersion
        targetCompatibility = JavaVersion.VERSION_17.majorVersion
    }

    compileKotlin {
        sourceCompatibility = JavaVersion.VERSION_17.majorVersion
        targetCompatibility = JavaVersion.VERSION_17.majorVersion
    }
}
