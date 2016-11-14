package com.github.seitensei.dcindex

import java.io.*;
import java.net.*;

class IndexReceiver(var sock: Socket, var command: String, @Volatile var status: Boolean): Runnable {

    override fun run() {
        try {
            Logger.log("Initiating Metadata Upload Proc")
            proc()
        } catch (e: Exception) {
            Logger.log(e.stackTrace.toString())
            throw e
        }
    }

    fun proc() {
        val fileIn: BufferedReader = BufferedReader(InputStreamReader(sock.inputStream))
        Logger.log("Command was $command")
        if(command.equals("META")) {
            // TODO: Upload Metadata from Client
            Logger.log("--- Starting Metadata Upload Process ---")
            System.out.println("META DATA UPLOAD START")
            try {
                var fileOutput: BufferedWriter = BufferedWriter(FileWriter(File("tmp/" + sock.inetAddress.hostAddress)))
                //var fileOutput = FileWriter(File("tmp/" + sock.inetAddress.hostAddress))
                var readFile: String
                Logger.log("writing to file.")
                while(true) {
                    readFile = fileIn.readLine()
                    if(!readFile.contains("EOF")) {
                        Logger.log("Writing Line: $readFile")
                        fileOutput.write(readFile)
                        //fileOutput.write("\r\n")
                        fileOutput.flush()
                    } else {
                        break
                    }
                }
                fileOutput.close()
                Logger.log("File Write Ended.")

                // Parse it!
                Logger.log("Feeding tmp/" + sock.inetAddress.hostAddress + " into parser.")
                var xmlAgent = XMLHandler(File("tmp/" + sock.inetAddress.hostAddress))
                Logger.log("Add Connection to Database")
                xmlAgent.addToConn()
                Logger.log("Update File Index")
                xmlAgent.refreshFiles()
                status = true
            } catch(e: Exception) {
                System.out.println("Metadata Upload and Update Failed")
                status = false
                throw e
            }

        }

        sock.close()

    }

    fun runStatus(): Boolean {
        return status
    }
}