package com.pmachovec.githooker.actions

import com.pmachovec.githooker.constants.DefaultValues
import com.pmachovec.githooker.constants.Texts
import com.pmachovec.githooker.extensions.GitHookerExtension

import java.io.ByteArrayOutputStream
import javax.inject.Inject

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.PumpStreamHandler
import org.gradle.api.Action
import org.gradle.api.Task

class SetGitHooksAction @Inject constructor() : Action<Task> {
    private val exec = DefaultExecutor()

    override fun execute(task: Task) {
        val gitHookerExtension = task.project.extensions.findByName(GitHookerExtension.NAME) as GitHookerExtension
        val hooksPath = gitHookerExtension.hooksPath

        if (hooksPath.isNullOrEmpty()) {
            println(Texts.PATH_NOT_CONFIGURED)
        } else {
            val gitHooksConfigPath = getGitHooksConfigPath()

            if (gitHooksConfigPath.isEmpty() || gitHooksConfigPath != hooksPath) {
                runSetGitHooksCommand(hooksPath)
            }
        }
    }

    private fun getGitHooksConfigPath(): String {
        val cmdLine = CommandLine.parse(DefaultValues.GIT_CONFIG_COMMAND)
        val outputStream = ByteArrayOutputStream()
        val streamHandler = PumpStreamHandler(outputStream)
        exec.streamHandler = streamHandler

        try {
            exec.execute(cmdLine)
        } catch (err: ExecuteException) {
            println(Texts.PATH_NOT_SET)
        }

        return outputStream.toString().trim()
    }

    private fun runSetGitHooksCommand(hooksPath: String) {
        println(Texts.SETTING_PATH)
        val cmdLine = CommandLine.parse("${DefaultValues.GIT_CONFIG_COMMAND} $hooksPath")
        exec.execute(cmdLine)
        println(Texts.PATH_SET)
    }
}
