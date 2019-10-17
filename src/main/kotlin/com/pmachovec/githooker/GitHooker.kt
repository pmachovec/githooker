package com.pmachovec.githooker

import com.pmachovec.githooker.actions.SetGitHooksAction
import com.pmachovec.githooker.constants.tasks.SetGitHooksTask
import com.pmachovec.githooker.extensions.GitHookerExtension
import com.pmachovec.githooker.listeners.TriggerTaskListener

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitHooker : Plugin<Project> {
    companion object {
        const val NAME = "githooker"
    }

    override fun apply(project: Project) {
        project.gradle.taskGraph.addTaskExecutionListener(TriggerTaskListener())
        project.extensions.create(GitHookerExtension.NAME, GitHookerExtension::class.java)
        val setGitHooksTask = project.task(SetGitHooksTask.NAME)
        setGitHooksTask.doLast(SetGitHooksAction())
        setGitHooksTask.group = SetGitHooksTask.GROUP
        setGitHooksTask.description = SetGitHooksTask.DESCRIPTION
    }
}
