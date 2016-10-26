package com.github.seitensei.dcindex

import java.io.*;
import java.net.*;

class IndexReceiver(conn: Socket, command: String): Runnable {
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

        var fileOut: DataOutputStream = DataOutputStream(sock.outputStream)
        var fileIn: DataInputStream = DataInputStream(sock.inputStream)

        if(command.equals("META")) {
            // TODO: Upload Metadata from Client
            System.out.println("Let's META DATA")
            try {
                var fileOutput: BufferedWriter = BufferedWriter(FileWriter(File("./tmp/" + sock.inetAddress.hostName)))
                var inputString: String = "init"

                while(!(inputString.equals("EOF\r\n"))) {
                    inputString = fileIn.readUTF()
                    fileOutput.write(inputString + "\r\n")
                    fileOutput.flush()
                }
                fileOutput.close()
            } catch(e: Exception) {
                System.out.println("Metadata Upload Failed")
                throw e
            }

        }

        sock.close()

    }
}