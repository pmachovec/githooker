import org.gradle.api.tasks.testing.logging.TestLogEvent

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

    testImplementation("org.powermock", "powermock-api-mockito2", "2.0.2")
    testImplementation("org.powermock", "powermock-module-testng", "2.0.2")
    testImplementation("org.testng", "testng", "7.0.0")

    runtime(files(sourceSets["main"].output.resourcesDir))
}

idea {
    module {
        outputDir = File("$buildDir/classes/kotlin/main")
        testOutputDir = File("$buildDir/classes/kotlin/test")
    }
}

tasks.compileTestKotlin {
    kotlinOptions.suppressWarnings = true
}

tasks.withType<Test> {
    testLogging {
        displayGranularity = 4 // Prints only method names with package path
        events(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
    }

    useTestNG {
        suites("src/test/resources/testng.xml")
        useDefaultListeners = true // Generates TestNG reports instead of Gradle reports
    }
}
