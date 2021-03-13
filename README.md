# Githooker 1.1
Gradle plugin for enforcing git hooks stored in a non-default location. Triggers Git hooks path configuration after a specified Gradle task is finished. It's recommended to specify a task that is a part of the current project build.

For example, when the path `custom/hooks` and the task `classes` is specified, then every time after the task is executed the Git hooks path configuration is checked for the current project. And if it's different from the specified path, or not set at all, the specified path is configured.

### Prerequisities
* Java 8 or higher
* Gradle 6.1 or higher <!-- 6.0 doesn't work -->
* Git 2.20.0 or higher
* Current project is Git project

### Usage
* The plugin is available in the Github Maven repository `https://raw.github.com/pmachovec/mavenrepo/master`.
  
* In Gradle settings script load the repository with the plugin. The Gradle plugin portal is also needed.
* In Gradle build script apply the plugin first and then use the `githooker` extension to configure the plugin.
  - Use the `hooksPath` property to set the path to a folder with Git hooks. The path is relative to the root of the current project.
  - Use the `triggerTaskName` property to specify a task that should set the Git configuration.
#### Kotlin DSL
* _settings.gradle.kts_:
```
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://raw.github.com/pmachovec/mavenrepo/master")
    }
}
```
* _build.gradle.kts_
```
plugins {
    id("com.pmachovec.githooker") version "1.1" 
}

githooker {
    hooksPath = "CUSTOM_PATH"
    triggerTaskName = "ANY_AVAILABLE_TASK_NAME"
}
```

#### Groovy DSL
* _settings.gradle_:
```
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url 'https://raw.github.com/pmachovec/mavenrepo/master'
        }
    }
}
```
* _build.gradle_:
```
plugins {
    id 'com.pmachovec.githooker' version '1.1'
}

githooker {
    hooksPath = 'CUSTOM_PATH'
    triggerTaskName = 'ANY_AVAILABLE_TASK_NAME'
}
```

### Release track
**1.1**
* made compatible with Java 8

<br/>

**1.0.4**
* imports in the code organized

<br/>

**1.0.3**
* user name omitted for package repository publication configuration

<br/>

**1.0.2**
* fixed version of Ultrabuilder plugin incorporated
* ready for publishing to GitHub package repository

<br/>

**1.0.1**
* source code structure enhanced
