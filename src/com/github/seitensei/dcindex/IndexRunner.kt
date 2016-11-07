package com.github.seitensei.dcindex

import java.net.*;
import java.io.*;
import java.util.*

class IndexRunner(conn: Socket): Runnable {
    var sock = conn

    override fun run() {
        try {
            proc()
        } catch (e: Exception) {
            System.out.println(e)
            throw e
        }
    }

    fun proc() {
        var outStream: BufferedWriter = BufferedWriter(OutputStreamWriter(sock.outputStream))
        var inStream: BufferedReader = BufferedReader(InputStreamReader(sock.inputStream))

        while(true) {
            Logger.log("Sending connection readiness to client.")
            writeToClient("Line ready.", outStream)
            var command = inStream.readLine()
            Logger.log("Received command $command")
            var tokenizer = StringTokenizer(command, " ")
            var parsed = tokenizer.nextToken()
            Logger.log("Tokenized element: $parsed" )

            if(parsed == "META") {
                Logger.log("Received META command from client.")
                // TODO: Prompt for and receive metadata
                var status = false
                var port = -1
                while(port == -1) {
                    port = randPort()
                }
                var recSock = ServerSocket(port)
                writeToClient("PORT: " + port, outStream)
                var recConn = recSock.accept()

                var recRunner = IndexReceiver(recConn, command, status) // TODO: IndexReceiver command decoding?
                var recThread = Thread(recRunner)

                recThread.start()

                if(status) {
                    Logger.log("Transfer completed successfully")
                } else {
                    Logger.log("Transfer failed.")
                }


            }
            if(parsed == "SEARCH") {

            }
            if(parsed == "QUIT") {
                // TODO: Delete Client from Data
                break

            }
        }

        outStream.close()
        inStream.close()
        sock.close()


    }

    fun writeToClient(res: String, os: BufferedWriter) {
        var text = res + "\n\r" // CRLF
        os.write(text, 0, text.length)
        os.flush()
    }

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
}