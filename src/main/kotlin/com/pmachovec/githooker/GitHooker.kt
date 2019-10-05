package com.pmachovec.githooker

import com.pmachovec.githooker.actions.SetGitHooksAction
import com.pmachovec.githooker.constants.Texts
import com.pmachovec.githooker.constants.tasks.SetGitHooksTask
import com.pmachovec.githooker.extensions.GitHookerExtension

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException

class GitHooker: Plugin<Project> {
    companion object {
        const val NAME = "githooker"
    }

    override fun apply(project: Project) {
        val gitHookerExtension = project.extensions.create(GitHookerExtension.NAME, GitHookerExtension::class.java)
        val setGitHooksTask = project.task(SetGitHooksTask.NAME)
        setGitHooksTask.doLast(SetGitHooksAction(gitHookerExtension.hooksPath))
        setGitHooksTask.group = SetGitHooksTask.GROUP
        setGitHooksTask.description = SetGitHooksTask.DESCRIPTION

        if (!gitHookerExtension.triggerTaskName.isNullOrEmpty()) {
            try {
                val triggerTask = project.tasks.getByName(gitHookerExtension.triggerTaskName!!)
                setGitHooksTask.dependsOn(triggerTask)
            } catch(ute: UnknownTaskException) {
                println(Texts.TRIGGER_TASK_NOT_FOUND.format(gitHookerExtension.triggerTaskName))
            }
        }
    }
}
