package net.minespire.landclaim;


import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class LandClaim extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    public static LandClaim plugin;
    public static WorldEdit we;
    public static WorldGuard wg;
    public static StringFlag LandClaimRegionFlag;
    
    public static Map<String, Claim> claimMap;
    public static Map<String, GUI> guiMap;
    
    public static Economy econ = null;

    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StringFlag regionFlag = new StringFlag("land-claim-region", "region");
            registry.register(regionFlag);
            LandClaimRegionFlag = regionFlag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("land-claim-region");
            if (existing instanceof StateFlag) {
            	LandClaimRegionFlag = (StringFlag) existing;
            }
        }
    }
    public void onEnable() {
        plugin = this;
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        //getServer().getPluginManager().registerEvents(new DeedListener(), this);
        this.getCommand("lc").setTabCompleter(new CommandCompleter());
        this.getCommand("lc").setExecutor(new MainCommand());
        we = WorldEdit.getInstance();
        wg = WorldGuard.getInstance();
        claimMap = new HashMap<>();
        
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.loadConfiguration();
        this.getLogger().info("LandClaim Enabled.");
    }


    public void onDisable() {
        this.getLogger().info("LandClaim Disabled.");
    }

    public void loadConfiguration() {
        File pluginFolder = this.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();
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

    
    public LandClaim getPlugin() {
    	return plugin;
    }
    
}