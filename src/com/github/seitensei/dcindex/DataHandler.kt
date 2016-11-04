package com.github.seitensei.dcindex

import java.sql.*

/**
 * Database Access Class
 */
object DataHandler {
    var conn: Connection? = null
    var stmt: Statement? = null

    init {
        setup()
    }

    fun setup() {
        System.out.println("Attempting DB Setup")

        // verify driver
        try {
            Class.forName("org.sqlite.JDBC")
        } catch (e: Exception) {
            System.out.println("SQLite driver is not installed.")
            System.err.println(e)
        }

        // create connection
        conn = DriverManager.getConnection("jdbc:sqlite:tmp.db")
        conn?.autoCommit = false
        try {
            stmt = conn?.createStatement()
        } catch (e: Exception) {
            try {
                conn?.close()
            } catch (i: Exception) {
                conn = null
            }
        }
        stmt?.close()
    }

    fun closeConnection() {
        if(stmt != null) {
            try {
                stmt?.close()
            } catch (i: Exception) {}
        }
        if(conn != null) {
            try {
                conn?.close()
            } catch (i: Exception) {}
        }
    }

    fun tableExists(table: String): Boolean {
        try {
            stmt?.executeQuery("SELECT 1 FROM $table LIMIT 1")
        } catch (e: Exception) {
            System.out.println("Table ${table} does not exist.")
            return false
        }
        return true
    }

    fun initTables() {
        // Drop the Tables when we Initialize them
        if(tableExists("files")) {
            try {
                stmt?.executeUpdate("DROP TABLE IF EXISTS files")
                System.out.println("Dropping Files")
                // Dropped
            } catch (e: Exception) {
                System.out.println("Unable to initialize file table")
                System.err.println(e)
            }
        }
        if(tableExists("peers")) {
            try {
                stmt?.executeUpdate("DROP TABLE IF EXISTS peers")
                System.out.println("Dropping Peers")
                // Dropped
            } catch (e: Exception) {
                System.out.println("Unable to initialize peer table")
                System.err.println(e)
                throw e
            }
        }
        stmt?.executeUpdate("CREATE TABLE peers ( peer_id INTEGER PRIMARY KEY, peer_name varchar(255) NOT NULL, peer_ip varchar(255) NOT NULL, peer_conn varchar(255) );")
        stmt?.executeUpdate("CREATE TABLE files ( file_id INTEGER PRIMARY KEY, peer_id int NOT NULL,  file_name varchar(255) NOT NULL, file_desc varchar(255) NOT NULL );")
        conn?.commit()
    }

    fun peerExists(peer_name: String, peer_ip: String): Boolean {
        var sql_query: String = "SELECT peer_id, peer_name, peer_ip FROM peers WHERE peer_name='${peer_name}' AND peer_ip='${peer_ip}';"
        var rs: ResultSet? = stmt?.executeQuery(sql_query)
        try {
            rs?.getInt("peer_id")
        } catch (e: Exception) {
            System.out.println(e)
            System.out.println("${peer_name} does not exist")
            return false
        }
        System.out.println("Peer Exists")
        return true
    }

    fun addPeer(peer_name: String, peer_ip: String, peer_conn: String): Boolean {
        if(!peerExists(peer_name, peer_ip)) {
            var sql_stmt = "INSERT INTO peers (peer_name, peer_ip, peer_conn) VALUES ('${peer_name}','${peer_ip}','${peer_conn}');"
            System.out.println("adding ${peer_name}")
            stmt?.executeUpdate(sql_stmt)
            conn?.commit()
            return true
        } else {
            System.out.println("Peer already exists")
            return false
        }
    }

    fun delPeer(peer_name: String, peer_ip: String): Boolean {
        if(peerExists(peer_name, peer_ip)) {
            System.out.println("Peer ${peer_name} exists, deleting.")
            var sql_stmt = "DELETE FROM peers WHERE peer_name = '${peer_name}' AND peer_ip = '${peer_ip}';"
            stmt?.execute(sql_stmt)
            conn?.commit()
            return true
        } else {
            System.out.println("no ${peer_name}")
            return false
        }
    }

    fun fileExists(peer_id: Int, file_name: String): Boolean {
        var sql_query: String = "SELECT file_id, peer_id, file_name FROM files WHERE peer_id='${peer_id}' AND file_name='${file_name}';"
        var rs: ResultSet? = stmt?.executeQuery(sql_query)
        try {
            rs?.getInt("file_id")
        } catch (e: Exception) {

            return false
        }
        return true
    }

    fun addFile(peer_id: Int, file_name: String, file_desc: String): Boolean{
        if(!fileExists(peer_id, file_name)) {
            // file does not exist
            var sql_stmt = "INSERT INTO files (peer_id, file_name, file_desc) VALUES (${peer_id},'${file_name}','${file_desc}');"
            stmt?.executeUpdate(sql_stmt)
            conn?.commit()
            return true
        } else {
            return false
        }
    }

    fun delFile(peer_id: Int, file_name: String): Boolean {
        if(fileExists(peer_id, file_name)) {
            System.out.println("File ${file_name} exists, deleting.")
            var sql_stmt = "DELETE FROM files WHERE peer_id = ${peer_id} AND file_name = '${file_name}';"
            stmt?.execute(sql_stmt)
            conn?.commit()
            return true
        } else {
            return false
        }
    }



}
