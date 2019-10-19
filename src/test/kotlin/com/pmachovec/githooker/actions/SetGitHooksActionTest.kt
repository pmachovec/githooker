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
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.testng.PowerMockTestCase
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotEquals
import org.testng.Assert.assertNotNull
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@PrepareForTest(
    CommandLine::class,
    SetGitHooksAction::class // A class, in which mocked constructors are called, must be prepared for test.
)
class SetGitHooksActionTest : PowerMockTestCase() {
    private val executeExceptionMessage = "executeExceptionMessage"
    private val gitConfigPath = "gitConfigPath"
    private val pluginConfigPath = "pluginConfigPath"
    private val testConsole = ByteArrayOutputStream()
    private val standardOutput = System.out
    private lateinit var gitHookerExtension: GitHookerExtension
    private lateinit var setGitHooksTask: Task
    private lateinit var setGitHooksActions: List<Action<in Task>>

    @Mock
    private lateinit var byteArrayOutputStreamMock: ByteArrayOutputStream

    @Mock
    private lateinit var cmdLineInitialMock: CommandLine

    @Mock
    private lateinit var cmdLineGitConfigCommandMock: CommandLine

    @Mock
    private lateinit var defaultExecutorMock: DefaultExecutor

    @Mock
    private lateinit var pumpStreamHandlerMock: PumpStreamHandler

    @BeforeClass
    fun setupClass() {
        System.setOut(PrintStream(testConsole))
    }

    @BeforeMethod
    fun setupMethod(params: Array<Any>) {
        // Static and behavior mocking doesn't work in @BeforeClass methods.
        PowerMockito.mockStatic(CommandLine::class.java)
        PowerMockito.whenNew(DefaultExecutor::class.java).withNoArguments().thenReturn(defaultExecutorMock)
        PowerMockito.whenNew(ByteArrayOutputStream::class.java).withAnyArguments().thenReturn(byteArrayOutputStreamMock)
        PowerMockito.whenNew(PumpStreamHandler::class.java).withArguments(byteArrayOutputStreamMock).thenReturn(pumpStreamHandlerMock)
        PowerMockito.`when`(CommandLine.parse(ArgumentMatchers.anyString())).thenReturn(cmdLineInitialMock)
        PowerMockito.`when`(CommandLine.parse("${DefaultValues.GIT_CONFIG_COMMAND} $pluginConfigPath")).thenReturn(cmdLineGitConfigCommandMock)

        // Plugin must be applied only after behavior mocks are created, otherwise, original classes and methods would be used.
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
        gitHookerExtension = project.extensions.getByName(GitHookerExtension.NAME) as GitHookerExtension
        setGitHooksTask = project.tasks.getByName(SetGitHooksTask.NAME)
        setGitHooksActions = setGitHooksTask.actions
    }

    @AfterClass
    fun teardownMethod() {
        System.setOut(standardOutput)
    }

    @Test
    fun actionAvailableTest() {
        assertNotNull(setGitHooksActions)
        assertEquals(setGitHooksActions.size, 1)
    }

    @Test(
        dependsOnMethods = ["actionAvailableTest"],
        description = "Git hooks path not configured, plugin hooks path not configured, command execution not expected"
    )
    fun executeNothingConfiguredTest() {
        PowerMockito.doThrow(ExecuteException(executeExceptionMessage, 1)).`when`(defaultExecutorMock).execute(cmdLineInitialMock)
        PowerMockito.`when`(byteArrayOutputStreamMock.toString()).thenReturn("")
        setGitHooksActions[0].execute(setGitHooksTask)

        assertNotEquals(testConsole.toString().indexOf(Texts.PATH_NOT_CONFIGURED), -1)
    }

    @Test(
        dependsOnMethods = ["actionAvailableTest"],
        description = "Git hooks path configured, plugin hooks path not configured, command execution not expected"
    )
    fun executeGitConfiguredTest() {
        PowerMockito.`when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        PowerMockito.`when`(byteArrayOutputStreamMock.toString()).thenReturn(gitConfigPath)
        setGitHooksActions[0].execute(setGitHooksTask)
        assertNotEquals(testConsole.toString().indexOf(Texts.PATH_NOT_CONFIGURED), -1)
    }

    @Test(
        dependsOnMethods = ["actionAvailableTest"],
        description = "Git hooks path not configured, plugin hooks path configured, command execution expected"
    )
    fun executePluginConfiguredTest() {
        gitHookerExtension.hooksPath = pluginConfigPath
        PowerMockito.doThrow(ExecuteException(executeExceptionMessage, 1)).`when`(defaultExecutorMock).execute(cmdLineInitialMock)
        PowerMockito.`when`(byteArrayOutputStreamMock.toString()).thenReturn("")
        setGitHooksActions[0].execute(setGitHooksTask)
        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineGitConfigCommandMock)
    }

    @Test(
        dependsOnMethods = ["actionAvailableTest"],
        description = "Git hooks path configured, plugin hooks path configured, they differ, command execution expected"
    )
    fun executeBothConfiguredDifferentTest() {
        gitHookerExtension.hooksPath = pluginConfigPath
        PowerMockito.`when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        PowerMockito.`when`(byteArrayOutputStreamMock.toString()).thenReturn(gitConfigPath)
        setGitHooksActions[0].execute(setGitHooksTask)
        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineGitConfigCommandMock)
    }

    @Test(
        dependsOnMethods = ["actionAvailableTest"],
        description = "Git hooks path configured, plugin hooks path configured, they are same, command execution not expected"
    )
    fun executeBothConfiguredSameTest() {
        gitHookerExtension.hooksPath = pluginConfigPath
        PowerMockito.`when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        PowerMockito.`when`(byteArrayOutputStreamMock.toString()).thenReturn(pluginConfigPath)
        setGitHooksActions[0].execute(setGitHooksTask)
        Mockito.verify(defaultExecutorMock, Mockito.times(0)).execute(cmdLineGitConfigCommandMock)
    }
}
