package net.minespire.landclaim;


import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class LandClaim extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    private Map<String, Flag<?>> flags = new HashMap();
    private Map<String, FlagCost> flagCosts = new HashMap<String, FlagCost>();
    private Map<String, Conversation> conversations = new HashMap<String, Conversation>();
    public static LandClaim plugin;
    public static WorldEdit we;
    public static WorldGuard wg;
    
    public static Map<String, Claim> claimMap;
    public static Economy econ = null;
    private static boolean enablePlots = false;
    private static boolean autoExpand = false;
    private static boolean useUUID = true;
    private int maxRegionWidth = 150;
    private int minRegionWidth = 10;
    private int maxPlotWidth = 30;
    public ConversationFactory factory;
    //public CreateRegions regionCreator;

    public LandClaim() {
        this.factory = new ConversationFactory((Plugin)this);
        //this.regionCreator = new CreateRegions(this);
    }

    public void onEnable() {
        this.getCommand("land").setExecutor(new MainCommand());
        plugin = this;
        we = WorldEdit.getInstance();
        wg = WorldGuard.getInstance();
        
        
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        /*Flag<?>[] allFlags = DefaultFlag.getFlags();
        int i = 0;
        while (i < allFlags.length) {
            Flag f = allFlags[i];
            this.flags.put(f.getName().toLowerCase(), (Flag)f);
            ++i;
        }*/
        this.loadConfiguration();
        this.getLogger().info("LandClaim Enabled.");
    }

    public static boolean getUseUUID() {
        return useUUID;
    }

    public static boolean getAutoExpand() {
        return autoExpand;
    }

    public static boolean getEnablePlots() {
        return enablePlots;
    }

    public void onDisable() {
        this.getLogger().info("LandClaim Disabled.");
    }

    public void loadConfiguration() {
        ConfigurationSection section1;
        Object expand;
        Object maxPlWidth;
        Object useUUIDOpt;
        Object minRgWidth;
        HashMap<String, Double> defaultPerms;
        Object maxRgWidth;
        File pluginFolder = this.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        if (this.getConfig().get("enable_plots") == null) {
            this.getConfig().addDefault("enable_plots", (Object)false);
            this.saveConfig();
        }
        if (this.getConfig().get("use_uuid") == null) {
            this.getConfig().addDefault("use_uuid", (Object)true);
            this.saveConfig();
        }
        if (this.getConfig().get("region_auto_expand_vert") == null) {
            this.getConfig().addDefault("region_auto_expand_vert", (Object)false);
            this.saveConfig();
        }
        if (this.getConfig().get("max_width_region") == null) {
            this.getConfig().addDefault("max_width_region", (Object)150);
            this.saveConfig();
        }
        if (this.getConfig().get("min_width_region") == null) {
            this.getConfig().addDefault("min_width_region", (Object)10);
            this.saveConfig();
        }
        if (this.getConfig().get("max_width_plot") == null) {
            this.getConfig().addDefault("max_width_plot", (Object)30);
            this.saveConfig();
        }
        if (this.getConfig().get("regions") == null) {
            defaultPerms = new HashMap<String, Double>();
            defaultPerms.put("region", 5000.0);
            defaultPerms.put("plot", 500.0);
            this.getConfig().addDefault("regions", defaultPerms);
            this.saveConfig();
        }
        if (this.getConfig().get("flags") == null) {
            defaultPerms = new HashMap();
            defaultPerms.put("pvp", 1000.0);
            this.getConfig().addDefault("flags", defaultPerms);
            this.saveConfig();
        }
        this.reloadConfig();
        FileConfiguration config = this.getConfig();
        Object enable = config.get("enable_plots");
        if (enable != null) {
            enablePlots = (Boolean)enable;
        }
        if ((expand = config.get("region_auto_expand_vert")) != null) {
            autoExpand = (Boolean)expand;
        }
        if ((useUUIDOpt = config.get("use_uuid")) != null) {
            useUUID = (Boolean)useUUIDOpt;
        }
        if ((maxRgWidth = config.get("max_width_region")) != null) {
            this.maxRegionWidth = (Integer)maxRgWidth;
        }
        if ((minRgWidth = config.get("min_width_region")) != null) {
            this.minRegionWidth = (Integer)minRgWidth;
        }
        if ((maxPlWidth = config.get("max_width_plot")) != null) {
            this.maxPlotWidth = (Integer)maxPlWidth;
        }
        if ((section1 = config.getConfigurationSection("flags")) != null) {
            Map<String, Object> flagsMap = section1.getValues(false);
            Iterator<Entry<String, Object>> it = flagsMap.entrySet().iterator();
            while (it.hasNext()) {
              Map.Entry pairs = (Map.Entry)it.next();
              String rank = pairs.getKey().toString().toLowerCase();
              FlagCost fcst = new FlagCost(rank, ((Double)pairs.getValue()).doubleValue());
              flagCosts.put(rank, fcst);
            }
        } else {
            this.getLogger().info("Failed to read flag costs.");
        }
    }

    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void promptForConfirmation(Player player, double townCost) {
        if (this.conversations.containsKey(player.getName())) {
            this.conversations.get(player.getName()).abandon();
            this.conversations.remove(player.getName());
        }
        player.sendMessage((Object)ChatColor.GOLD + "This region will cost " + (Object)ChatColor.WHITE + "$" + townCost + (Object)ChatColor.GOLD + " to claim");
        //Conversation c = this.factory.withFirstPrompt((Prompt)new FirstPrompt(this, this.regionCreator, player)).withEscapeSequence("exit").withTimeout(15).thatExcludesNonPlayersWithMessage("close prompt").withLocalEcho(false).buildConversation((Conversable)player);
       // this.conversations.put(player.getName(), c);
        //c.begin();
    }
    
    
    public LandClaim getPlugin() {
    	return plugin;
    }
}