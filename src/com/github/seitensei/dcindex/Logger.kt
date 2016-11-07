package com.github.seitensei.dcindex

import java.io.*
import java.text.*
import java.util.*

object Logger {
    var fileName: String = "log.txt"
    var fileWriter = BufferedWriter(FileWriter(fileName))

    fun log(message: String) {
        var timestamp = SimpleDateFormat("[dd-MM-yyyy HH:mm:ss]").format(Date())
        fileWriter.write("${timestamp}: $message\n")
        fileWriter.flush()
    }

}