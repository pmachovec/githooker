plugins {
    idea
    kotlin("jvm") version "1.3.41"
}

group = "com.pmachovec"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib"))

    implementation("org.apache.commons", "commons-exec", "1.3")

    runtime(files(sourceSets["main"].output.resourcesDir))
}

idea {
    module {
        outputDir = sourceSets["main"].java.outputDir
    }
}
