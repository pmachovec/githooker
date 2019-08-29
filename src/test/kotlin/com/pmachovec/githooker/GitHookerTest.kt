package com.pmachovec.githooker

import com.pmachovec.githooker.extensions.GitHookerExtension
import com.pmachovec.githooker.tasks.SetGitHooks

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.powermock.modules.testng.PowerMockTestCase
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertNull
import org.testng.Assert.assertTrue
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class GitHookerTest: PowerMockTestCase() {
    private lateinit var project: Project

    @BeforeClass
    fun setup() {
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
    }

    @Test
    fun applyPluginNotNullTest() {
        assertNotNull(project.plugins.findPlugin(GitHooker.NAME))
    }

    @Test(dependsOnMethods = ["applyPluginNotNullTest"])
    fun applyPluginExtensionAvailableTest() {
        val gitHookerExtension = project.extensions.findByName(GitHookerExtension.NAME)
        assertNotNull(gitHookerExtension)
        assertTrue(gitHookerExtension is GitHookerExtension)
    }

    @Test(dependsOnMethods = ["applyPluginExtensionAvailableTest"])
    fun applyPluginExtensionPropertiesNotSetTest() {
        val gitHookerExtension = project.extensions.findByName(GitHookerExtension.NAME)
        gitHookerExtension as GitHookerExtension
        assertNull(gitHookerExtension.hooksPath)
        assertNull(gitHookerExtension.triggerTaskName)
    }

    @Test(dependsOnMethods = ["applyPluginNotNullTest"])
    fun applyPluginTaskAvailableTest() {
        val setGitHooks = project.tasks.getByName(SetGitHooks.NAME)
        assertNotNull(setGitHooks)
    }
}
