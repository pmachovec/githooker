package com.pmachovec.githooker.tasks

import com.pmachovec.githooker.constants.DefaultValues
import com.pmachovec.githooker.constants.SetGitHooksTexts
import com.pmachovec.githooker.extensions.GitHookerExtension

import java.io.ByteArrayOutputStream
import javax.inject.Inject

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.PumpStreamHandler
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class SetGitHooks @Inject constructor(gitHookerExtensionExtension: GitHookerExtension): DefaultTask() {
    private val gitHookerExtension: GitHookerExtension = gitHookerExtensionExtension
    private val exec = DefaultExecutor()

    companion object {
        const val NAME = "setGitHooks"
    }

    @TaskAction
    fun setGitHooks() {
        val gitHooksConfigPath = getGitHooksConfigPath()

        if (gitHooksConfigPath.isEmpty() || gitHooksConfigPath != gitHookerExtension.hooksPath) {
            runSetGitHooksCommand(gitHookerExtension.hooksPath?: DefaultValues.HOOKS_PATH)
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
            println(SetGitHooksTexts.PATH_NOT_SET)
        }

        return outputStream.toString().trim()
    }

    private fun runSetGitHooksCommand(hooksPath: String) {
        println(SetGitHooksTexts.SETTING_PATH)
        val cmdLine = CommandLine.parse("${DefaultValues.GIT_CONFIG_COMMAND} $hooksPath")
        exec.execute(cmdLine)
        println(SetGitHooksTexts.PATH_SET)
    }
}
