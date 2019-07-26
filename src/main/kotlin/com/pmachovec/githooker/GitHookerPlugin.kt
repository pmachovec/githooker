package com.pmachovec.githooker

import com.pmachovec.githooker.extensions.GitHooker
import com.pmachovec.githooker.tasks.setgithooks.SetGitHooks

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitHookerPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        val gitHookerExtension = project.extensions.create(GitHooker.NAME_FOR_PLUGIN, GitHooker::class.java)
        val hooksPath = gitHookerExtension.hooksPath
        val triggerTaskName = gitHookerExtension.triggerTaskName
        project.tasks.create(SetGitHooks.NAME_FOR_PLUGIN, SetGitHooks::class.java, hooksPath, triggerTaskName)
    }
}
