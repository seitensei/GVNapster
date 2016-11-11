package com.github.seitensei.dcindex


import org.w3c.dom.*
import java.io.*
import java.util.*
import javax.xml.parsers.*

/**
 * XMLHandler
 * Accesses and inserts XML data into appropriate database tables.
 * We assume that DB is already initialized.
 */
class XMLHandler(file: File){
    var parseFile: File = file



    fun parseTest() {
        try {
            var dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            var dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            var doc: Document = dBuilder.parse(parseFile)

            doc.documentElement.normalize()

            Logger.log("Root Element :" + doc.documentElement.nodeName)
            var nList: NodeList = doc.getElementsByTagName("file")
            var i = 0;
            while(i < nList.length) {
                var nNode: Node = nList.item(i)
                Logger.log("Current Element: " + nNode.nodeName)
                if(nNode.nodeType == Node.ELEMENT_NODE) {
                    var eElement: Element = nNode as Element;
                    Logger.log("File Name: " + eElement.getElementsByTagName("name").item(0).textContent)
                    Logger.log("Desc: " + eElement.getElementsByTagName("desc").item(0).textContent)
                }
                i++
            }

        } catch (e: Exception) {
            Logger.log("--- Unable to Parse ---")
            Logger.log(e.stackTrace.toString())
        }
    }

    fun addToConn() {
        try {
            var dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            var dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            var doc: Document = dBuilder.parse(parseFile)

            doc.documentElement.normalize()

            var nodeList = doc.getElementsByTagName("connection")
            var connNode = nodeList.item(0) as Element
            var peerName = connNode.getElementsByTagName("username").item(0).textContent
            var peerAddr = connNode.getElementsByTagName("address").item(0).textContent
            var peerRate = connNode.getElementsByTagName("speed").item(0).textContent

            Logger.log("Parsed Connection as $peerName@$peerAddr with rate of $peerRate")
            DataHandler.addPeer(peerName, peerAddr, peerRate)
        } catch (e: Exception) {
            Logger.log("--- Unable to Parse ---")
            Logger.log(e.stackTrace.toString())
        }
    }

    fun refreshFiles() {
            try {
                var dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                var dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
                var doc: Document = dBuilder.parse(parseFile)

                doc.documentElement.normalize()

                var nodeList = doc.getElementsByTagName("connection")
                var connNode = nodeList.item(0) as Element
                var peerName = connNode.getElementsByTagName("username").item(0).textContent
                var peerAddr = connNode.getElementsByTagName("address").item(0).textContent

                DataHandler.dropFiles(peerName, peerAddr)
                var nList: NodeList = doc.getElementsByTagName("file")
                var i = 0;
                while(i < nList.length) {
                    var nNode: Node = nList.item(i)
                    Logger.log("Current Element: " + nNode.nodeName)
                    if(nNode.nodeType == Node.ELEMENT_NODE) {
                        var eElement: Element = nNode as Element;
                        Logger.log("Parsing file ${eElement.getElementsByTagName("name").item(0).textContent}.")
                        var file_name: String = eElement.getElementsByTagName("name").item(0).textContent
                        var file_desc: String = eElement.getElementsByTagName("desc").item(0).textContent
                        DataHandler.insertFile(peerName, peerAddr, file_name, file_desc)
                    }
                    i++
                }


            } catch (e: Exception) {
                Logger.log("--- Unable to Parse ---")
                Logger.log(e.stackTrace.toString())
            }
    }

}