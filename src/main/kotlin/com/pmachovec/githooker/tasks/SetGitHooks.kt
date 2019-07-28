package com.pmachovec.githooker.tasks

import com.pmachovec.githooker.constants.DefaultValues
import com.pmachovec.githooker.constants.SetGitHooksTexts
import com.pmachovec.githooker.extensions.GitHookerExtension

import java.io.ByteArrayOutputStream
import javax.inject.Inject

import org.apache.commons.exec.*
import org.gradle.api.*
import org.gradle.api.tasks.TaskAction

open class SetGitHooks @Inject constructor(gitHookerExtensionExtension: GitHookerExtension): DefaultTask() {
    private val gitHookerExtension: GitHookerExtension = gitHookerExtensionExtension
    private val gitConfigCommand = "git config core.hooksPath"
    private val exec = DefaultExecutor()

    companion object {
        const val NAME = "setGitHooks"
    }

    @TaskAction
    fun setGitHooks() {
        val gitHooksConfig = getGitHooksConfig()

        if (gitHooksConfig.isEmpty() || gitHooksConfig != gitHookerExtension.hooksPath) {
            runSetGitHooksCommand(gitHookerExtension.hooksPath?: DefaultValues.HOOKS_PATH)
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
            println(SetGitHooksTexts.PATH_NOT_SET)
        }

        return outputStream.toString().trim()
    }

    private fun runSetGitHooksCommand(hooksPath: String) {
        println(SetGitHooksTexts.SETTING_PATH)
        val cmdLine = CommandLine.parse("$gitConfigCommand $hooksPath")
        exec.execute(cmdLine)
        println(SetGitHooksTexts.PATH_SET)
    }
}
