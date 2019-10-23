import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "1.3.50"
    id("org.jlleitschuh.gradle.ktlint") version "9.0.0"
    id("com.pmachovec.ultrabuilder").version("1.0")
}

group = "com.pmachovec"
version = "1.0"

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
            try {
                // 'repoUrl' variable to be set in gradle.properties
                url = uri(rootProject.extra["repoUrl"]!!)
            } catch (upe: ExtraPropertiesExtension.UnknownPropertyException) {
                println("Repository for publishing not set")
            }
        }
    }
}

idea {
    module {
        outputDir = File("$buildDir/classes/kotlin/main")
        testOutputDir = File("$buildDir/classes/kotlin/test")
    }
}

// PROJECT CONFIGURATION
ktlint {
    disabledRules.add("import-ordering")
    verbose.set(true)
}

tasks.compileTestKotlin {
    kotlinOptions.suppressWarnings = true
}

// The 'jar' task creates a fat jar without Gradle API, project binaries are NOT skipped
tasks.jar {
    from({
        configurations.runtimeClasspath.get().filter {
            it.name.contains("commons-exec")
            /*
             * Using 'it.name.endswith("jar")' would create an enormous jar with the whole Gradle API,
             * which makes no sense for a Gradle plugin.
             */
        }.map {
            zipTree(it)
        }
    })
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_12.toString()
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
