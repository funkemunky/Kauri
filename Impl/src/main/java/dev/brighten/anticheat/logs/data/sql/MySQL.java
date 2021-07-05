package dev.brighten.anticheat.logs.data.sql;

import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.logs.data.config.MySQLConfig;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public class MySQL {
    private static Connection conn;

    public static void init() {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection("jdbc:mysql://" + MySQLConfig.ip
                                + ":3306/?useSSL=true&autoReconnect=true",
                        MySQLConfig.username,
                        MySQLConfig.password);
                conn.setAutoCommit(true);
                Query.use(conn);
                Query.prepare("CREATE DATABASE IF NOT EXISTS `" + MySQLConfig.database + "`").execute();
                Query.prepare("USE `" + MySQLConfig.database + "`").execute();
                System.out.println("Connection to MySQL has been established.");
            }
        } catch (Exception e) {
            System.out.println("Failed to load mysql: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void initSqlLite() {
        File dataFolder = new File(Kauri.INSTANCE.getDataFolder(), MySQLConfig.database + ".db");
        if (!dataFolder.exists()){
            try {
                if(dataFolder.createNewFile()) {
                    Kauri.INSTANCE.getLogger().info("Successfully created " + MySQLConfig.database + ".db" + " in Kauri folder!");
                }
            } catch (IOException e) {
                Kauri.INSTANCE.getLogger().log(Level.SEVERE, "File write error: "+MySQLConfig.database+".db");
            }
        }
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
        } catch (SQLException ex) {
            Kauri.INSTANCE.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            Kauri.INSTANCE.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
    }

    public static void shutdown() {
        try {
            if(conn != null && !conn.isClosed()) {
                conn.close();
                conn = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
