package de.crafttogether.craftbahn;

import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.util.MySQLAdapter;
import de.crafttogether.craftbahn.util.MySQLAdapter.MySQLConfig;
import de.crafttogether.craftbahn.util.MySQLAdapter.MySQLConnection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.DynmapCommonAPI;

import java.sql.SQLException;

public final class CraftBahn extends JavaPlugin {
    private static CraftBahn plugin;

    private DynmapAPI dynmap;

    private MySQLAdapter MySQLAdapter;
    private MySQLConnection MySQL;

    @Override
    public void onEnable() {
        plugin = this;

        if(getConfig() != null)
            saveDefaultConfig();

        FileConfiguration config = getConfig();

        if (!getServer().getPluginManager().isPluginEnabled("Dynmap")) {
            plugin.getLogger().warning("Couln't find Dynmap");
            Bukkit.getServer().getPluginManager().disablePlugin((Plugin)plugin);
            return;
        }

        this.dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("Dynmap");

        if (getConfig().getBoolean("Settings.Debug"))
            getLogger().info("[MySQL]: Initialize Adapter...");

        // Setup MySQLConfig
        MySQLAdapter.MySQLConfig myCfg = new MySQLConfig();
        myCfg.setHost(config.getString("MySQL.Host"));
        myCfg.setPort(config.getInt("MySQL.Port"));
        myCfg.setUsername(config.getString("MySQL.Username"));
        myCfg.setPassword(config.getString("MySQL.Password"));
        myCfg.setDatabase(config.getString("MySQL.Database"));
        myCfg.setTablePrefix(config.getString("MySQL.TablePrefix"));

        if (!myCfg.checkInputs() || myCfg.getDatabase() == null) {
            getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize MySQLAdapter
        MySQLAdapter = new MySQLAdapter(myCfg);
        MySQL = MySQLAdapter.getConnection();

        if (getConfig().getBoolean("Settings.Debug"))
            getLogger().info("[MySQL]: Create Tables ...");

        // Create Tables
        try {
            String query = "CREATE TABLE IF NOT EXISTS `%sdestinations` (\n" +
                    "  `name` varchar(24) NOT NULL,\n" +
                    "  `server` varchar(24) NOT NULL,\n" +
                    "  `world` varchar(24) NOT NULL,\n" +
                    "  `x` double NOT NULL,\n" +
                    "  `y` double NOT NULL,\n" +
                    "  `z` double NOT NULL,\n" +
                    "  `owner` varchar(36) NOT NULL,\n" +
                    "  `participants` longtext NOT NULL\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

            MySQL.execute(query, MySQL.getTablePrefix());
        }

        catch (SQLException ex) {
            getLogger().warning("[MySQL]: " + ex.getMessage());
        }

        DestinationStorage.loadAll((err, destinations) -> {
            Bukkit.getLogger().info("Loaded " + destinations.size() + " destinations");
        });
    }

    public void onDisable() {
        if(MySQLAdapter != null)
            MySQLAdapter.disconnect();
    }

    public MySQLConnection getMySQL() {
        return MySQL;
    }

    public DynmapAPI getDynmap() {
        return dynmap;
    }

    public static CraftBahn getInstance() {
        return plugin;
    }
}
