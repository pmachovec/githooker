package com.pmachovec.githooker.actions

import com.pmachovec.githooker.GitHooker
import com.pmachovec.githooker.constants.DefaultValues
import com.pmachovec.githooker.constants.Texts
import com.pmachovec.githooker.constants.tasks.SetGitHooksTask
import com.pmachovec.githooker.extensions.GitHookerExtension

import java.io.ByteArrayOutputStream
import java.io.PrintStream

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
import org.testng.Assert.assertNotEquals
import org.testng.Assert.assertNotNull

@PrepareForTest(CommandLine::class,
                SetGitHooksAction::class) // A class, in which mocked constructors are called, must be prepared for test.
class SetGitHooksActionTest: PowerMockTestCase() {
    private lateinit var byteArrayOutputStreamMock: ByteArrayOutputStream
    private lateinit var cmdLineInitialMock: CommandLine
    private lateinit var cmdLineGitConfigCommandMock: CommandLine
    private lateinit var defaultExecutorMock: DefaultExecutor
    private lateinit var pumpStreamHandlerMock: PumpStreamHandler
    private lateinit var project: Project
    private lateinit var setGitHooksTask: Task
    private var testConsole = ByteArrayOutputStream()
    private var standardOutput = System.out
    private var executeExceptionMessageMock = "executeExceptionMessageMock"
    private var gitConfigPathMock = "gitConfigPathMock"
    private var pluginConfigPathMock = "pluginConfigPathMock"

    @BeforeClass
    fun setupClass() {
        byteArrayOutputStreamMock = mock(ByteArrayOutputStream::class.java)
        cmdLineInitialMock = mock(CommandLine::class.java)
        cmdLineGitConfigCommandMock = mock(CommandLine::class.java)
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
        `when`(CommandLine.parse("${DefaultValues.GIT_CONFIG_COMMAND} $pluginConfigPathMock")).thenReturn(cmdLineGitConfigCommandMock)

        whenNew(ByteArrayOutputStream::class.java).withAnyArguments().thenReturn(byteArrayOutputStreamMock)
        whenNew(DefaultExecutor::class.java).withNoArguments().thenReturn(defaultExecutorMock)
        whenNew(PumpStreamHandler::class.java).withArguments(byteArrayOutputStreamMock).thenReturn(pumpStreamHandlerMock)

        // Plugin must be applied only after static and constructor mocks are created, otherwise, original classes would be used
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
        setGitHooksTask = project.tasks.getByName(SetGitHooksTask.NAME)
        System.setOut(PrintStream(testConsole))
    }

    @AfterMethod
    fun teardownMethod() {
        Mockito.reset(defaultExecutorMock)
        System.setOut(standardOutput)
    }

    @Test
    fun setGitHooksActionAvailableTest() {
        val setGitHooksActions = setGitHooksTask.actions
        assertNotNull(setGitHooksActions)
        assertEquals(setGitHooksActions.size, 1)
    }

    @Test(dependsOnMethods = ["setGitHooksActionAvailableTest"],
          description = "Git hooks path not configured, plugin hooks path not configured, command execution not expected")
    fun executeNothingConfiguredTest() {
        doThrow(ExecuteException(executeExceptionMessageMock, 1)).`when`(defaultExecutorMock).execute(cmdLineInitialMock)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn("")
        val setGitHooksAction = setGitHooksTask.actions[0]
        setGitHooksAction.execute(setGitHooksTask)

        assertNotEquals(testConsole.toString().indexOf(Texts.PATH_NOT_CONFIGURED), -1)
    }

    @Test(dependsOnMethods = ["setGitHooksActionAvailableTest"],
          description = "Git hooks path configured, plugin hooks path not configured, command execution not expected")
    fun executeGitConfiguredTest() {
        /*
         * When the plugin is applied and no path is configured, it uses the plugin-default path,
         * even when there is a different path configured for Git itself.
         */
        `when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn(gitConfigPathMock)
        val setGitHooksAction = setGitHooksTask.actions[0]
        setGitHooksAction.execute(setGitHooksTask)

        assertNotEquals(testConsole.toString().indexOf(Texts.PATH_NOT_CONFIGURED), -1)
    }

    @Test(dependsOnMethods = ["setGitHooksActionAvailableTest"],
          description = "Git hooks path not configured, plugin hooks path configured, command execution expected")
    fun executePluginConfiguredTest() {
        doThrow(ExecuteException(executeExceptionMessageMock, 1)).`when`(defaultExecutorMock).execute(cmdLineInitialMock)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn("")
        val setGitHooksActions = setGitHooksTask.actions
        val setGitHooksAction = setGitHooksActions[0]
        val gitHookerExtension = project.extensions.getByName(GitHookerExtension.NAME) as GitHookerExtension
        gitHookerExtension.hooksPath = pluginConfigPathMock
        setGitHooksAction.execute(setGitHooksTask)

        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineGitConfigCommandMock)
    }

    @Test(dependsOnMethods = ["setGitHooksActionAvailableTest"],
          description = "Git hooks path configured, plugin hooks path configured, command execution expected")
    fun executeAllConfiguredTest() {
        `when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn(gitConfigPathMock)
        val setGitHooksActions = setGitHooksTask.actions
        val setGitHooksAction = setGitHooksActions[0]
        val gitHookerExtension = project.extensions.getByName(GitHookerExtension.NAME) as GitHookerExtension
        gitHookerExtension.hooksPath = pluginConfigPathMock
        setGitHooksAction.execute(setGitHooksTask)

        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineGitConfigCommandMock)
    }
}
