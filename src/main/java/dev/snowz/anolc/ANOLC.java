package dev.snowz.anolc;


import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import dev.snowz.anolc.claim.Claim;
import dev.snowz.anolc.command.CommandCompleter;
import dev.snowz.anolc.command.MainCommand;
import dev.snowz.anolc.listener.BypassListener;
import dev.snowz.anolc.listener.GUIClickListener;
import dev.snowz.anolc.listener.PlayerChatListener;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ANOLC extends JavaPlugin {

    public static StringFlag LAND_CLAIM_REGION_FLAG;

    @Getter
    private static ANOLC instance;
    @Getter
    private static WorldEdit we;
    @Getter
    private static WorldGuard wg;
    @Getter
    private static final Map<String, Claim> claimMap = new HashMap<>();

    @Override
    public void onLoad() {
        final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            final StringFlag regionFlag = new StringFlag("land-claim-region", "region");
            registry.register(regionFlag);
            LAND_CLAIM_REGION_FLAG = regionFlag;
        } catch (final FlagConflictException e) {
            final Flag<?> existing = registry.get("land-claim-region");
            if (existing instanceof StringFlag) {
                LAND_CLAIM_REGION_FLAG = (StringFlag) existing;
            }
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        // Config
        saveDefaultConfig();

        // Listeners
        List.of(
            new BypassListener(),
            new GUIClickListener(),
            new PlayerChatListener()
        ).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));

        final PluginCommand command = getCommand("lc");
        command.setTabCompleter(new CommandCompleter());
        command.setExecutor(new MainCommand());

        we = WorldEdit.getInstance();
        wg = WorldGuard.getInstance();
    }
}