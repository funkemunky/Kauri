package dev.brighten.anticheat.logs.data.sql;

import dev.brighten.anticheat.logs.data.config.MySQLConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

    public static void use() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
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
