package com.pmachovec.githooker.constants.tasks

import com.pmachovec.githooker.constants.Groups
import com.pmachovec.githooker.extensions.GitHookerExtension

object SetGitHooksTask {
    const val GROUP = Groups.GITHOOKER
    const val NAME = "setGitHooks"
    const val DESCRIPTION = "Sets git hooks folder to the path specified in the " + GitHookerExtension.NAME + " extension."
}
