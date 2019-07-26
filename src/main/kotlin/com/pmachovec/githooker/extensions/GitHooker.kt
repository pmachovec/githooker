package com.pmachovec.githooker.extensions

import com.pmachovec.githooker.defaultvalues.DefaultValues

class GitHooker {
    var hooksPath = DefaultValues.HOOKS_PATH
    var triggerTaskName = DefaultValues.TRIGGER_TASK_NAME

    companion object {
        const val NAME_FOR_PLUGIN = "githooker"
    }
}
