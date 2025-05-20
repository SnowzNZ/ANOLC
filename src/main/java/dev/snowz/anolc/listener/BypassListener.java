package dev.snowz.anolc.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import dev.snowz.anolc.ANOLC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class BypassListener implements Listener {

    private boolean hasBypassPermission(final Player player, final Location loc) {
        if (!player.hasPermission("lc.bypass")) return false;

        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(loc.getWorld()));
        if (regions == null) return false;

        final ApplicableRegionSet regionSet = regions.getApplicableRegions(BukkitAdapter.asBlockVector(loc));
        for (final ProtectedRegion region : regionSet) {
            if (region.getFlag(ANOLC.LAND_CLAIM_REGION_FLAG) != null) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldBypass(final Player player, final Location loc) {
        return hasBypassPermission(player, loc);
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (shouldBypass(player, event.getBlock().getLocation())) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        if (shouldBypass(player, event.getBlock().getLocation())) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (shouldBypass(player, player.getLocation())) {
            event.setCancelled(false);
        }
    }
}
