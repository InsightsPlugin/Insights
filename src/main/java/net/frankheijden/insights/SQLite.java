package net.frankheijden.insights;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public class SQLite {
    private Insights plugin;

    public String db = "players";
    public Connection connection;

    public SQLite(Insights plugin) {
        this.plugin = plugin;
    }

    public String CREATE_TABLE_STATEMENT = "CREATE TABLE IF NOT EXISTS players (`uuid` varchar(128) NOT NULL, `realtime_check` bit NOT NULL, PRIMARY KEY (`uuid`));";
    public String CREATE_TABLE_STATEMENT_AUTOSCAN = "CREATE TABLE IF NOT EXISTS players_autoscan (`uuid` varchar(128) NOT NULL, `autoscan` varchar(128) NOT NULL, PRIMARY KEY (`uuid`));";

    public Connection setupConnection() {
        File dbFile = new File(plugin.getDataFolder(), db+".db");
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

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
    public void load() {
        Bukkit.getLogger().info("[Insights] Setting up database connection...");
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
            ps = connection.prepareStatement("SELECT * FROM " + db + ";");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cached.put(rs.getString("uuid"), rs.getBoolean("realtime_check"));
            }

            ps2 = connection.prepareStatement("SELECT * FROM players_autoscan;");
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                cached_autoscan.put(rs2.getString("uuid"), rs2.getString("autoscan"));
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
            ps = connection.prepareStatement("REPLACE INTO " + db + " (uuid,realtime_check) VALUES(?,?)");
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

    public void setAutoScan(UUID uuid, String value) {
        String uuidString = uuid.toString();

        PreparedStatement ps = null;
        try {
            connection = setupConnection();
            ps = connection.prepareStatement("REPLACE INTO players_autoscan (uuid,autoscan) VALUES(?,?)");
            ps.setString(1, uuidString);
            ps.setString(2, value);
            ps.executeUpdate();

            cached_autoscan.put(uuidString, value);
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
}
