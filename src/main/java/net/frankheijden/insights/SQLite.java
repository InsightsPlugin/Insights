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
    public void load() {
        Bukkit.getLogger().info("[Insights] Setting up database connection...");
        setupConnection();

        try {
            Statement s = connection.createStatement();
            s.executeUpdate(CREATE_TABLE_STATEMENT);
            s.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM " + db + ";");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cached.put(rs.getString("uuid"), rs.getBoolean("realtime_check"));
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

    public boolean hasRealtimeCheckEnabled(Player player) {
        String uuid = player.getUniqueId().toString();
        if (cached.containsKey(uuid)) {
            return cached.get(uuid);
        }
        return true;
    }
}
