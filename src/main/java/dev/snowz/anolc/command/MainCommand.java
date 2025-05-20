package dev.snowz.anolc.command;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.snowz.anolc.ANOLC;
import dev.snowz.anolc.claim.Claim;
import dev.snowz.anolc.claim.Claimer;
import dev.snowz.anolc.claim.Visualizer;
import dev.snowz.anolc.gui.GUIManager;
import dev.snowz.anolc.gui.NGUI;
import dev.snowz.anolc.prompt.Prompt;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public final class MainCommand implements CommandExecutor {

    private Player player = null;
    private boolean isPlayer = false;
    private final GUIManager guiManager = GUIManager.getInst();

    @Override
    public boolean onCommand(
        final @NotNull CommandSender sender,
        final @NotNull Command cmd,
        final @NotNull String label,
        final String @NotNull [] args
    ) {
        if (sender instanceof Player) {
            player = (Player) sender;
            isPlayer = true;
        }

        if (args.length < 1) {
            if (isPlayer) GUIManager.getInst().openMainGUI(player);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "gui":
                    if (isPlayer) GUIManager.getInst().openMainGUI(player);
                    break;
                case "claim":
                    if (player != null) {
                        if (args[1] == null) return false;
                        if (!Claimer.permToOwnAnotherRegion(player) && !player.isOp()) {
                            player.sendMessage(ChatColor.RED + "You don't have permission to claim or have claimed too many regions.");
                            return true;
                        }
                        if (!Claimer.permissionToClaimInWorld(player) && !player.isOp()) {
                            player.sendMessage(ChatColor.RED + "You don't have permission to claim in this world.");
                            return true;
                        }
                        if (!ProtectedRegion.isValidId(args[1])) {
                            player.sendMessage(ChatColor.GOLD + "That is not a valid region name.");
                            return true;
                        }

                        final Claim claim = new Claim(player, args[1]);
                        if (!claim.createClaim()) return true;

                        if (claim.overlapsUnownedRegion()) {
                            player.sendMessage(ChatColor.GOLD + "Your selection overlaps a region belonging to someone else!");
                            return true;
                        }
                        if (!meetsClaimLimits(claim)) return true;
                        ANOLC.getClaimMap().put(player.getUniqueId().toString(), claim);

                        final NGUI claimGUI = new NGUI(36, "LandClaim Claim Region");
                        claimGUI.addItem(
                            Material.GRASS_BLOCK,
                            ChatColor.translateAlternateColorCodes(
                                '&',
                                "&3Are You Sure You Want To Claim This Region?"
                            ),
                            guiManager.parseLoreString(Claim.parsePlaceholders(
                                "&5Region Name: &f{RegionName}|&5Region Cost: &f{RegionCost}|&5Region Size: &f{RegionLength} x {RegionHeight} x {RegionWidth}|&aClick to confirm purchase.",
                                claim
                            )),
                            13
                        );
                        claimGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
                        claimGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
                        claimGUI.open(player);

                    } else sender.sendMessage("You must be a player to use that command!");
                    break;
                case "claimplot":
                    if (player != null) {
                        if (args[1] == null) return false;
                        if (!Claimer.permToOwnAnotherPlot(player) && !player.isOp()) {
                            player.sendMessage(ChatColor.RED + "You don't have permission to claim or have claimed too many plots.");
                            return true;
                        }
                        if (!Claimer.permissionToClaimInWorld(player) && !player.isOp()) {
                            player.sendMessage(ChatColor.RED + "You don't have permission to claim in this world.");
                            return true;
                        }

                        if (!ProtectedRegion.isValidId(args[1])) {
                            player.sendMessage(ChatColor.GOLD + "That is not a valid plot name.");
                            return true;
                        }

                        final Claim claim = new Claim(player, args[1], true);
                        if (!claim.createClaim()) return true;

                        if (!claim.insideOwnedRegion()) {
                            player.sendMessage(ChatColor.GOLD + "You may only claim a plot inside a region that you own.");
                            return true;
                        }
                        if (claim.overlapsUnownedRegion()) {
                            player.sendMessage(ChatColor.GOLD + "Your selection overlaps a region belonging to someone else.");
                            return true;
                        }
                        ANOLC.getClaimMap().put(player.getUniqueId().toString(), claim);


                        final NGUI claimGUI = new NGUI(36, "LandClaim Claim Plot");
                        claimGUI.addItem(
                            Material.DIRT,
                            ChatColor.translateAlternateColorCodes('&', "&3Are You Sure You Want To Claim This Plot?"),
                            guiManager.parseLoreString(Claim.parsePlaceholders(
                                "&5Region Name: &f{RegionName}|&5Region Cost: &f{RegionCost}|&5Region Size: &f{RegionLength} x {RegionHeight} x {RegionWidth}|&aClick to confirm purchase.",
                                claim
                            )),
                            13
                        );
                        claimGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
                        claimGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
                        claimGUI.open(player);

                    } else sender.sendMessage("You must be a player to use that command!");
                    break;
                case "reload":
                    if ((sender instanceof Player) && !player.hasPermission("landclaim.reload") && !player.isOp()) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                        return true;
                    }
                    ANOLC.getInstance().reloadConfig();
                    ANOLC.getInstance().getCommand("lc").setTabCompleter(new CommandCompleter());
                    ANOLC.getInstance().getCommand("lc").setExecutor(new MainCommand());
                    sender.sendMessage(ChatColor.GOLD + "LandClaim config reloaded!");
                    break;
                case "cancel":
                    if (Prompt.hasActivePrompt(player)) {
                        Prompt.getPrompt(player.getName()).cancelPrompt();
                    }
                    break;
                case "nearby":
                    if (!(sender instanceof Player)) sender.sendMessage("You must be a player to use that command.");
                    if ((sender instanceof Player) && !player.hasPermission("landclaim.nearby")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                        return true;
                    }

                    final String playerName = player.getName();
                    final Player nPlayer = Bukkit.getPlayer(player.getName());
                    if (Visualizer.seeNearbyBukkitTask.containsKey(playerName)) {
                        if (Visualizer.seeNearbyAsyncService.containsKey(playerName)) {
                            Bukkit.getScheduler().cancelTask(Visualizer.seeNearbyBukkitTask.get(playerName));
                            Visualizer.seeNearbyAsyncService.get(playerName).shutdown();
                            Visualizer.seeNearbyBukkitTask.remove(playerName);
                            Visualizer.seeNearbyAsyncService.remove(playerName);
                            Visualizer.timer.remove(playerName);
                            nPlayer.sendMessage(ChatColor.GOLD + "Disabled nearby region viewing.");
                            return true;
                        }
                    }
                    final AtomicInteger timer = new AtomicInteger(0);
                    Visualizer.timer.put(playerName, timer);
                    nPlayer.sendMessage(ChatColor.GOLD + "Enabled nearby region viewing.");

                    try {
                        Visualizer.seeNearbyAsyncService.put(playerName, Executors.newSingleThreadScheduledExecutor());
                        final Runnable task = () -> {
                            Visualizer.timer.get(playerName).getAndAdd(2);
                            Visualizer.seeNearbyRegions(nPlayer);
                            if (Visualizer.timer.get(playerName).get() >= 14) {
                                Visualizer.seeNearbyAsyncService.get(playerName).shutdown();
                                Visualizer.seeNearbyAsyncService.remove(playerName);
                            }
                        };
                        Visualizer.seeNearbyAsyncService.get(playerName).scheduleAtFixedRate(
                            task,
                            0L,
                            2L,
                            TimeUnit.SECONDS
                        );
                    } catch (final Throwable t) {
                        ANOLC.getInstance().getLogger().warning("Error while trying to run async task: " + t.getMessage());
                    }

                    final BukkitRunnable bukkitTask = new BukkitRunnable() {
                        Queue<BlockVector3> particleLocations = Visualizer.playerParticleCoords.get(playerName);
                        final int maxSeconds = 15;
                        int ticks = 0;

                        @Override
                        public void run() {
                            ticks++;
                            if (particleLocations != null) {
                                final int loops = Math.min(particleLocations.size(), 400);
                                for (int i = 0; i < loops; i++) {
                                    if (particleLocations == null) break;
                                    final BlockVector3 loc = particleLocations.remove();
                                    final Location location = Visualizer.getBestSpawnLocation(
                                        nPlayer.getWorld(),
                                        loc.x(),
                                        loc.y(),
                                        loc.z()
                                    );
                                    if (location != null) nPlayer.spawnParticle(
                                        Particle.BLOCK_MARKER,
                                        location.getX() + .5,
                                        location.getY() + .5,
                                        location.getZ() + .5,
                                        1,
                                        Bukkit.createBlockData(Material.valueOf(ANOLC.getInstance().getConfig().getString(
                                            "Claims.Regions.NearbyDisplayBlock")))
                                    );
                                }
                            } else {
                                particleLocations = Visualizer.playerParticleCoords.get(playerName);
                            }
                            if (ticks / 20 == maxSeconds) {
                                this.cancel();
                                Visualizer.seeNearbyBukkitTask.remove(playerName);
                                nPlayer.sendMessage(ChatColor.GOLD + "Disabled nearby region viewing.");
                            }
                        }
                    };
                    bukkitTask.runTaskTimer(ANOLC.getInstance(), 0, 1);
                    final Integer taskID = bukkitTask.getTaskId();
                    Visualizer.seeNearbyBukkitTask.put(player.getName(), taskID);
                    break;
                case "teleport":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("You must be a player to do that.");
                        return true;
                    }
                    if (!player.hasPermission("landclaim.teleport")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.GOLD + "Teleport to a claim with /lc teleport [region],[world]");
                        return true;
                    }
                    Claim.teleportToClaim(player, args[1]);
                    return true;
                case "delete":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("You must be a player to do that.");
                        return true;
                    }
                    if (!player.hasPermission("landclaim.delete.own")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.GOLD + "You must specify the claim you want to remove.");
                        return true;
                    }
                    final Set<UUID> regionOwners = Claim.getRegionOwners(player, args[1]);
                    if (regionOwners != null) {
                        if (regionOwners.contains(player.getUniqueId()) || player.hasPermission(
                            "landclaim.delete.others")) {
                            guiManager.promptForRemoval(player.getName(), args[1]);
                            return true;
                        } else player.sendMessage(ChatColor.RED + "You don't own that region.");
                    } else player.sendMessage(ChatColor.GOLD + "Invalid Region");
                    break;
                case "list":
                    if (!player.hasPermission("landclaim.list")) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
                        return true;
                    }
                    guiManager.openAllClaimsGUI(player);
                    break;
                case "inspect":
                    if (!(sender instanceof Player)) {
                        sender.sendMessage("You must be a player to do that.");
                        return true;
                    }
                    if (!player.hasPermission("landclaim.inspect.own") && !player.isOp()) {
                        sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                        return true;
                    }
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.GOLD + "You must specify the claim you want to inspect.");
                        return true;
                    }
                    final String regionName = args[1];
                    if (!Claim.exists(player, regionName)) {
                        player.sendMessage(ChatColor.GOLD + "That claim does not exist");
                        return true;
                    }
                    final String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName);
                    if ((ownerOrMember != null && player.hasPermission("landclaim.inspect.own")) || player.hasPermission(
                        "landclaim.inspect.others")) {
                        guiManager.openClaimInspector(player, regionName);
                        return true;
                    } else player.sendMessage(ChatColor.GOLD + "You cannot inspect that region.");
                    break;
                default:
                    return false;
            }
        } catch (final IndexOutOfBoundsException e) {
            return false;
        }

        return true;
    }

    public boolean meetsClaimLimits(final Claim claim) {
        if (claim.isPlot()) {
            return true;
        } else {
            final int area = claim.getClaimArea();
            final int length = claim.getClaimLength();
            final int width = claim.getClaimWidth();
            if (area < ANOLC.getInstance().getConfig().getInt("Claims.Regions.MinSize")) {
                player.sendMessage(ChatColor.GOLD + "Your selection is too small!");
                return false;
            } else if (area > ANOLC.getInstance().getConfig().getInt("Claims.Regions.MaxSize")) {
                player.sendMessage(ChatColor.GOLD + "Your selection is too large!");
                return false;
            } else if (((double) length / width) > ANOLC.getInstance().getConfig().getDouble("Claims.Regions.MaxLWRatio") || ((double) width / length) > ANOLC.getInstance().getConfig().getDouble(
                "Claims.Regions.MaxLWRatio")) {
                player.sendMessage(ChatColor.GOLD + "Your selection doesn't meet proportion requirements!");
                return false;
            } else return true;
        }
    }
}