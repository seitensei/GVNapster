package com.github.seitensei.dcindex

import java.sql.*;

/**
 * Database Access Class
 */
object DataHandler {

    var conn: Connection = DriverManager.getConnection("jdbc:sqlite:tmp.db")
    // Table: Peers
    // Contains Peer Hostname/IP/Connection Rate

    // Table: Files
    // Contains Filename, IP (FK)

    fun setup() {
        System.out.println("Attempting DB Setup")
        try {
            Class.forName("org.sqlite.JDBC")
            conn = DriverManager.getConnection("jdbc:sqlite:tmp.db")
            conn.autoCommit = false
            System.out.println("Opened connection to tmp.db")
        } catch (e: Exception) {
            throw e
        }
    }


}
