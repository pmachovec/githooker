# Githooker 1.0.3
Gradle plugin for enforcing git hooks stored in a non-default location. Triggers Git hooks path configuration after a specified Gradle task is finished. It's recommended to specify a task that is a part of the current project build.

For example, when the path `custom/hooks` and the task `classes` is specified, then every time after the task is executed the Git hooks path configuration is checked for the current project. And if it's different from the specified path, or not set at all, the specified path is configured.

### Prerequisities
* Java 12
* Gradle 6.1.1
* Git 2.22.0
* Current project is Git project

### Usage (Kotlin DSL)
The plugin is available in the Github Maven repository `https://raw.github.com/pmachovec/mavenrepo/master`.
* In `settings.gradle.kts` load the repository with the plugin.
```
pluginManagement {
    repositories {
        ...
        maven("https://raw.github.com/pmachovec/mavenrepo/master")
        ...
    }
}
```
* In `build.gradle.kts` apply the plugin first.
```
plugins {
    ...
    id("com.pmachovec.githooker").version("1.0.3")
    ...
}
```
* Then use the `githooker` extension to configure the plugin.
    - Use the `hooksPath` property to set the path to a folder with Git hooks. The path is relative to the root of the current project.
    - Use the `triggerTaskName` property to specify a task that should set the Git configuration. 
```
githooker {
    hooksPath = "custom/hooks"
    triggerTaskName = "classes"
}
```

### Release track
**1.0.3**
* user name omitted for package repository publication configuration
* no new functionality

<br/>

**1.0.2**
* fixed version of Ultrabuilder plugin incorporated
* ready for publishing to GitHub package repository
* no new functionality

<br/>

**1.0.1**
* source code structure enhanced
* no new functionality
