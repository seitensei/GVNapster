package com.github.seitensei.dcindex

import java.util.*;
import java.net.*;
import java.io.*;

class IndexServer {}

fun randPort(): Int {
    val rng = Random()
    var port = rng.nextInt(32769) + 32768 // generate port on range of 32768-61000
    Logger.log("Testing ${port}.")
    try {
        var socktest = ServerSocket(port)
        socktest.close()
    } catch (e: Exception) {
        Logger.log("${port} in use.")
        return -1 // return -1 if port cannot be used
    }
    Logger.log("Using ${port}.")
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
    Logger.log("Server initialization started.")
    dirInit()
    var db = DataHandler
    db.initTables()
    Logger.log("Initialization completed.")
    var listSock = ServerSocket(9060)
    Logger.log("Server listening on ${listSock.inetAddress.hostAddress}:${listSock.localPort}")
    while(true) {
        var listConn = listSock.accept();
        Logger.log("Connection from ${listConn.inetAddress.hostName}:${listConn.port}.")

        // generating thread socket
        Logger.log("Searching for available port.")
        var port = -1
        while(port == -1) {
            port = randPort()
        }
        Logger.log("Port available on ${port}. Establishing socket for listening.")
        var branchSocket = ServerSocket(port)

        val portExport = "PORT: " + port + "\r\n"
        var portOut: BufferedWriter = BufferedWriter(OutputStreamWriter(listConn.outputStream))
        portOut.write(portExport, 0, portExport.length)
        portOut.flush()
        Logger.log("${port} sent to client for usage.")

        var branchConn = branchSocket.accept()
        Logger.log("Connection from ${branchSocket.inetAddress.hostName}:${branchConn.port}.")
        listConn.close()
        Logger.log("Terminating client connection.")

        // new thread
        var branchRunner = IndexRunner(branchConn)
        var branchThread = Thread(branchRunner)

        branchThread.start()

    }
}