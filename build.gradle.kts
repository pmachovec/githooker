plugins {
    idea
    kotlin("jvm") version "1.3.41"
}

group = "com.pmachovec"
version = "0.1"

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk12"))

    runtime(files(sourceSets["main"].output.resourcesDir))
}

idea {
    module {
        outputDir = sourceSets["main"].java.outputDir
    }
}
