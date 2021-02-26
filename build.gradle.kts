import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm").version("1.4.30")
    id("org.jlleitschuh.gradle.ktlint").version("9.0.0")
}

group = "com.pmachovec"
version = "1.0.4"

// REPOSITORIES AND DEPENDENCIES
repositories {
    mavenCentral()
}

dependencies {
    // implementation(gradleApi()) // Added automatically by the java-gradle-plugin
    implementation(kotlin("stdlib"))
    implementation("org.apache.commons", "commons-exec", "1.3")

    testImplementation("org.powermock", "powermock-api-mockito2", "2.0.2")
    testImplementation("org.powermock", "powermock-module-testng", "2.0.2")
    testImplementation("org.testng", "testng", "7.0.0")

    runtimeOnly(files(sourceSets["main"].output.resourcesDir))
}

// PUBLICATION TO MAVEN REPOSITORY
gradlePlugin {
    plugins {
        create("gitHookerPublication") { // Can be anything
            id = "com.pmachovec.githooker"
            implementationClass = "com.pmachovec.githooker.GitHooker"
        }
    }
}

publishing {
    repositories {
        maven {
            project.properties["repoUrl"]?.let { url = uri(it) }

            credentials {
                username = "." // Doesn't have to be a real user name
                project.properties["token"]?.let { password = it.toString() }
            }
        }
    }
}

// PROJECT CONFIGURATION
idea {
    module {
        outputDir = File("$buildDir/classes/kotlin/main")
        testOutputDir = File("$buildDir/classes/kotlin/test")
    }
}

ktlint {
    verbose.set(true)
}

tasks.compileJava {
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

tasks.compileTestKotlin {
    kotlinOptions.suppressWarnings = true
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
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

val tasksToDisable = tasks.withType<PublishToMavenRepository>()

tasksToDisable.forEach {
    it.enabled = false
}
