package dev.brighten.anticheat.logs.data.sql;

import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.logs.data.config.MySQLConfig;
import dev.brighten.anticheat.utils.MiscUtils;
import lombok.SneakyThrows;
import org.h2.jdbc.JdbcConnection;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

public class MySQL {
    private static Connection conn;

    public static void init() {
        try {
            if (conn == null || conn.isClosed()) {
                File mysqlLib = new File(Kauri.INSTANCE.getDataFolder(), "mysqllib.jar");

                if(!mysqlLib.exists()) {
                    Kauri.INSTANCE.getLogger().info("Downloading mysqllib.jar...");
                    MiscUtils.download(mysqlLib, "https://nexus.funkemunky.cc/content/repositories/releases" +
                            "/mysql/mysql-connector-java/8.0.22/mysql-connector-java-8.0.22.jar");
                }
                MiscUtils.injectURL(mysqlLib.toURI().toURL());
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

    @SneakyThrows
    public static void initSqlLite() {
        File h2Driver = new File(Kauri.INSTANCE.getDataFolder(), "h2-driver.jar");

        if(!h2Driver.exists()) {
            h2Driver.createNewFile();
            Kauri.INSTANCE.getLogger().info("Downloading h2-driver.jar...");
            MiscUtils.download(h2Driver, "https://nexus.funkemunky.cc/service/local/repositories/releases/" +
                    "content/com/h2database/h2/1.4.199/h2-1.4.199.jar");
        }
        MiscUtils.injectURL(h2Driver.toURI().toURL());
        File dataFolder = new File(Kauri.INSTANCE.getDataFolder(), MySQLConfig.database + ".db");
        if (!dataFolder.exists()){
            try {//https://nexus.funkemunky.cc/service/local/repositories/releases/content/com/h2database/h2/1.4.199/h2-1.4.199.jar
                if(dataFolder.createNewFile()) {
                    Kauri.INSTANCE.getLogger().info("Successfully created " + MySQLConfig.database + ".db" + " in Kauri folder!");
                }
            } catch (IOException e) {
                Kauri.INSTANCE.getLogger().log(Level.SEVERE, "File write error: "+MySQLConfig.database+".db");
            }
        }
        try {
            Class.forName("org.h2.Driver");
            conn = new NonClosableConnection(new JdbcConnection("jdbc:h2:file:" +
                    dataFolder.getAbsolutePath().replace(".db", ""), new Properties()));
            conn.setAutoCommit(true);
            Query.use(conn);
            System.out.println("Connection to H2 SQlLite has been established.");
        } catch (SQLException ex) {
            Kauri.INSTANCE.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            Kauri.INSTANCE.getLogger().log(Level.SEVERE, "You need the H2 JBDC library. Google it. Put it in /lib folder.");
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
