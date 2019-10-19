package com.pmachovec.githooker.constants.tasks

import com.pmachovec.githooker.constants.Groups
import com.pmachovec.githooker.extensions.GitHookerExtension

class SetGitHooksTask {
    companion object {
        const val GROUP = Groups.GITHOOKER
        const val NAME = "setGitHooks"
        const val DESCRIPTION = "Sets git hooks folder to the path specified in the " + GitHookerExtension.NAME + " extension."
    }
}
