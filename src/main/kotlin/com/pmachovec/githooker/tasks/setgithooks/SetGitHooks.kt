package com.pmachovec.githooker.tasks.setgithooks

import com.pmachovec.githooker.defaultvalues.DefaultValues

import java.io.ByteArrayOutputStream

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.PumpStreamHandler
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

class SetGitHooks(hooksPath: String, triggerTaskName: String) : DefaultTask() {
    private var hooksPath = DefaultValues.HOOKS_PATH
    private val gitConfigCommand = "git config core.hooksPath"
    private val exec = DefaultExecutor()

    companion object {
        const val NAME_FOR_PLUGIN = "setGitHooks"
    }

    init {
        if (hooksPath.isNotEmpty()) {
            this.hooksPath = hooksPath
        }

        val triggerTask: Task = if (triggerTaskName.isNotEmpty()) {
            project.tasks.getByName(triggerTaskName)
        } else {
            project.tasks.getByName(DefaultValues.TRIGGER_TASK_NAME)
        }

        if (triggerTask != null) {
            triggerTask.dependsOn(this)
        } else {
            println(String.format(SetGitHooksOutputs.TASK_NOT_FOUND, triggerTaskName))
        }
    }

    @TaskAction
    fun setGitHooks() {
        val gitHooksConfig = getGitHooksConfig()

        if (hooksPath != gitHooksConfig) {
            println(SetGitHooksOutputs.SETTING_PATH)
            val cmdLine = CommandLine.parse("$gitConfigCommand $hooksPath")
            exec.execute(cmdLine)
            println(SetGitHooksOutputs.PATH_SET)
        }
    }

    private fun getGitHooksConfig(): String {
        val cmdLine = CommandLine.parse(gitConfigCommand)
        val outputStream = ByteArrayOutputStream()
        val streamHandler = PumpStreamHandler(outputStream)
        exec.streamHandler = streamHandler

        try {
            exec.execute(cmdLine)
        } catch (err: ExecuteException) {
            println(SetGitHooksOutputs.PATH_NOT_SET)
        }

        return outputStream.toString().trim()
    }
}
