package com.github.seitensei.dcindex

import java.util.*;
import java.net.*;
import java.io.*;

class IndexServer {}

fun randPort(): Int {
    val rng = Random()
    var port = rng.nextInt(32769) + 32768 // generate port on range of 32768-61000

    try {
        var socktest = ServerSocket(port)
        socktest.close()
    } catch (e: Exception) {
        return -1 // return -1 if port cannot be used
    }
    return port
}

/* dirInit()
 * Deletes the temporary and data directories, and recreates them for use.
 */
fun dirInit() {
    var dataDir: File = File("./data")
    var tmpDir: File = File("./tmp")
    dataDir.delete()
    tmpDir.delete()
    dataDir.mkdir()
    tmpDir.mkdir()
}

fun main(args: Array<String>) {
    dirInit()
    var db = DataHandler
    db.setup()
    var listSock = ServerSocket(9060)
    System.out.println("Listening on 9060")
    while(true) {
        System.out.println("Accepting Connections.")
        var listConn = listSock.accept();
        System.out.println("Accepted: " + listConn.inetAddress)

        // generating thread socket
        var port = -1
        while(port == -1) {
            port = randPort()
        }
        System.out.println("Port available: " + port)

        var branchSocket = ServerSocket(port)

        val portExport = "PORT: " + port + "\r\n"
        var portOut: BufferedWriter = BufferedWriter(OutputStreamWriter(listConn.outputStream))
        portOut.write(portExport, 0, portExport.length)
        portOut.flush()
        System.out.println("Sent Port # $port to output.")

        var branchConn = branchSocket.accept()

        listConn.close()

        // new thread
        var branchRunner = IndexRunner(branchConn)
        var branchThread = Thread(branchRunner)

        branchThread.start()

    }
}