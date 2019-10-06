package com.pmachovec.githooker

import com.pmachovec.githooker.actions.SetGitHooksAction
import com.pmachovec.githooker.constants.Texts
import com.pmachovec.githooker.constants.tasks.SetGitHooksTask
import com.pmachovec.githooker.extensions.GitHookerExtension

import java.io.ByteArrayOutputStream
import java.io.PrintStream

import org.apache.commons.exec.DefaultExecutor
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginManager
import org.gradle.api.tasks.TaskContainer
import org.gradle.testfixtures.ProjectBuilder
import org.mockito.ArgumentMatchers.any
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
import org.testng.Assert.assertNotEquals
import org.testng.Assert.assertNotNull
import org.testng.Assert.assertNull
import org.testng.Assert.assertTrue

@PrepareForTest(
    ProjectBuilder::class,
    SetGitHooksAction::class
)
class GitHookerTriggerTaskTest : PowerMockTestCase() {
    private lateinit var projectBuilderMock: ProjectBuilder
    private lateinit var projectMock: Project
    private lateinit var extensionContainerMock: ExtensionContainer
    private lateinit var taskContainerMock: TaskContainer
    private lateinit var triggerTaskMock: Task
    private lateinit var triggerTaskActionMock: Action<Task>
    private lateinit var setGitHooksTaskMock: Task
    private lateinit var setGitHooksActionMock: Action<Task>
    private lateinit var pluginManagerMock: PluginManager
    private lateinit var defaultExecutorMock: DefaultExecutor
    private lateinit var project: Project

    private val triggerTaskMockName = "triggerTaskMock"
    private val nonExistingTaskNameMock = "nonExistingTaskNameMock"
    private val unknownTaskExceptionMessageMock = "unknownTaskExceptionMessageMock"
    private val testConsole = ByteArrayOutputStream()
    private val standardOutput = System.out

    private inline fun <reified T : Any> genericMock() = mock(T::class.java)

    @BeforeClass
    fun setupClass() {
        projectBuilderMock = mock(ProjectBuilder::class.java)
        projectMock = mock(Project::class.java)
        extensionContainerMock = mock(ExtensionContainer::class.java)
        taskContainerMock = mock(TaskContainer::class.java)
        triggerTaskMock = mock(Task::class.java)
        triggerTaskActionMock = genericMock()
        setGitHooksTaskMock = mock(Task::class.java)
        setGitHooksActionMock = genericMock()
        pluginManagerMock = mock(PluginManager::class.java)
        defaultExecutorMock = mock(DefaultExecutor::class.java)
    }

    @BeforeMethod
    fun setupMethod(params: Array<Any>) {
        mockStatic(ProjectBuilder::class.java)
        val gitHooker = GitHooker()
        val gitHookerExtension = GitHookerExtension()

        if (params.isNotEmpty()) {
            gitHookerExtension.triggerTaskName = params[0].toString()
        }

        whenNew(DefaultExecutor::class.java).withNoArguments().thenReturn(defaultExecutorMock)

        `when`(ProjectBuilder.builder()).thenReturn(projectBuilderMock)
        `when`(projectBuilderMock.build()).thenReturn(projectMock)
        `when`(projectMock.extensions).thenReturn(extensionContainerMock)
        `when`(extensionContainerMock.create(GitHookerExtension.NAME, GitHookerExtension::class.java)).thenReturn(gitHookerExtension)
        `when`(extensionContainerMock.findByName(GitHookerExtension.NAME)).thenReturn(gitHookerExtension)
        `when`(projectMock.pluginManager).thenReturn(pluginManagerMock)
        `when`(projectMock.task(SetGitHooksTask.NAME)).thenReturn(setGitHooksTaskMock)
        `when`(projectMock.tasks).thenReturn(taskContainerMock)
        `when`(taskContainerMock.getByName(triggerTaskMockName)).thenReturn(triggerTaskMock)
        `when`(taskContainerMock.getByName(SetGitHooksTask.NAME)).thenReturn(setGitHooksTaskMock)
        doThrow(UnknownTaskException(unknownTaskExceptionMessageMock)).`when`(taskContainerMock).getByName(nonExistingTaskNameMock)

        `when`(pluginManagerMock.apply(GitHooker::class.java)).then {
            gitHooker.apply(projectMock)
        }

        System.setOut(PrintStream(testConsole))
        project = ProjectBuilder.builder().build()
        project.pluginManager.apply(GitHooker::class.java)
    }

    @AfterMethod
    fun teardownMethod() {
        Mockito.reset(pluginManagerMock)
        System.setOut(standardOutput)
    }

    @DataProvider(name = "nonExistingTaskNameProvider")
    private fun nonExistingTaskNameProvider(): Iterator<String> {
        return listOf(nonExistingTaskNameMock).iterator()
    }

    @DataProvider(name = "triggerTaskNameProvider")
    private fun triggerTaskNameProvider(): Iterator<String> {
        return listOf(triggerTaskMockName).iterator()
    }

    @Test(description = "Trigger task not set, dependency setting not expected")
    fun applyTriggerTaskNotSetTest() {
        val gitHookerExtension = project.extensions.findByName(GitHookerExtension.NAME)

        assertNotNull(gitHookerExtension)
        assertTrue(gitHookerExtension is GitHookerExtension)
        gitHookerExtension as GitHookerExtension
        assertNull(gitHookerExtension.triggerTaskName)
        Mockito.verify(setGitHooksTaskMock, Mockito.times(0)).dependsOn(any())
    }

    @Test(
        dataProvider = "nonExistingTaskNameProvider",
        description = "Trigger task set, but doesn't exist, dependency setting not expected"
    )
    fun applyTriggerTaskSetNonExistingTest(triggerTaskName: String) {
        val expectedOutput = Texts.TRIGGER_TASK_NOT_FOUND.format(nonExistingTaskNameMock)

        assertNotEquals(testConsole.toString().indexOf(expectedOutput), -1)
        Mockito.verify(setGitHooksTaskMock, Mockito.times(0)).dependsOn(any())
    }

    @Test(
        dataProvider = "triggerTaskNameProvider",
        description = "Trigger task set, dependency setting expected"
    )
    fun applyTriggerTaskSetTest(triggerTaskName: String) {
        Mockito.verify(setGitHooksTaskMock, Mockito.times(1)).dependsOn(triggerTaskMock)
    }
}
