package de.crafttogether.craftbahn;

import de.crafttogether.MySQLAdapter;
import de.crafttogether.MySQLConfig;
import de.crafttogether.craftbahn.commands.Commands;
import de.crafttogether.craftbahn.commands.ListCommand;
import de.crafttogether.craftbahn.commands.MobEnterCommand;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.listener.TrainEnterListener;
import de.crafttogether.craftbahn.net.Client;
import de.crafttogether.craftbahn.net.Server;
import de.crafttogether.craftbahn.portals.PortalStorage;
import de.crafttogether.craftbahn.util.TCHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.util.Objects;

public final class CraftBahn extends JavaPlugin {
    private static CraftBahn plugin;

    private String serverName;
    private DynmapAPI dynmap;

    private MySQLAdapter MySQLAdapter;
    private PortalStorage portalStorage;
    private DestinationStorage destinationStorage;

    // Socket Server (CB-Portals)
    private Server server;

    @Override
    public void onEnable() {
        plugin = this;

        /* Check dependencies */
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

        if (!getServer().getPluginManager().isPluginEnabled("dynmap")) {
            plugin.getLogger().warning("Couldn't find Dynmap");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Create default config
        saveDefaultConfig();

        // Initialize
        FileConfiguration config = getConfig();
        dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("Dynmap");
        serverName = config.getString("Settings.ServerName");

        // Register Listener
        getServer().getPluginManager().registerEvents(new TrainEnterListener(), this);
        //getServer().getPluginManager().registerEvents(new PlayerSpawnListener(), this);

        // Register PluginChannel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Register Commands
        Commands commands = new Commands();
        registerCommand("rbf", commands);
        registerCommand("fahrziel", commands);
        registerCommand("fahrziele", new ListCommand());
        registerCommand("mobenter", new MobEnterCommand());
        registerCommand("setroute", new ListCommand());
        registerCommand("setdestination", new ListCommand());
        registerCommand("fahrzieledit", commands);
        registerCommand("fze", commands);

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
        MySQLAdapter = new MySQLAdapter(this, myCfg);

        // Initialize Storage-Adapter
        portalStorage = new PortalStorage();
        destinationStorage = new DestinationStorage();

        // Create Server Socket
        server = new Server();
        server.listen(config.getInt("Settings.Port"));

        // Register SignActions (TrainCarts)
        TCHelper.registerActionSigns();
    }

    public void onDisable() {
        // Unregister SignActions (TrainCarts)
        TCHelper.unregisterActionSigns();

        // Close server
        server.close();

        // Close all active clients
        Client.closeAll();

        // Shutdown MySQL-Adapter
        if(MySQLAdapter != null)
            MySQLAdapter.disconnect();
    }

    private void registerCommand(String cmd, TabExecutor executor) {
        Objects.requireNonNull(getCommand(cmd)).setExecutor(executor);
        Objects.requireNonNull(getCommand(cmd)).setTabCompleter(executor);
    }

    public MySQLAdapter getMySQLAdapter() { return MySQLAdapter; }
    public DynmapAPI getDynmap() { return dynmap; }

    public PortalStorage getPortalStorage() { return portalStorage; }
    public DestinationStorage getDestinationStorage() { return destinationStorage; }

    public String getServerName() { return serverName; }
    public static CraftBahn getInstance() { return plugin; }
}
