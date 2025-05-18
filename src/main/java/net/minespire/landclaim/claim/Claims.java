package net.minespire.landclaim.claim;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Claims {

    public static List<ProtectedRegion> getRegionsAtLocation(final Location loc) {
        final List<ProtectedRegion> regionList = new ArrayList<>();
        LandClaim.wg.getPlatform().getRegionContainer()
            .get(BukkitAdapter.adapt(loc.getWorld()))
            .getApplicableRegions(BukkitAdapter.asBlockVector(loc))
            .forEach(region -> {
                if (region.getFlag(LandClaim.LandClaimRegionFlag) != null) {
                    if (region.getFlag(LandClaim.LandClaimRegionFlag).equals("region")) {
                        regionList.add(region);
                    }
                }
            });
        return regionList;
    }

    public static ProtectedRegion getRegionByName(final String name, final World world) {
        if (!ProtectedRegion.isValidId(name)) return null;
        final List<ProtectedRegion> regionList = new ArrayList<>();
        final ProtectedRegion region = LandClaim.wg.getPlatform().getRegionContainer()
            .get(world)
            .getRegion(name);
        if (region != null) {
            if (region.getFlag(LandClaim.LandClaimRegionFlag) != null) {
                if (region.getFlag(LandClaim.LandClaimRegionFlag).equals("region")) {
                    return region;
                }
            }
        }
        return null;
    }
}
