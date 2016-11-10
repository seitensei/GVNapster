package com.github.seitensei.dcindex

import java.io.*;
import java.net.*;

class IndexReceiver(conn: Socket, command: String, status: Boolean, fileLength: Int): Runnable {
    var sock = conn
    var command = command
    var fileLength = fileLength

    override fun run() {
        try {
            proc()
        } catch (e: Exception) {
            throw e
        }
    }

    fun proc() {

        //var fileOut: DataOutputStream = DataOutputStream(sock.outputStream)
        //var fileIn: DataInputStream = DataInputStream(sock.inputStream)
        var fileIn: BufferedInputStream = BufferedInputStream(sock.inputStream)

        if(command.equals("META")) {
            // TODO: Upload Metadata from Client
            Logger.log("--- Starting Metadata Upload Process ---")
            System.out.println("META DATA UPLOAD START")
            try {
                //var fileOutput: BufferedWriter = BufferedWriter(FileWriter(File("./tmp/" + sock.inetAddress.hostName)))
                var fileOutput = FileWriter(File("./tmp/" + sock.inetAddress.hostAddress))
                // TODO: Make byte array big and work
                var blob: ByteArray = ByteArray(fileLength)
                Logger.log("Waiting for read.")
                var fileInput = fileIn.read(blob, 0, fileLength)

                Logger.log("writing to file.")
                fileOutput.write(fileInput)
                fileOutput.flush()
                fileOutput.close()
                Logger.log("File Write Ended.")

                // Parse it!
                Logger.log("Feeding ./tmp/" + sock.inetAddress.hostAddress + " into parser.")
                var xmlAgent = XMLHandler(File("./tmp/" + sock.inetAddress.hostAddress))
                Logger.log("Add Connection to Database")
                xmlAgent.addToConn()

            } catch(e: Exception) {
                System.out.println("Metadata Upload and Update Failed")
                throw e
            }

        }

        sock.close()

    }
}