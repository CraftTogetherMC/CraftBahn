package de.crafttogether.craftbahn;

import de.crafttogether.craftbahn.destinations.Destination;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.util.MySQLAdapter;
import de.crafttogether.craftbahn.util.MySQLAdapter.MySQLConfig;
import de.crafttogether.craftbahn.util.MySQLAdapter.MySQLConnection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.sql.SQLException;
import java.util.List;

public final class CraftBahn extends JavaPlugin {
    private static CraftBahn plugin;

    private String serverName;
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

        this.serverName = config.getString("Settings.ServerName");
        this.dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("Dynmap");

        // Register Listener
        //getServer().getPluginManager().registerEvents(new TrainListener(), (Plugin)this);

        // Register Commands
        Commands commands = new Commands();
        registerCommand("rbf", (TabExecutor)commands);
        registerCommand("fahrziel", (TabExecutor)commands);
        registerCommand("fahrziele", (TabExecutor)commands);
        registerCommand("fahrzieledit", (TabExecutor)commands);

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
            String query =
                "CREATE TABLE `cb_destinations` (\n" +
                "  `name` varchar(24) NOT NULL,\n" +
                "  `type` varchar(24) NOT NULL,\n" +
                "  `server` varchar(24) NOT NULL,\n" +
                "  `world` varchar(24) NOT NULL,\n" +
                "  `loc_x` double NOT NULL,\n" +
                "  `loc_y` double NOT NULL,\n" +
                "  `loc_z` double NOT NULL,\n" +
                "  `owner` varchar(36) NOT NULL,\n" +
                "  `participants` longtext,\n" +
                "  `public` tinyint(1) NOT NULL,\n" +
                "  `tp_x` double,\n" +
                "  `tp_y` double,\n" +
                "  `tp_z` double\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

            MySQL.execute(query, MySQL.getTablePrefix());
        }

        catch (SQLException ex) {
            getLogger().warning("[MySQL]: " + ex.getMessage());
        }

        DestinationStorage.loadAll((err, destinations) -> {
            Bukkit.getLogger().info("Loaded " + destinations.size() + " destinations");

            for (Destination dest : destinations) {
                Bukkit.getLogger().info(dest.toString());
            }
        });
    }

    private void registerCommand(String cmd, TabExecutor executor) {
        getCommand(cmd).setExecutor((CommandExecutor)executor);
        getCommand(cmd).setTabCompleter((TabCompleter)executor);
    }

    public void broadcast(List<Player> players, String message) {
        for (Player p : players)
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void onDisable() {
        if(MySQLAdapter != null)
            MySQLAdapter.disconnect();
    }

    public MySQLAdapter getMySQLAdapter() {
        return MySQLAdapter;
    }

    public DynmapAPI getDynmap() {
        return dynmap;
    }

    public String getServerName() {
        return serverName;
    }

    public static CraftBahn getInstance() {
        return plugin;
    }
}
