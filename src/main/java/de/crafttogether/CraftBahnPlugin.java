package de.crafttogether;

import de.crafttogether.craftbahn.commands.Commands;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.mysql.MySQLAdapter;
import de.crafttogether.mysql.MySQLConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftBahnPlugin extends JavaPlugin {
    private static CraftBahnPlugin plugin;

    private String serverName;

    private Commands commands;
    private MySQLAdapter mySQLAdapter;
    private DestinationStorage destinationStorage;

    @Override
    public void onEnable() {
        plugin = this;

        /* Check dependencies */
        if (!getServer().getPluginManager().isPluginEnabled("MySQLAdapter")) {
            plugin.getLogger().warning("Couldn't find MySQLAdapter");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("BKCommonLib")) {
            plugin.getLogger().warning("Couldn't find BKCommonLib");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Train_Carts")) {
            plugin.getLogger().warning("Couldn't find TrainCarts");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Create default config
        saveDefaultConfig();

        // Initialize
        FileConfiguration config = getConfig();
        serverName = config.getString("Settings.ServerName");

        // Setup MySQLConfig
        MySQLConfig myCfg = new MySQLConfig();
        myCfg.setHost(config.getString("MySQL.Host"));
        myCfg.setPort(config.getInt("MySQL.Port"));
        myCfg.setUsername(config.getString("MySQL.Username"));
        myCfg.setPassword(config.getString("MySQL.Password"));
        myCfg.setDatabase(config.getString("MySQL.Database"));
        myCfg.setTablePrefix(config.getString("MySQL.TablePrefix"));

        // Validate configuration
        if (!myCfg.checkInputs() || myCfg.getDatabase() == null) {
            getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize MySQLAdapter
        mySQLAdapter = new MySQLAdapter(this, myCfg);

        // Initialize Storages
        destinationStorage = new DestinationStorage();

        // Register Commands
        commands = new Commands();
        commands.enable(this);
    }

    public void onDisable() {
        // Shutdown MySQL-Adapter
        if(mySQLAdapter != null)
            mySQLAdapter.disconnect();
    }

    public MySQLAdapter getMySQLAdapter() { return mySQLAdapter; }
    public DestinationStorage getDestinationStorage() { return destinationStorage; }

    public String getServerName() { return serverName; }
    public static CraftBahnPlugin getInstance() { return plugin; }
}
