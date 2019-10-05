package com.pmachovec.githooker

import com.pmachovec.githooker.constants.tasks.SetGitHooksTask
import com.pmachovec.githooker.extensions.GitHookerExtension

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.powermock.modules.testng.PowerMockTestCase
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertNull
import org.testng.Assert.assertTrue

class GitHookerProjectStructureTest: PowerMockTestCase() {
    private lateinit var project: Project

    @BeforeMethod
    fun setupMethod() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
    }

    @Test(description = "Plugin available expected")
    fun applyPluginNotNullTest() {
        assertNotNull(project.plugins.findPlugin(GitHooker.NAME))
    }

    @Test(dependsOnMethods = ["applyPluginNotNullTest"],
          description = "Configuration extension available expected")
    fun applyExtensionAvailableTest() {
        val gitHookerExtension = project.extensions.findByName(GitHookerExtension.NAME)
        assertNotNull(gitHookerExtension)
        assertTrue(gitHookerExtension is GitHookerExtension)
    }

    @Test(dependsOnMethods = ["applyExtensionAvailableTest"],
          description = "Configuration extension properties not set expected")
    fun applyExtensionPropertiesNotSetTest() {
        val gitHookerExtension = project.extensions.findByName(GitHookerExtension.NAME)
        gitHookerExtension as GitHookerExtension
        assertNull(gitHookerExtension.hooksPath)
        assertNull(gitHookerExtension.triggerTaskName)
    }

    @Test(dependsOnMethods = ["applyPluginNotNullTest"],
          description = "Task available expected")
    fun applyTaskAvailableTest() {
        val setGitHooksTask = project.tasks.getByName(SetGitHooksTask.NAME)
        assertNotNull(setGitHooksTask)
    }

    @Test(dependsOnMethods = ["applyTaskAvailableTest"],
          description = "Task properties correct expected")
    fun applyTaskPropertiesTest() {
        val setGitHooksTask = project.tasks.getByName(SetGitHooksTask.NAME)
        assertEquals(setGitHooksTask.group, SetGitHooksTask.GROUP)
        assertEquals(setGitHooksTask.description, SetGitHooksTask.DESCRIPTION)
    }
}
