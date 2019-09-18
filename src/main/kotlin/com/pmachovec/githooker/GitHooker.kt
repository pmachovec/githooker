package com.pmachovec.githooker

import com.pmachovec.githooker.actions.SetGitHooksAction
import com.pmachovec.githooker.extensions.GitHookerExtension
import com.pmachovec.githooker.constants.tasks.SetGitHooksTask

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitHooker: Plugin<Project> {
    companion object {
        const val NAME = "githooker"
    }

    override fun apply(project: Project) {
        val gitHookerExtension = project.extensions.create(GitHookerExtension.NAME, GitHookerExtension::class.java)
        val objectActionTask = project.task(SetGitHooksTask.NAME)
        objectActionTask.doLast(SetGitHooksAction(gitHookerExtension))
        objectActionTask.group = SetGitHooksTask.GROUP
        objectActionTask.description = SetGitHooksTask.DESCRIPTION
    }
}
