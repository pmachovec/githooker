package com.pmachovec.githooker.tasks

import com.pmachovec.githooker.GitHooker
import com.pmachovec.githooker.constants.DefaultValues
import com.pmachovec.githooker.extensions.GitHookerExtension

import java.io.ByteArrayOutputStream

import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.ExecuteException
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.exec.PumpStreamHandler
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito.doThrow
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.whenNew
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.testng.PowerMockTestCase
import org.testng.annotations.AfterMethod
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotNull

@PrepareForTest(CommandLine::class,
                SetGitHooks::class) // A class, in which mocked constructors are called, must be prepared for test.
class SetGitHooksTest: PowerMockTestCase() {
    private lateinit var byteArrayOutputStreamMock: ByteArrayOutputStream
    private lateinit var cmdLineInitialMock: CommandLine
    private lateinit var cmdLineDefaultExecutedMock: CommandLine
    private lateinit var cmdLineConfigExecutedMock: CommandLine
    private lateinit var defaultExecutorMock: DefaultExecutor
    private lateinit var pumpStreamHandlerMock: PumpStreamHandler
    private lateinit var project: Project
    private lateinit var setGitHooks: Task
    private var executeExceptionMessageMock = "executeExceptionMessageMock"
    private var gitConfigPathMock = "gitConfigPathMock"
    private var pluginConfigPathMock = "pluginConfigPathMock"

    @BeforeClass
    fun setupClass() {
        byteArrayOutputStreamMock = mock(ByteArrayOutputStream::class.java)
        cmdLineInitialMock = mock(CommandLine::class.java)
        cmdLineDefaultExecutedMock = mock(CommandLine::class.java)
        cmdLineConfigExecutedMock = mock(CommandLine::class.java)
        defaultExecutorMock = mock(DefaultExecutor::class.java)
        pumpStreamHandlerMock = mock(PumpStreamHandler::class.java)
    }

    @BeforeMethod
    fun setupMethod() {
        /*
         * Static and behavior mocking doesn't work in @BeforeClass methods. Everything but non-static mocks initialization
         * must be done for each test separately.
         */
        mockStatic(CommandLine::class.java)
        `when`(CommandLine.parse(anyString())).thenReturn(cmdLineInitialMock)
        `when`(CommandLine.parse("${DefaultValues.GIT_CONFIG_COMMAND} ${DefaultValues.HOOKS_PATH}")).thenReturn(cmdLineDefaultExecutedMock)
        `when`(CommandLine.parse("${DefaultValues.GIT_CONFIG_COMMAND} $pluginConfigPathMock")).thenReturn(cmdLineConfigExecutedMock)

        whenNew(ByteArrayOutputStream::class.java).withAnyArguments().thenReturn(byteArrayOutputStreamMock)
        whenNew(DefaultExecutor::class.java).withNoArguments().thenReturn(defaultExecutorMock)
        whenNew(PumpStreamHandler::class.java).withArguments(byteArrayOutputStreamMock).thenReturn(pumpStreamHandlerMock)

        // Plugin must be applied only after static and constructor mocks are created, otherwise, original classes would be used
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
        setGitHooks = project.tasks.getByName(SetGitHooks.NAME)
    }

    @AfterMethod
    fun teardownMethod() {
        Mockito.reset(defaultExecutorMock)
    }

    @Test
    fun setGitHooksActionAvailableTest() {
        val setGitHooksActions = setGitHooks.actions;
        assertNotNull(setGitHooksActions)
        assertEquals(setGitHooksActions.size, 1);
    }

    @Test(dependsOnMethods = ["setGitHooksActionAvailableTest"],
          description = "Git hooks path not configured, plugin hooks path not configured by the extension, plugin default path expected")
    fun setGitHooksNothingConfiguredTest() {
        doThrow(ExecuteException(executeExceptionMessageMock, 1)).`when`(defaultExecutorMock).execute(cmdLineInitialMock)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn("")
        val setGitHooksActions = setGitHooks.actions;
        val setGitHooksAction = setGitHooksActions[0];
        setGitHooksAction.execute(setGitHooks);

        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineDefaultExecutedMock);
    }

    @Test(dependsOnMethods = ["setGitHooksActionAvailableTest"],
          description = "Git hooks path configured, plugin hooks path not configured by the extension, plugin default path expected")
    fun setGitHooksGitConfiguredTest() {
        /*
         * When the plugin is applied and no path is configured, it uses the plugin-default path,
         * even when there is a different path configured for Git itself.
         */
        `when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn(gitConfigPathMock)
        val setGitHooksActions = setGitHooks.actions;
        val setGitHooksAction = setGitHooksActions[0];
        setGitHooksAction.execute(setGitHooks);

        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineDefaultExecutedMock);
    }

    @Test(dependsOnMethods = ["setGitHooksActionAvailableTest"],
          description = "Git hooks path not configured, plugin hooks path configured by the extension, plugin configured path expected")
    fun setGitHooksPluginConfiguredTest() {
        doThrow(ExecuteException(executeExceptionMessageMock, 1)).`when`(defaultExecutorMock).execute(cmdLineInitialMock)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn("")
        val setGitHooksActions = setGitHooks.actions;
        val setGitHooksAction = setGitHooksActions[0];
        val gitHookerExtension = project.extensions.getByName(GitHookerExtension.NAME) as GitHookerExtension
        gitHookerExtension.hooksPath = pluginConfigPathMock
        setGitHooksAction.execute(setGitHooks);

        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineConfigExecutedMock);
    }

    @Test(dependsOnMethods = ["setGitHooksActionAvailableTest"],
          description = "Git hooks path configured, plugin hooks path configured by the extension, plugin configured path expected")
    fun setGitHooksAllConfiguredTest() {
        `when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn(gitConfigPathMock)
        val setGitHooksActions = setGitHooks.actions;
        val setGitHooksAction = setGitHooksActions[0];
        val gitHookerExtension = project.extensions.getByName(GitHookerExtension.NAME) as GitHookerExtension
        gitHookerExtension.hooksPath = pluginConfigPathMock
        setGitHooksAction.execute(setGitHooks);

        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineConfigExecutedMock);
    }
}
