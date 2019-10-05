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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.TaskContainer
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
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNotEquals
import org.testng.Assert.assertNotNull

@PrepareForTest(CommandLine::class,
                ProjectBuilder::class,
                SetGitHooksAction::class) // A class, in which mocked constructors are called, must be prepared for test.
class SetGitHooksActionTest: PowerMockTestCase() {
    private lateinit var projectBuilderMock: ProjectBuilder
    private lateinit var projectMock: Project
    private lateinit var pluginManagerMock: PluginManager
    private lateinit var extensionContainerMock: ExtensionContainer
    private lateinit var taskContainerMock: TaskContainer
    private lateinit var taskMock: Task
    private lateinit var byteArrayOutputStreamMock: ByteArrayOutputStream
    private lateinit var cmdLineInitialMock: CommandLine
    private lateinit var cmdLineGitConfigCommandMock: CommandLine
    private lateinit var defaultExecutorMock: DefaultExecutor
    private lateinit var pumpStreamHandlerMock: PumpStreamHandler
    private lateinit var setGitHooksTask: Task
    private lateinit var setGitHooksActions: List<Action<in Task>>

    private val executeExceptionMessageMock = "executeExceptionMessageMock"
    private val gitConfigPathMock = "gitConfigPathMock"
    private val pluginConfigPathMock = "pluginConfigPathMock"
    private val testConsole = ByteArrayOutputStream()
    private val standardOutput = System.out

    @BeforeClass
    fun setupClass() {
        projectBuilderMock = mock(ProjectBuilder::class.java)
        projectMock = mock(Project::class.java)
        pluginManagerMock = mock(PluginManager::class.java)
        extensionContainerMock = mock(ExtensionContainer::class.java)
        taskContainerMock = mock(TaskContainer::class.java)
        taskMock = mock(Task::class.java)
        byteArrayOutputStreamMock = mock(ByteArrayOutputStream::class.java)
        cmdLineInitialMock = mock(CommandLine::class.java)
        cmdLineGitConfigCommandMock = mock(CommandLine::class.java)
        defaultExecutorMock = mock(DefaultExecutor::class.java)
        pumpStreamHandlerMock = mock(PumpStreamHandler::class.java)
    }

    @BeforeMethod
    fun setupMethod(params: Array<Any>) {
        /*
         * Static and behavior mocking doesn't work in @BeforeClass methods. Everything but non-static mocks initialization
         * must be done for each test separately.
         */
        mockStatic(CommandLine::class.java)
        mockStatic(ProjectBuilder::class.java)
        val gitHookerExtension = GitHookerExtension()

        if (params.isNotEmpty()) {
            gitHookerExtension.hooksPath = params[0].toString()
        }

        // The DefaultExecutor constructor must be mocked before its usage - before creating the SetGitHooksAction instance
        whenNew(DefaultExecutor::class.java).withNoArguments().thenReturn(defaultExecutorMock)
        whenNew(ByteArrayOutputStream::class.java).withAnyArguments().thenReturn(byteArrayOutputStreamMock)
        whenNew(PumpStreamHandler::class.java).withArguments(byteArrayOutputStreamMock).thenReturn(pumpStreamHandlerMock)

        /*
         * Must be created separately. If the constructor is called directly in the `when` command, it's mocking inside
         * another mocking (there are mocked constructors and static classes inside SetGitHooksAction) and that doesn't work.
         */
        val setGitHooksAction = SetGitHooksAction(gitHookerExtension.hooksPath)
        `when`(ProjectBuilder.builder()).thenReturn(projectBuilderMock)
        `when`(projectBuilderMock.build()).thenReturn(projectMock)
        `when`(projectMock.extensions).thenReturn(extensionContainerMock)
        `when`(extensionContainerMock.create(GitHookerExtension.NAME, GitHookerExtension::class.java)).thenReturn(gitHookerExtension)
        `when`(projectMock.pluginManager).thenReturn(pluginManagerMock)
        `when`(projectMock.tasks).thenReturn(taskContainerMock)
        `when`(taskContainerMock.getByName(SetGitHooksTask.NAME)).thenReturn(taskMock)
        `when`(taskMock.actions).thenReturn(mutableListOf(setGitHooksAction) as List<Action<in Task>>)
        `when`(CommandLine.parse(anyString())).thenReturn(cmdLineInitialMock)
        `when`(CommandLine.parse("${DefaultValues.GIT_CONFIG_COMMAND} $pluginConfigPathMock")).thenReturn(cmdLineGitConfigCommandMock)
        System.setOut(PrintStream(testConsole))

        // Plugin must be applied only after static and constructor mocks are created, otherwise, original classes would be used
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
        setGitHooksTask = project.tasks.getByName(SetGitHooksTask.NAME)
        setGitHooksActions = setGitHooksTask.actions
    }

    @AfterMethod
    fun teardownMethod() {
        Mockito.reset(defaultExecutorMock)
        System.setOut(standardOutput)
    }

    @DataProvider(name = "pluginConfigPathMockProvider")
    private fun pluginConfigPathMockProvider(): Iterator<String> {
        return listOf(pluginConfigPathMock).iterator()
    }

    @Test
    fun actionAvailableTest() {
        assertNotNull(setGitHooksActions)
        assertEquals(setGitHooksActions.size, 1)
    }

    @Test(dependsOnMethods = ["actionAvailableTest"],
          description = "Git hooks path not configured, plugin hooks path not configured, command execution not expected")
    fun executeNothingConfiguredTest() {
        doThrow(ExecuteException(executeExceptionMessageMock, 1)).`when`(defaultExecutorMock).execute(cmdLineInitialMock)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn("")
        setGitHooksActions[0].execute(setGitHooksTask)

        assertNotEquals(testConsole.toString().indexOf(Texts.PATH_NOT_CONFIGURED), -1)
    }

    @Test(dependsOnMethods = ["actionAvailableTest"],
          description = "Git hooks path configured, plugin hooks path not configured, command execution not expected")
    fun executeGitConfiguredTest() {
        `when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn(gitConfigPathMock)
        setGitHooksActions[0].execute(setGitHooksTask)

        assertNotEquals(testConsole.toString().indexOf(Texts.PATH_NOT_CONFIGURED), -1)
    }

    @Test(dataProvider = "pluginConfigPathMockProvider",
          dependsOnMethods = ["actionAvailableTest"],
          description = "Git hooks path not configured, plugin hooks path configured, command execution expected")
    fun executePluginConfiguredTest(pluginConfigPath: String) {
        doThrow(ExecuteException(executeExceptionMessageMock, 1)).`when`(defaultExecutorMock).execute(cmdLineInitialMock)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn("")
        setGitHooksActions[0].execute(setGitHooksTask)

        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineGitConfigCommandMock)
    }

    @Test(dataProvider = "pluginConfigPathMockProvider",
          dependsOnMethods = ["actionAvailableTest"],
          description = "Git hooks path configured, plugin hooks path configured, command execution expected")
    fun executeAllConfiguredTest(pluginConfigPath: String) {
        `when`(defaultExecutorMock.execute(cmdLineInitialMock)).thenReturn(0)
        `when`(byteArrayOutputStreamMock.toString()).thenReturn(gitConfigPathMock)
        setGitHooksActions[0].execute(setGitHooksTask)

        Mockito.verify(defaultExecutorMock, Mockito.times(1)).execute(cmdLineGitConfigCommandMock)
    }
}
