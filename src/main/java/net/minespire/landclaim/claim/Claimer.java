package net.minespire.landclaim.claim;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Claimer {


    public static boolean permToOwnAnotherRegion(final Player player) {
        if (player.hasPermission("landclaim.claimregion")) {
            if (player.hasPermission("landclaim.regions.*")) return true;
            final int numRegionsAllowed = getNumAllowedRegions(player);
            return Claim.getClaimListOwner(player, false).size() < numRegionsAllowed;
        } else return false;

    }

    public static int getNumAllowedRegions(final Player player) {
        final Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
        int permRegions;
        int numRegionsAllowed = 0;
        for (final PermissionAttachmentInfo perm : playerPermissions) {
            if (perm.getPermission().startsWith("landclaim.regions.")) {
                permRegions = Integer.parseInt(perm.getPermission().substring(18));
                numRegionsAllowed = Math.max(permRegions, numRegionsAllowed);
            }
        }
        return numRegionsAllowed;
    }


    public static boolean permToOwnAnotherPlot(final Player player) {
        if (player.hasPermission("landclaim.claimplot")) {
            if (player.hasPermission("landclaim.plots.*")) return true;
            final int numPlotsAllowed = getNumAllowedPlots(player);
            return Claim.getClaimListOwner(player, true).size() < numPlotsAllowed;
        } else return false;
    }

    public static int getNumAllowedPlots(final Player player) {
        final Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
        int permRegions;
        int numRegionsAllowed = 0;
        for (final PermissionAttachmentInfo perm : playerPermissions) {
            if (perm.getPermission().startsWith("landclaim.plots.")) {
                permRegions = Integer.parseInt(perm.getPermission().substring(16));
                numRegionsAllowed = Math.max(permRegions, numRegionsAllowed);
            }
        }
        return numRegionsAllowed;
    }

    public static boolean permissionToClaimInWorld(final Player player) {
        if (player.hasPermission("landclaim.world.*")) return true;
        final Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
        for (final PermissionAttachmentInfo perm : playerPermissions) {
            if (perm.getPermission().toLowerCase().startsWith("landclaim.world.")) {
                final String worldPerm = perm.getPermission().substring(16);
                if (worldPerm.equalsIgnoreCase((player.getWorld().getName()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<World> claimableWorlds(final Player player) {
        final List<World> worlds = Bukkit.getWorlds();
        if (player.hasPermission("landclaim.world.*")) return worlds;
        final List<World> claimableWorlds = new ArrayList<>();
        final Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
        for (final PermissionAttachmentInfo perm : playerPermissions) {
            if (perm.getPermission().toLowerCase().startsWith("landclaim.world.")) {
                final String worldPerm = perm.getPermission().substring(16);
                worlds.forEach(world -> {
                    if (world.getName().equalsIgnoreCase(worldPerm)) claimableWorlds.add(world);
                });
            }
        }
        return claimableWorlds;
    }
}
