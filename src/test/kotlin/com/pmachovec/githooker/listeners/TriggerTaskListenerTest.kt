package com.pmachovec.githooker.listeners

import com.pmachovec.githooker.constants.tasks.SetGitHooksTask
import com.pmachovec.githooker.extensions.GitHookerExtension

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskState
import org.mockito.Mock
import org.mockito.Mockito
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.testng.PowerMockTestCase
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

@PrepareForTest(
    TriggerTaskListener::class
)
class TriggerTaskListenerTest : PowerMockTestCase() {
    private val triggerTaskListener = TriggerTaskListener()
    private val triggerTaskNameMock = "triggerTaskNameMock"
    private lateinit var gitHookerExtension: GitHookerExtension

    @Mock
    private lateinit var projectMock: Project

    @Mock
    private lateinit var extensionContainerMock: ExtensionContainer

    @Mock
    private lateinit var taskContainerMock: TaskContainer

    @Mock
    private lateinit var setGitHooksTaskMock: Task

    @Mock
    private lateinit var triggerTaskMock: Task

    @Mock
    private lateinit var setGitHooksActionMock: Action<in Task>

    @Mock
    private lateinit var triggerTaskActionMock: Action<in Task>

    @Mock
    private lateinit var triggerTaskStateMock: TaskState

    @BeforeMethod
    fun setupMethod() {
        // The extension must be initiated separately for each test because each test sets a different extension configuration
        gitHookerExtension = GitHookerExtension()
        PowerMockito.`when`(projectMock.extensions).thenReturn(extensionContainerMock)
        PowerMockito.`when`(projectMock.tasks).thenReturn(taskContainerMock)
        PowerMockito.`when`(extensionContainerMock.getByName(GitHookerExtension.NAME)).thenReturn(gitHookerExtension)
        PowerMockito.`when`(taskContainerMock.getByName(SetGitHooksTask.NAME)).thenReturn(setGitHooksTaskMock)
        PowerMockito.`when`(setGitHooksTaskMock.actions).thenReturn(listOf(setGitHooksActionMock))
        PowerMockito.`when`(triggerTaskMock.project).thenReturn(projectMock)
        PowerMockito.`when`(triggerTaskMock.name).thenReturn(triggerTaskNameMock)

        PowerMockito.`when`(triggerTaskActionMock.execute(triggerTaskMock)).then {
            triggerTaskListener.afterExecute(triggerTaskMock, triggerTaskStateMock)
        }
    }

    @Test
    fun triggerTaskNotConfiguredTest() {
        triggerTaskActionMock.execute(triggerTaskMock)
        Mockito.verify(setGitHooksActionMock, Mockito.times(0)).execute(setGitHooksTaskMock)
    }

    @Test
    fun triggerTaskConfiguredTest() {
        gitHookerExtension.triggerTaskName = triggerTaskNameMock
        triggerTaskActionMock.execute(triggerTaskMock)
        Mockito.verify(setGitHooksActionMock, Mockito.times(1)).execute(setGitHooksTaskMock)
    }

    @Test
    fun beforeExecuteCoverWorkaroundTest() {
        triggerTaskListener.beforeExecute(triggerTaskMock)
    }
}
