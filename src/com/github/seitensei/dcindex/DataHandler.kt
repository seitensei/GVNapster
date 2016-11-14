package com.github.seitensei.dcindex

import java.sql.*
import java.util.*

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
        Logger.log("Attempting to establish connection to database.")
        System.out.println("Attempting DB Setup")

        // verify driver
        try {
            Class.forName("org.sqlite.JDBC")
        } catch (e: Exception) {
            Logger.log("No SQLite Driver present.")
            System.err.println(e)
            Logger.log("[ERROR] ${e}")
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
        Logger.log("Database connection established.")
    }

    fun closeConnection() {
        Logger.log("Attempting closure of database connection.")
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
        Logger.log("Checking if $table exists in the database.")
        try {
            stmt?.executeQuery("SELECT 1 FROM $table LIMIT 1")
        } catch (e: Exception) {
            System.out.println("Table ${table} does not exist.")
            return false
        }
        Logger.log("$table is found to exist.")
        return true
    }

    fun initTables() {
        // Drop the Tables when we Initialize them
        Logger.log("Initializing database tables.")
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
        Logger.log("Database initialization commited to disk.")
    }

    fun peerExists(peer_name: String, peer_ip: String): Boolean {
        Logger.log("Checking for the existence of $peer_name on $peer_ip")
        var sql_query: String = "SELECT peer_id, peer_name, peer_ip FROM peers WHERE peer_name='${peer_name}' AND peer_ip='${peer_ip}';"
        var rs: ResultSet? = stmt?.executeQuery(sql_query)
        try {
            rs?.getInt("peer_id")
        } catch (e: Exception) {
            System.out.println(e)
            Logger.log("$peer_name on $peer_ip was not located in the database.")
            return false
        }
        Logger.log("$peer_name on $peer_ip was found in the database.")
        return true
    }

    fun addPeer(peer_name: String, peer_ip: String, peer_conn: String): Boolean {
        if(!peerExists(peer_name, peer_ip)) {
            Logger.log("Peer does not exist, adding.")
            var sql_stmt = "INSERT INTO peers (peer_name, peer_ip, peer_conn) VALUES ('${peer_name}','${peer_ip}','${peer_conn}');"
            System.out.println("adding ${peer_name}")
            stmt?.executeUpdate(sql_stmt)
            conn?.commit()
            return true
        } else {
            Logger.log("Peer Exists, Updating.")
            var sql_stmt = "UPDATE peers SET peer_conn= '$peer_conn' WHERE peer_name='$peer_name' AND peer_ip='$peer_ip';"
            stmt?.executeUpdate(sql_stmt)
            conn?.commit()
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

    fun insertFile(peer_name: String, peer_ip: String, file_name: String, file_desc: String): Boolean{
        var peer_stmt = "SELECT peer_id FROM peers WHERE peer_name = '$peer_name' AND peer_ip = '$peer_ip';"
        var peer_set: ResultSet? = stmt?.executeQuery(peer_stmt)
        var peer_id: Int? = 0;
        try {
            peer_id = peer_set?.getInt("peer_id")
            addFile(peer_id as Int, file_name, file_desc)
            return true
        } catch (e: Exception) {
            Logger.log("Unable to add file belonging to $peer_name@$peer_ip.")
            return false
        }

    }

    fun dbDump(): ArrayList<ResultEntity> {
        try {
            var peer_stmt = "SELECT peers.peer_name, peers.peer_ip, peers.peer_conn, files.file_name, " +
                    "files.file_desc FROM peers INNER JOIN files ON peers.peer_id=files.peer_id;"
            var results: ResultSet? = stmt?.executeQuery(peer_stmt)
            var list: ArrayList<ResultEntity> = ArrayList()
            while(results?.next() as Boolean) {
                list.add(ResultEntity(results?.getString(1) as String, results?.getString(2) as String,
                        results?.getString(3) as String, results?.getString(4) as String, results?.getString(5) as String))
            }
            return list
        } catch (e: Exception) {
            Logger.log("Unable to get data list.")
            Logger.log(e.stackTrace.toString())
        }
        return ArrayList<ResultEntity>()
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

    fun dropFiles(peer_name: String, peer_ip: String): Boolean {
        var peer_stmt = "SELECT peer_id FROM peers WHERE peer_name = '$peer_name' AND peer_ip = '$peer_ip';"
        var file_stmt = "";
        var peer_set: ResultSet? = stmt?.executeQuery(peer_stmt)
        var peer_id: Int? = 0;
        try {
            peer_id = peer_set?.getInt("peer_id")
            file_stmt = "DELETE FROM files WHERE peer_id = $peer_id;"
            stmt?.execute(file_stmt)
            return true
        } catch (e: Exception) {
            Logger.log("Unable to drop files belonging to $peer_name@$peer_ip.")
            return false
        }

    }





}
