package de.crafttogether;

import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.commands.Commands;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.listener.CreatureSpawnListener;
import de.crafttogether.craftbahn.listener.PlayerSpawnListener;
import de.crafttogether.craftbahn.listener.SignBreakListener;
import de.crafttogether.craftbahn.listener.TrainEnterListener;
import de.crafttogether.craftbahn.localization.LocalizationManager;
import de.crafttogether.craftbahn.portals.PortalHandler;
import de.crafttogether.craftbahn.portals.PortalStorage;
import de.crafttogether.craftbahn.util.Util;
import de.crafttogether.mysql.MySQLAdapter;
import de.crafttogether.mysql.MySQLConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.util.Objects;

public final class CraftBahnPlugin extends JavaPlugin {
    public static CraftBahnPlugin plugin;

    private String serverName;
    private DynmapAPI dynmap;

    private Commands commands;
    private MySQLAdapter mySQLAdapter;
    private LocalizationManager localizationManager;
    private DestinationStorage destinationStorage;
    private PortalStorage portalStorage;
    private PortalHandler portalHandler;
    private MiniMessage miniMessageParser;

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

        if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
            plugin.getLogger().warning("Dynmap found!");
            dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        }

        // Create default config
        saveDefaultConfig();

        // Export resources
        Util.exportResource("commands.yml");
        if (dynmap != null) {
            Util.exportResource("minecart.png");
            Util.exportResource("rail.png");
        }

        // Register Listener
        getServer().getPluginManager().registerEvents(new TrainEnterListener(), this);
        getServer().getPluginManager().registerEvents(new SignBreakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(), this);

        // Register PluginChannel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Setup MySQLConfig
        MySQLConfig myCfg = new MySQLConfig();
        myCfg.setHost(getConfig().getString("MySQL.Host"));
        myCfg.setPort(getConfig().getInt("MySQL.Port"));
        myCfg.setUsername(getConfig().getString("MySQL.Username"));
        myCfg.setPassword(getConfig().getString("MySQL.Password"));
        myCfg.setDatabase(getConfig().getString("MySQL.Database"));
        myCfg.setTablePrefix(getConfig().getString("MySQL.TablePrefix"));

        // Validate configuration
        if (!myCfg.checkInputs() || myCfg.getDatabase() == null) {
            getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        serverName = getConfig().getString("Settings.ServerName");

        // Initialize MySQLAdapter
        mySQLAdapter = new MySQLAdapter(this, myCfg);

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager();

        // Register Commands
        commands = new Commands();
        commands.enable(this);

        // Initialize Storages
        destinationStorage = new DestinationStorage();
        portalStorage = new PortalStorage();

        // Initialize PortalHandler
        portalHandler = new PortalHandler(getConfig().getString("Portals.Server.BindAddress"), getConfig().getInt("Portals.Server.Port"));
        portalHandler.registerActionSigns();

        // Register Tags/Placeholder for MiniMessage
        miniMessageParser = MiniMessage.builder()
                .editTags(t -> t.resolver(TagResolver.resolver("prefix", Tag.selfClosingInserting(Localization.PREFIX.deserialize()))))
                .editTags(t -> t.resolver(TagResolver.resolver("header", Tag.selfClosingInserting(Localization.HEADER.deserialize()))))
                .editTags(t -> t.resolver(TagResolver.resolver("footer", Tag.selfClosingInserting(Localization.FOOTER.deserialize()))))
                .build();
    }

    @Override
    public void onDisable() {
        // Shutdown MySQL-Adapter
        if(mySQLAdapter != null)
            mySQLAdapter.disconnect();

        // Close TCPServer/TCPClients & Unregister ActionSigns
        if (portalHandler != null)
            portalHandler.shutdown();
    }

    public DynmapAPI getDynmap() { return dynmap; }
    public Commands getCommandManager() {
        return commands;
    }
    public LocalizationManager getLocalizationManager() { return localizationManager; }
    public DestinationStorage getDestinationStorage() { return destinationStorage; }
    public PortalStorage getPortalStorage() {
        return portalStorage;
    }
    public PortalHandler getPortalHandler() {
        return portalHandler;
    }
    public MiniMessage getMiniMessageParser() {
        return Objects.requireNonNullElseGet(miniMessageParser, MiniMessage::miniMessage);
    }
    public String getServerName() { return serverName; }
}
