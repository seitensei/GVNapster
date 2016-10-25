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

        var fileOut: OutputStream = sock.outputStream
        var fileIn: InputStream = sock.inputStream

        if(command.equals("META")) {
            // TODO: Upload Metadata from Client
            System.out.println("Let's META DATA")
        }

        sock.close()

    }
}