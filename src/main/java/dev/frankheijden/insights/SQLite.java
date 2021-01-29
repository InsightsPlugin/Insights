package dev.frankheijden.insights;

import dev.frankheijden.insights.utils.FileUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

public class SQLite {

    public static final String DATABASE_NAME = "players";
    public Connection connection;

    public static final String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS players (`uuid` varchar(128) NOT NULL, `realtime_check` bit NOT NULL, PRIMARY KEY (`uuid`));";
    public static final String CREATE_TABLE_STATEMENT_AUTOSCAN = "CREATE TABLE IF NOT EXISTS players_autoscan (`uuid` varchar(128) NOT NULL, `type` integer NOT NULL, `autoscan` varchar(512) NOT NULL, PRIMARY KEY (`uuid`));";

    public Connection setupConnection() {
        File dbFile = FileUtils.createFileIfNotExists(DATABASE_NAME + ".db");

        try {
            if (connection != null && !connection.isClosed()){
                return connection;
            } else {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            }
            return connection;
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private HashMap<String, Boolean> cached = new HashMap<>();
    private HashMap<String, String> cached_autoscan = new HashMap<>();
    private HashMap<String, Integer> cached_autoscan_types = new HashMap<>();
    public void load() {
        setupConnection();

        try {
            Statement s = connection.createStatement();
            s.executeUpdate(CREATE_TABLE_STATEMENT);
            s.executeUpdate(CREATE_TABLE_STATEMENT_AUTOSCAN);
            s.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM " + DATABASE_NAME + ";");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cached.put(rs.getString("uuid"), rs.getBoolean("realtime_check"));
            }

            ps2 = connection.prepareStatement("SELECT * FROM players_autoscan;");
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                cached_autoscan.put(rs2.getString("uuid"), rs2.getString("autoscan"));
                cached_autoscan_types.put(rs2.getString("uuid"), rs2.getInt("type"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setRealtimeCheck(UUID uuid, boolean value) {
        String uuidString = uuid.toString();

        PreparedStatement ps = null;
        try {
            connection = setupConnection();
            ps = connection.prepareStatement("REPLACE INTO " + DATABASE_NAME + " (uuid,realtime_check) VALUES(?,?)");
            ps.setString(1, uuidString);
            ps.setBoolean(2, value);
            ps.executeUpdate();

            cached.put(uuidString, value);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void toggleRealtimeCheck(UUID uuid) {
        boolean value = false;

        String uuidString = uuid.toString();
        if (cached.containsKey(uuidString)) {
            value = !cached.get(uuidString);
        }

        setRealtimeCheck(uuid, value);
    }

    public void setAutoScan(UUID uuid, int type, String value) {
        String uuidString = uuid.toString();

        PreparedStatement ps = null;
        try {
            connection = setupConnection();
            ps = connection.prepareStatement("REPLACE INTO players_autoscan (uuid,type,autoscan) VALUES(?,?,?)");
            ps.setString(1, uuidString);
            ps.setInt(2, type);
            ps.setString(3, value);
            ps.executeUpdate();

            cached_autoscan.put(uuidString, value);
            cached_autoscan_types.put(uuidString, type);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void disableAutoScan(UUID uuid) {
        String uuidString = uuid.toString();

        PreparedStatement ps = null;
        try {
            connection = setupConnection();
            ps = connection.prepareStatement("DELETE FROM `players_autoscan` WHERE uuid='" + uuid.toString() + "';");
            ps.executeUpdate();

            cached_autoscan.remove(uuidString);
            cached_autoscan_types.remove(uuidString);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean hasRealtimeCheckEnabled(Player player) {
        String uuid = player.getUniqueId().toString();
        if (cached.containsKey(uuid)) {
            return cached.get(uuid);
        }
        return true;
    }

    public String getAutoscan(Player player) {
        String uuid = player.getUniqueId().toString();
        return cached_autoscan.get(uuid);
    }

    public Integer getAutoscanType(Player player) {
        String uuid = player.getUniqueId().toString();
        return cached_autoscan_types.get(uuid);
    }
}
