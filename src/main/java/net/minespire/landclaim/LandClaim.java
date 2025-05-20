package net.minespire.landclaim;


import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.milkbowl.vault.economy.Economy;
import net.minespire.landclaim.claim.Claim;
import net.minespire.landclaim.claim.VoteFile;
import net.minespire.landclaim.claim.VoteRegion;
import net.minespire.landclaim.command.CommandCompleter;
import net.minespire.landclaim.command.MainCommand;
import net.minespire.landclaim.listener.BypassListener;
import net.minespire.landclaim.listener.GUIClickListener;
import net.minespire.landclaim.listener.PlayerChatListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LandClaim extends JavaPlugin {
    public static LandClaim plugin;
    public static FileConfiguration config;
    public static WorldEdit we;
    public static WorldGuard wg;
    public static StringFlag LandClaimRegionFlag;

    public static Map<String, Claim> claimMap;

    public static Economy econ = null;

    public void onLoad() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            final StringFlag regionFlag = new StringFlag("land-claim-region", "region");
            registry.register(regionFlag);
            LandClaimRegionFlag = regionFlag;
        } catch (final FlagConflictException e) {
            final Flag<?> existing = registry.get("land-claim-region");
            if (existing instanceof StateFlag) {
                LandClaimRegionFlag = (StringFlag) existing;
            }
        }
    }

    public void onEnable() {
        plugin = this;
        this.loadConfiguration();

        // Listeners
        List.of(
            new BypassListener(),
            new GUIClickListener(),
            new PlayerChatListener()
        ).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        this.getCommand("lc").setTabCompleter(new CommandCompleter());
        this.getCommand("lc").setExecutor(new MainCommand());
        we = WorldEdit.getInstance();
        wg = WorldGuard.getInstance();
        VoteFile.load();
        VoteRegion.tallyAllVotes();
        claimMap = new HashMap<>();

        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public void loadConfiguration() {
        final File pluginFolder = this.getDataFolder();
        if (!pluginFolder.exists()) {
            pluginFolder.mkdir();
        }
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        this.reloadConfig();
        config = getConfig();
    }


    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
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