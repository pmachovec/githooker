plugins {
    idea
    id("com.adarshr.test-logger") version "1.7.0"
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
    testImplementation("org.junit.jupiter", "junit-jupiter-api", "5.5.1")
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", "5.5.1")
    testImplementation("org.junit.platform", "junit-platform-suite-api", "1.5.1")
    testImplementation("org.junit.platform", "junit-platform-runner", "1.5.1")
}

idea {
    module {
        outputDir = File("build/classes/kotlin/main")
        testOutputDir = File("build/classes/kotlin/test")
    }
}