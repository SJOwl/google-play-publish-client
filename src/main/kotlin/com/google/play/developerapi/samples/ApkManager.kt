package com.google.play.developerapi.samples

import java.io.File
import java.nio.file.Files

class ApkManager(
    private val publishConfig: PublishConfig
) {

    fun copyFiles() {
        File(publishConfig.buildFolder)
            .listFiles()
            ?.filter { it.extension == "apk" }
            ?.forEach {
                if (!File(publishConfig.outputApkFolder, it.name).exists()) {
                    Files.copy(it.toPath(), File(publishConfig.outputApkFolder, it.name).toPath())
                }
            }
    }
}

fun propertiesPath(args: Array<String>): String {
    return try {
        args[0]
    } catch (e: Exception) {
        println("First argument must be a path to properties file")
        throw(e)
    }
}