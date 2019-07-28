package com.pmachovec.githooker

import com.pmachovec.githooker.extensions.GitHookerExtension
import com.pmachovec.githooker.tasks.SetGitHooks

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitHooker: Plugin<Project> {
    companion object {
        const val NAME = "githooker"
    }

    override fun apply(project: Project) {
        val gitHookerExtension = project.extensions.create(GitHookerExtension.NAME, GitHookerExtension::class.java)
        project.tasks.create(SetGitHooks.NAME, SetGitHooks::class.java, gitHookerExtension)
    }
}
