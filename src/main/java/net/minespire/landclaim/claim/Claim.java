package net.minespire.landclaim.claim;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.minespire.landclaim.LandClaim;
import net.minespire.landclaim.gui.GUIManager;
import org.bukkit.*;

import java.util.*;

public class Claim {

    private ProtectedRegion region;
    private BlockVector3 minPoint;
    private BlockVector3 maxPoint;
    private final Player player;
    private World world;
    public static List<org.bukkit.World> worlds = Bukkit.getWorlds();
    private RegionManager rgManager;
    private final String rgName;
    private final org.bukkit.entity.Player bukkitPlayer;
    private double claimCost;
    private int claimArea;
    private int claimVolume;
    private boolean isPlot = false;
    private final boolean regionAlreadyExists = false;
    public static Map<String, List<String>> playerClaimsMap = new HashMap<>();
    public static Map<String, ProtectedRegion> awaitingRemovalConfirmation = new HashMap<>();

    public Claim(final org.bukkit.entity.Player player, final String rgName) {
        this.player = BukkitAdapter.adapt(player);
        this.bukkitPlayer = player;
        this.rgName = rgName;
    }

    public Claim(final org.bukkit.entity.Player player, final String rgName, final boolean isPlot) {
        this.player = BukkitAdapter.adapt(player);
        this.bukkitPlayer = player;
        this.rgName = rgName;
        this.isPlot = isPlot;
    }

    public Claim(final org.bukkit.entity.Player player, final String rgName, final String worldName) {
        this.player = BukkitAdapter.adapt(player);
        this.bukkitPlayer = player;
        this.rgName = rgName;
        this.world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
    }

    public static void teleportToClaim(
        final org.bukkit.entity.Player player,
        final String regionName,
        final String worldName
    ) {
        final org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            player.sendMessage(GUIManager.colorize("&6That world does not exist."));
            return;
        }
        final World world = BukkitAdapter.adapt(bukkitWorld);

        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        final ProtectedRegion region = rgManager.getRegion(regionName);
        if (region == null) {
            player.sendMessage(GUIManager.colorize("&6That region does not exist."));
            return;
        }
        if (region.getFlag(Flags.TELE_LOC) != null) {
            player.teleport(new Location(
                bukkitWorld,
                region.getFlag(Flags.TELE_LOC).getX(),
                region.getFlag(Flags.TELE_LOC).getY(),
                region.getFlag(Flags.TELE_LOC).getZ()
            ));
            BukkitAdapter.adapt(player).setLocation(region.getFlag(Flags.TELE_LOC));
            player.sendMessage(ChatColor.GOLD + "Teleported to " + ChatColor.DARK_PURPLE + regionName);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        } else player.sendMessage(ChatColor.GOLD + "There is no teleport set for this claim.");
    }

    public static void setClaimTeleport(
        final org.bukkit.entity.Player player,
        final String regionName,
        final String worldName
    ) {
        if (!player.getWorld().getName().equals(worldName)) {
            player.sendMessage(ChatColor.GOLD + "You must be standing inside the claim to set a teleport.");
            return;
        }
        final World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        final ProtectedRegion region = rgManager.getRegion(regionName);
        final Location loc = player.getLocation();
        if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
            region.setFlag(Flags.TELE_LOC, BukkitAdapter.adapt(loc));
            player.sendMessage(ChatColor.GOLD + "You have successfully set the teleport location for " + ChatColor.DARK_PURPLE + regionName + ChatColor.GOLD + ".");
        } else player.sendMessage(ChatColor.GOLD + "You must be standing inside the claim to set a teleport.");

    }

    public static void removeClaimTeleport(
        final org.bukkit.entity.Player player,
        final String regionName,
        final String worldName
    ) {
        final org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            player.sendMessage("That world does not exist.");
            return;
        }
        final World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        final ProtectedRegion region = rgManager.getRegion(regionName);
        if (region.getFlag(Flags.TELE_LOC) == null) {
            player.sendMessage(ChatColor.GOLD + "There was no teleport set for this claim.");
            return;
        }
        region.setFlag(Flags.TELE_LOC, null);
        player.sendMessage(ChatColor.GOLD + "You removed the teleport location for " + ChatColor.DARK_PURPLE + regionName + ChatColor.GOLD + ".");
        player.closeInventory();
    }

    public boolean createClaim() {
        world = this.player.getWorld();
        rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);

        if (rgManager == null) return false;
        final Map<String, ProtectedRegion> regions = rgManager.getRegions();

        final LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);

        Region selection;
        try {
            selection = session.getSelection(this.player.getWorld());
        } catch (final IncompleteRegionException e) {
            bukkitPlayer.sendMessage(ChatColor.GOLD + "You must select two points first.");
            return false;
        }

        minPoint = selection.getMinimumPoint();
        maxPoint = selection.getMaximumPoint();

        if (selection != null) {
            if (!isPlot) {
                minPoint = BlockVector3.at(minPoint.x(), (player.getWorld().getMaxY() + 1), minPoint.z());
                maxPoint = BlockVector3.at(maxPoint.x(), -64, maxPoint.z());
                region = new ProtectedCuboidRegion(rgName, minPoint, maxPoint);
            } else region = new ProtectedCuboidRegion(rgName, minPoint, maxPoint);
        }

        for (final ProtectedRegion rg : regions.values()) {
            if (rg.getId().equals(region.getId())) {
                bukkitPlayer.sendMessage(ChatColor.GOLD + "A region with that name already exists.");
                return false;
            }
        }

        if (!isPlot) region.setFlag(LandClaim.LandClaimRegionFlag, "region");
        else region.setFlag(LandClaim.LandClaimRegionFlag, "plot");

        calculateClaimArea();
        calculateClaimCost();
        return true;
    }

    public static void queueForRemoval(final String playerName, final String regionName) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getPlayer(playerName).getWorld()));
        if (regions != null) {
            final ProtectedRegion region = regions.getRegion(regionName);
            awaitingRemovalConfirmation.put(playerName, region);
        }
    }

    public void deleteRegion() {
        rgManager.removeRegion(rgName);
    }

    public static Set<UUID> getRegionOwners(final String regionName, final String worldName) {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return null;
        final RegionManager regions = container.get(BukkitAdapter.adapt(bukkitWorld));
        if (regions != null) {
            final ProtectedRegion region = regions.getRegion(regionName);
            final DefaultDomain owners;
            if (region != null) {
                owners = region.getOwners();
                return owners.getUniqueIds();
            }
            return null;
        } else return null;
    }

    public void setNewOwner() {
        final RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        final RegionManager regions = container.get(world);
        if (regions != null) {
            final ProtectedRegion region = regions.getRegion(rgName);
            final DefaultDomain owners = new DefaultDomain();
            owners.addPlayer(player.getUniqueId());
            region.setOwners(owners);
        }
    }

    public void saveClaim() {
        rgManager.addRegion(region);
        final DefaultDomain owner = new DefaultDomain();
        owner.addPlayer(player.getUniqueId());
        region.setOwners(owner);
    }

    public boolean overlapsUnownedRegion() {
        final Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
        for (final ProtectedRegion rg : region.getIntersectingRegions(regionCollection)) {
            if (!rg.getOwners().contains(player.getUniqueId())) return true;

        }
        return false;
    }

    private void calculateClaimCost() {
        if (isPlot) {
            calculateClaimVolume();
            final double tmpCost = LandClaim.plugin.getConfig().getDouble("Claims.Plots.PricePerBlock") * claimVolume;
            final double baseCost = LandClaim.plugin.getConfig().getDouble("Claims.Plots.BaseCost");
            claimCost = Math.max(tmpCost, baseCost);
        } else claimCost = claimArea * LandClaim.plugin.getConfig().getDouble("Claims.Regions.PricePerBlock");
    }

    private void calculateClaimArea() {
        claimArea = (maxPoint.x() - minPoint.x() + 1) * (maxPoint.z() - minPoint.z() + 1);
    }

    private void calculateClaimVolume() {
        claimVolume = (maxPoint.x() - minPoint.x() + 1) * (maxPoint.y() - minPoint.y() + 1) * (maxPoint.z() - minPoint.z() + 1);
    }

    public int getClaimArea() {
        return claimArea;
    }

    public int getClaimVolume() {
        return claimVolume;
    }

    public int getClaimLength() {
        final int side1 = Math.abs(maxPoint.x() - minPoint.x() + 1);
        final int side2 = Math.abs(maxPoint.z() - minPoint.z() + 1);
        return Math.max(side1, side2);
    }

    public int getClaimWidth() {
        final int side1 = Math.abs(maxPoint.x() - minPoint.x() + 1);
        final int side2 = Math.abs(maxPoint.z() - minPoint.z() + 1);
        return Math.min(side1, side2);
    }

    public int getClaimHeight() {
        return Math.abs(maxPoint.y() - minPoint.y() + 1);
    }

    public boolean insideOwnedRegion() {
        final Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
        for (final ProtectedRegion rg : region.getIntersectingRegions(regionCollection)) {
            if (rg.getOwners().contains(player.getUniqueId())) {
                if (rg.contains(minPoint.x(), 30, minPoint.z()) &&
                    rg.contains(minPoint.x(), 30, maxPoint.z()) &&
                    rg.contains(maxPoint.x(), 30, minPoint.z()) &&
                    rg.contains(maxPoint.x(), 30, maxPoint.z())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean regionIsPlot(final org.bukkit.World bukkitWorld, final String regionName) {
        final World world = BukkitAdapter.adapt(bukkitWorld);
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        final Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
        for (final ProtectedRegion rg : regionCollection) {
            if (rg.getId().equals(regionName)) {
                if (rg.getFlag(LandClaim.LandClaimRegionFlag) != null) {
                    if (rg.getFlag(LandClaim.LandClaimRegionFlag).equals("plot")) return true;
                } else return false;

            }
        }
        return false;
    }

    public static String playerIsOwnerOrMember(
        final org.bukkit.entity.Player player,
        final String regionName,
        final String worldName
    ) {
        final org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return null;
        final World world = BukkitAdapter.adapt(bukkitWorld);
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        final ProtectedRegion region = rgManager.getRegion(regionName);
        if (region == null) return null;
        if (region.getOwners().contains(player.getUniqueId())) return "Owner";
        else if (region.getMembers().contains(player.getUniqueId())) return "Member";
        return null;
    }

    public static boolean exists(final String regionName, final String worldName) {
        final org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) return false;
        final World world = BukkitAdapter.adapt(bukkitWorld);
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        return rgManager.getRegion(regionName) != null;
    }

    public static void removeRegion(final org.bukkit.entity.Player player) {
        final World world = BukkitAdapter.adapt(player.getWorld());
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        final String regionName = Claim.awaitingRemovalConfirmation.get(player.getName()).getId();
        rgManager.removeRegion(regionName);
        player.sendMessage(ChatColor.GOLD + "You removed claim " + ChatColor.DARK_PURPLE + regionName);
    }

    public static void removeRegion(
        final org.bukkit.entity.Player player,
        final String regionName,
        final String worldName
    ) {
        final org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld == null) {
            player.sendMessage(ChatColor.GOLD + "That world does not exist.");
            return;
        }
        final World world = BukkitAdapter.adapt(bukkitWorld);
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        rgManager.removeRegion(regionName);
        player.sendMessage(ChatColor.GOLD + "You removed claim " + ChatColor.DARK_PURPLE + regionName);
    }

    public static String parsePlaceholders(String string, final Claim claim) {
        string = string.replace("{RegionCost}", String.format("$%.2f", claim.getClaimCost()));
        string = string.replace("{RegionSize}", String.valueOf(claim.getClaimArea()));
        string = string.replace("{RegionName}", claim.getRegionName());
        string = string.replace("{PlayerName}", claim.getPlayerName());
        string = string.replace("{RegionLength}", String.valueOf(claim.getClaimLength()));
        string = string.replace("{RegionWidth}", String.valueOf(claim.getClaimWidth()));
        string = string.replace("{RegionHeight}", String.valueOf(claim.getClaimHeight()));
        return string;
    }

    public static List<String> getClaimListOwner(final org.bukkit.entity.Player player, final boolean getPlots) {
        final List<String> claimPlotList = new ArrayList<>();
        final List<String> claimRegionList = new ArrayList<>();

        for (final org.bukkit.World bukkitWorld : worlds) {
            final World world = BukkitAdapter.adapt(bukkitWorld);
            final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
            final Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();

            for (final ProtectedRegion rg : regionCollection) {
                if (rg.getOwners().contains(player.getUniqueId())) {
                    if (getPlots) {
                        if (rg.getFlag(LandClaim.LandClaimRegionFlag) != null) {
                            if (rg.getFlag(LandClaim.LandClaimRegionFlag).equals("plot"))
                                claimPlotList.add(rg.getId() + "," + bukkitWorld.getName());
                        }
                    } else {
                        if (rg.getFlag(LandClaim.LandClaimRegionFlag) != null) {
                            if (rg.getFlag(LandClaim.LandClaimRegionFlag).equals("region"))
                                claimRegionList.add(rg.getId() + "," + bukkitWorld.getName());
                        }
                    }
                }
            }
        }
        if (getPlots) return claimPlotList;
        else return claimRegionList;
    }

    public static List<String> getClaimListMember(final org.bukkit.entity.Player player, final boolean getPlots) {
        final List<String> claimPlotList = new ArrayList<>();
        final List<String> claimRegionList = new ArrayList<>();

        for (final org.bukkit.World bukkitWorld : worlds) {
            final World world = BukkitAdapter.adapt(bukkitWorld);
            final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
            final Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();

            for (final ProtectedRegion rg : regionCollection) {
                if (rg.getMembers().contains(player.getUniqueId())) {
                    if (getPlots) {
                        if (rg.getFlag(LandClaim.LandClaimRegionFlag) != null) {
                            if (rg.getFlag(LandClaim.LandClaimRegionFlag).equals("plot"))
                                claimPlotList.add(rg.getId() + "," + bukkitWorld.getName());
                        }
                    } else {
                        if (rg.getFlag(LandClaim.LandClaimRegionFlag) != null) {
                            if (rg.getFlag(LandClaim.LandClaimRegionFlag).equals("region"))
                                claimRegionList.add(rg.getId() + "," + bukkitWorld.getName());
                        }
                    }
                }
            }
        }
        if (getPlots) return claimPlotList;
        else return claimRegionList;
    }

    public double getClaimCost() {
        return claimCost;
    }

    public String getPlayerName() {
        return player.getName();
    }

    public String getRegionName() {
        return rgName;
    }

    public boolean regionExists() {
        return regionAlreadyExists;
    }

    public World getWorld() {
        return world;
    }

    public void setClaimAsPlot() {
        this.isPlot = true;
    }

    public boolean isPlot() {
        return isPlot;
    }

    public static void saveClaimToPlayerMap(final org.bukkit.entity.Player player, final String claim) {
        final List<String> playerClaimList;
        if (!playerClaimsMap.containsKey(player.getUniqueId().toString())) {
            playerClaimList = new ArrayList<>();
            playerClaimList.add(claim);
            playerClaimsMap.put(player.getUniqueId().toString(), playerClaimList);
        } else {
            playerClaimList = playerClaimsMap.get(player.getUniqueId().toString());
            playerClaimList.add(claim);
            playerClaimsMap.put(player.getUniqueId().toString(), playerClaimList);
        }
    }

    public static boolean addOwner(
        final org.bukkit.entity.Player checkIfOwner,
        final String personToAdd,
        final ProtectedRegion region
    ) {
        final DefaultDomain regionOwners = region.getOwners();
        if (checkIfOwner.hasPermission("landclaim.edit.others") || regionOwners.contains(checkIfOwner.getUniqueId())) {
            final org.bukkit.entity.Player playerToAdd = Bukkit.getPlayer(personToAdd);
            if (playerToAdd != null) regionOwners.addPlayer(WorldGuardPlugin.inst().wrapPlayer(playerToAdd));
            else return false;
            region.setOwners(regionOwners);
            return true;
        } else return false;
    }

    public static boolean addMember(
        final org.bukkit.entity.Player checkIfOwner,
        final String personToAdd,
        final ProtectedRegion region
    ) {
        final DefaultDomain regionMembers = region.getMembers();
        if (checkIfOwner.hasPermission("landclaim.edit.others") || region.getOwners().contains(checkIfOwner.getUniqueId())) {
            final org.bukkit.entity.Player playerToAdd = Bukkit.getPlayer(personToAdd);
            if (playerToAdd != null) regionMembers.addPlayer(WorldGuardPlugin.inst().wrapPlayer(playerToAdd));
            else return false;
            region.setMembers(regionMembers);
            return true;
        } else return false;
    }

    public static boolean removeMember(
        final org.bukkit.entity.Player checkIfOwner,
        final String personToRemoveUUID,
        final ProtectedRegion region
    ) {
        final DefaultDomain regionMembers = region.getMembers();
        final UUID uuid = UUID.fromString(personToRemoveUUID);
        if (checkIfOwner.hasPermission("landclaim.edit.others") || region.getOwners().contains(checkIfOwner.getUniqueId())) {
            final OfflinePlayer playerToRemove = Bukkit.getOfflinePlayer(uuid);
            if (playerToRemove != null) regionMembers.removePlayer(WorldGuardPlugin.inst().wrapOfflinePlayer(
                playerToRemove));
            else return false;
            region.setMembers(regionMembers);
            return true;
        } else {
            checkIfOwner.sendMessage(ChatColor.GOLD + "You are not an owner of this claim");
            return false;
        }
    }

    public static boolean removeOwner(
        final org.bukkit.entity.Player checkIfOwner,
        final String personToRemoveUUID,
        final ProtectedRegion region
    ) {
        final DefaultDomain regionOwners = region.getOwners();
        final UUID uuid = UUID.fromString(personToRemoveUUID);
        if (region.getOwners().contains(checkIfOwner.getUniqueId()) || checkIfOwner.hasPermission(
            "landclaim.edit.others")) {
            final OfflinePlayer playerToRemove = Bukkit.getOfflinePlayer(uuid);
            if (playerToRemove != null) regionOwners.removePlayer(WorldGuardPlugin.inst().wrapOfflinePlayer(
                playerToRemove));
            else return false;
            region.setOwners(regionOwners);
            return true;
        } else {
            checkIfOwner.sendMessage(ChatColor.GOLD + "You are not an owner of this claim");
            return false;
        }
    }

    public static ProtectedRegion getRegion(
        final org.bukkit.entity.Player player,
        final String regionName,
        final String worldName
    ) {
        final World world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
        final RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
        return rgManager.getRegion(regionName);
    }

}