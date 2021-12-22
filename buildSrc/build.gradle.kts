plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

tasks {
    compileKotlin {
        sourceCompatibility = JavaVersion.VERSION_17.majorVersion
        targetCompatibility = JavaVersion.VERSION_17.majorVersion
    }
}
