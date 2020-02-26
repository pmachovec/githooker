package com.pmachovec.githooker.listeners

import com.pmachovec.githooker.constants.tasks.SetGitHooksTask
import com.pmachovec.githooker.extensions.GitHookerExtension
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState

class TriggerTaskListener : TaskExecutionListener {
    override fun afterExecute(task: Task, state: TaskState) {
        val gitHookerExtension: GitHookerExtension = task.project.extensions.getByName(GitHookerExtension.NAME) as GitHookerExtension

        if (gitHookerExtension.triggerTaskName.equals(task.name)) {
            val setGitHooksTask = task.project.tasks.getByName(SetGitHooksTask.NAME)
            val setGitHooksAction = setGitHooksTask.actions[0]
            setGitHooksAction.execute(setGitHooksTask)
        }
    }

    override fun beforeExecute(task: Task) {}
}
