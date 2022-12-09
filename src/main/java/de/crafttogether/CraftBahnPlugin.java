package de.crafttogether;

import de.crafttogether.craftbahn.Localization;
import de.crafttogether.craftbahn.commands.Commands;
import de.crafttogether.craftbahn.destinations.DestinationStorage;
import de.crafttogether.craftbahn.listener.TrainEnterListener;
import de.crafttogether.craftbahn.localization.LocalizationManager;
import de.crafttogether.mysql.MySQLAdapter;
import de.crafttogether.mysql.MySQLConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

public final class CraftBahnPlugin extends JavaPlugin {
    public static CraftBahnPlugin plugin;

    private String serverName;
    private DynmapAPI dynmap;

    private Commands commands;
    private MySQLAdapter mySQLAdapter;
    private LocalizationManager localizationManager;
    private DestinationStorage destinationStorage;
    private MiniMessage miniMessageParser;

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

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager();

        // Register Tags/Placeholder for MiniMessage
        miniMessageParser = MiniMessage.builder()
                .editTags(t -> t.resolver(TagResolver.resolver("prefix", Tag.selfClosingInserting(Localization.PREFIX.deserialize()))))
                .editTags(t -> t.resolver(TagResolver.resolver("header", Tag.selfClosingInserting(Localization.HEADER.deserialize()))))
                .editTags(t -> t.resolver(TagResolver.resolver("footer", Tag.selfClosingInserting(Localization.FOOTER.deserialize()))))
                .build();

        // Register Commands
        commands = new Commands();
        commands.enable(this);
    }

    @Override
    public void onDisable() {
        // Shutdown MySQL-Adapter
        if(mySQLAdapter != null)
            mySQLAdapter.disconnect();
    }

    public MySQLAdapter getMySQLAdapter() { return mySQLAdapter; }
    public DynmapAPI getDynmap() { return dynmap; }
    public LocalizationManager getLocalizationManager() { return localizationManager; }
    public DestinationStorage getDestinationStorage() { return destinationStorage; }

    public MiniMessage getMiniMessageParser() {
        if (miniMessageParser == null)
            return MiniMessage.miniMessage();
        else
            return miniMessageParser;
    }

    public String getServerName() { return serverName; }
}
