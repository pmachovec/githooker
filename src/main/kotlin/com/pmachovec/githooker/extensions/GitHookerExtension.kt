package com.pmachovec.githooker.extensions

open class GitHookerExtension {
    var hooksPath: String? = null
    var triggerTaskName: String? = null

    companion object {
        const val NAME = "githooker"
    }
}
