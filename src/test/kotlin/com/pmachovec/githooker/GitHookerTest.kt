package com.pmachovec.githooker

import com.pmachovec.githooker.constants.tasks.SetGitHooksTask
import com.pmachovec.githooker.extensions.GitHookerExtension

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.powermock.modules.testng.PowerMockTestCase
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertNull
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class GitHookerTest : PowerMockTestCase() {
    private lateinit var project: Project

    @BeforeMethod
    fun setupMethod() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
    }

    @Test(description = "Plugin available expected")
    fun pluginNotNullTest() {
        assertNotNull(project.plugins.findPlugin(GitHooker.NAME))
    }

    @Test(
        dependsOnMethods = ["pluginNotNullTest"],
        description = "Configuration extension available expected"
    )
    fun extensionAvailableTest() {
        val gitHookerExtension = project.extensions.findByName(GitHookerExtension.NAME)
        assertNotNull(gitHookerExtension)
        assertTrue(gitHookerExtension is GitHookerExtension)
    }

    @Test(
        dependsOnMethods = ["extensionAvailableTest"],
        description = "Configuration extension properties not set expected"
    )
    fun extensionPropertiesNotSetTest() {
        val gitHookerExtension = project.extensions.getByName(GitHookerExtension.NAME) as GitHookerExtension
        assertNull(gitHookerExtension.hooksPath)
        assertNull(gitHookerExtension.triggerTaskName)
    }

    @Test(
        dependsOnMethods = ["pluginNotNullTest"],
        description = "Task available expected"
    )
    fun taskAvailableTest() {
        val setGitHooksTask = project.tasks.findByName(SetGitHooksTask.NAME)
        assertNotNull(setGitHooksTask)
    }

    @Test(
        dependsOnMethods = ["taskAvailableTest"],
        description = "Task properties correct expected"
    )
    fun taskPropertiesTest() {
        val setGitHooksTask = project.tasks.getByName(SetGitHooksTask.NAME)
        assertEquals(setGitHooksTask.group, SetGitHooksTask.GROUP)
        assertEquals(setGitHooksTask.description, SetGitHooksTask.DESCRIPTION)
    }
}
