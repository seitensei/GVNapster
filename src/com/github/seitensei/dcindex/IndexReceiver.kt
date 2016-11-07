package com.github.seitensei.dcindex

import java.io.*;
import java.net.*;

class IndexReceiver(conn: Socket, command: String, status: Boolean): Runnable {
    var sock = conn
    var command = command

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
            Logger.log("Starting Metadata Upload Process.")
            System.out.println("META DATA UPLOAD START")
            try {
                //var fileOutput: BufferedWriter = BufferedWriter(FileWriter(File("./tmp/" + sock.inetAddress.hostName)))
                var fileOutput = FileWriter(File("./tmp/" + sock.inetAddress.hostAddress))
                // TODO: Make byte array big and work
                var blob: ByteArray = ByteArray(655135)
                var fileInput = fileIn.read(blob)
                Logger.log("Waiting for read.")
                fileOutput.write(fileInput)
                fileOutput.flush()
                fileOutput.close()
            } catch(e: Exception) {
                System.out.println("Metadata Upload Failed")
                throw e
            }

        }

        sock.close()

    }
}