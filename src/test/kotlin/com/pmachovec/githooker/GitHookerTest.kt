package com.pmachovec.githooker

import com.pmachovec.githooker.extensions.GitHookerExtension
import com.pmachovec.githooker.tasks.SetGitHooks

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
class GitHookerTest {
    @Test
    @DisplayName("Apply plugin '${GitHooker.NAME}'")
    fun applyPluginTest() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
        assertNotNull(project.plugins.findPlugin(GitHooker.NAME))

        val gitHookerExtension = project.extensions.findByName(GitHookerExtension.NAME)
        assertNotNull(gitHookerExtension)
        assertTrue(gitHookerExtension is GitHookerExtension)

        gitHookerExtension as GitHookerExtension
        assertNull(gitHookerExtension.hooksPath)
        assertNull(gitHookerExtension.triggerTaskName)

        val setGitHooks = project.tasks.getByName(SetGitHooks.NAME)
        assertNotNull(setGitHooks)
    }
}
