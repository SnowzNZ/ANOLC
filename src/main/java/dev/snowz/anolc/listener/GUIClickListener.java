package dev.snowz.anolc.listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.snowz.anolc.ANOLC;
import dev.snowz.anolc.claim.Claim;
import dev.snowz.anolc.gui.GUIManager;
import dev.snowz.anolc.prompt.Prompt;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public final class GUIClickListener implements Listener {

    public static Map<String, InventoryView> playerLCInventory = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(final InventoryClickEvent clickEvent) {
        final String inventoryTitle = clickEvent.getView().getTitle();
        final ItemStack itemStack = clickEvent.getCurrentItem();
        final ItemMeta itemMeta;
        final String itemName;
        if (itemStack == null) return;
        else itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return;
        else itemName = ChatColor.stripColor(itemMeta.getDisplayName());

        //if(!GUIManager.getInst().isLandClaimGui(inventoryTitle)) return;

        final GUIManager guiManager = GUIManager.getInst();
        final Player player;
        if (clickEvent.getWhoClicked() instanceof Player) player = ((Player) clickEvent.getWhoClicked()).getPlayer();
        else return;
        if (!isLandClaimGui(player)) return;
        clickEvent.setCancelled(true);


        if (inventoryTitle.equals("LandClaim Main Menu")) {
            if (itemName.equals("Claims")) guiManager.openAllClaimsGUI(player);
            if (itemName.equals("Claim Limits")) guiManager.openClaimLimitsGUI(player);
            if (itemName.equals("Wand")) guiManager.handleWandClick(player);
            if (itemName.equals("Close")) player.closeInventory();
            return;
        }

        if (inventoryTitle.startsWith("LandClaim Claims")) {
            if (player.hasPermission("landclaim.inspect.own")) {
                final Material mat = itemStack.getType();
                if (mat.equals(Material.DIAMOND_BLOCK)
                    || mat.equals(Material.DIAMOND_ORE)
                    || mat.equals(Material.IRON_BLOCK)
                    || mat.equals(Material.IRON_ORE)) guiManager.openClaimInspector(
                    player,
                    itemName
                );
            }
            if (itemName.equals("Next Page")) {
                final int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(24));
                guiManager.openAllClaimsGUI(player, (numRegionsToSkip * 28));
            }
            if (itemName.equals("Previous Page")) {
                final int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(24));
                guiManager.openAllClaimsGUI(player, (numRegionsToSkip - 2) * 28);
            }
            if (itemName.equals("Back")) guiManager.openMainGUI(player);
            if (itemName.equals("Close")) player.closeInventory();
            return;
        }

        switch (inventoryTitle) {
            case "LandClaim Claim Limits" -> {
                if (itemName.equalsIgnoreCase("Back")) guiManager.openMainGUI(player);
                if (itemName.equalsIgnoreCase("Close")) player.closeInventory();
                return;
            }
            case "LandClaim Inspector" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
                if (itemName.startsWith("Remove")) guiManager.promptForRemoval(player.getName(), regionName);
                if (itemName.startsWith("Players")) guiManager.openOwnersMembersEditor(player, regionName);
                if (itemName.startsWith("Flag Editor")) guiManager.openFlagsGUI(player, regionName);
                if (itemName.startsWith("Teleport")) guiManager.openTeleportGUI(player, regionName);

                if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) guiManager.openAllClaimsGUI(player);
                if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) player.closeInventory();
                return;
            }
            case "Owners/Members Editor" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());

                if (itemName.startsWith("View/Remove Players")) guiManager.openPlayersEditor(
                    player,
                    regionName
                );
                if (itemName.startsWith("Add Player to Claim")) guiManager.openAddPlayer(
                    player.getName(),
                    regionName
                );

                if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) guiManager.openClaimInspector(
                    player,
                    regionName
                );
                if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) player.closeInventory();
                return;
            }
            case "LandClaim Claim Removal" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());


                if (itemName.startsWith("Remove")) {
                    Claim.removeRegion(player, regionName);
                    guiManager.openAllClaimsGUI(player);
                }

                if (ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) guiManager.openClaimInspector(
                    player,
                    regionName
                );
                if (ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) player.closeInventory();
                return;
            }
            case "View/Remove Players" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(47).getItemMeta().getDisplayName());

                if (itemName.equalsIgnoreCase("Back")) {
                    guiManager.openOwnersMembersEditor(player, regionName);
                    return;
                }
                if (itemName.equalsIgnoreCase("Close")) {
                    player.closeInventory();
                    return;
                }

                if (player.hasPermission("landclaim.removeplayer")) {
                    final ItemMeta clickedItemMeta = clickEvent.getCurrentItem().getItemMeta();
                    String firstLore = "";
                    if (clickedItemMeta.getLore() != null)
                        firstLore = ChatColor.stripColor(clickedItemMeta.getLore().getFirst());
                    if (firstLore.startsWith("UUID")) {
                        final String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName);
                        if ((ownerOrMember != null && ownerOrMember.equalsIgnoreCase("Owner")) || player.hasPermission(
                            "landclaim.edit.others")) {
                            if (itemStack.getType().equals(Material.WITHER_SKELETON_SKULL)) {
                                guiManager.openOwnerRemover(player, firstLore.substring(5), regionName);
                            }
                            if (itemStack.getType().equals(Material.SKELETON_SKULL)) {
                                guiManager.openMemberRemover(player, firstLore.substring(5), regionName);
                            }
                        } else player.sendMessage(ChatColor.GOLD + "Only claim owners can remove players from claims.");
                    }
                }
                return;
            }
        }

        if (inventoryTitle.equalsIgnoreCase("Remove Member")) {
            final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());


            final String uuid = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(
                1)).substring(6);
            final String playerToRemoveName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(
                0)).substring(15);
            if (itemName.startsWith("Are you sure")) {
                final World world = BukkitAdapter.adapt(player.getWorld());
                final RegionManager rgManager = ANOLC.getWg().getPlatform().getRegionContainer().get(world);
                if (Claim.removeMember(player, uuid, rgManager.getRegion(regionName))) {
                    player.sendMessage(ChatColor.GOLD + "You removed member " + ChatColor.AQUA + playerToRemoveName + ChatColor.GOLD + " from " + ChatColor.AQUA + regionName + ChatColor.GOLD + ".");
                }
                player.closeInventory();
            }
            if (ChatColor.stripColor(itemName).startsWith("Back")) guiManager.openPlayersEditor(
                player,
                regionName
            );
            if (ChatColor.stripColor(itemName).startsWith("Close")) player.closeInventory();
            return;
        }

        if (inventoryTitle.equalsIgnoreCase("Remove Owner")) {
            final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());


            final String uuid = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(
                1)).substring(6);
            final String playerToRemoveName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore().get(
                0)).substring(14);
            if (itemName.startsWith("Are you sure")) {
                final World world = BukkitAdapter.adapt(player.getWorld());
                final RegionManager rgManager = ANOLC.getWg().getPlatform().getRegionContainer().get(world);
                if (Claim.removeOwner(player, uuid, rgManager.getRegion(regionName))) {
                    player.sendMessage(ChatColor.GOLD + "You removed owner " + ChatColor.AQUA + playerToRemoveName + ChatColor.GOLD + " from " + ChatColor.AQUA + regionName + ChatColor.GOLD + ".");
                }
                player.closeInventory();
            }
            if (ChatColor.stripColor(itemName).startsWith("Back")) guiManager.openPlayersEditor(
                player,
                regionName
            );
            if (ChatColor.stripColor(itemName).startsWith("Close")) player.closeInventory();
            return;
        }

        switch (inventoryTitle) {
            case "Add Player to Claim" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());


                if (itemName.startsWith("Add Owner to")) {
                    final World world = BukkitAdapter.adapt(player.getWorld());
                    final RegionManager rgManager = ANOLC.getWg().getPlatform().getRegionContainer().get(world);
                    final Prompt prompt = new Prompt(
                        ChatColor.GOLD + "Who would you like to add as an owner? " + ChatColor.RED +
                            "/lc cancel" + ChatColor.GOLD + " to cancel",
                        player,
                        "ADDOWNER",
                        rgManager.getRegion(regionName)
                    );
                    prompt.sendPrompt();
                    player.closeInventory();
                }
                if (itemName.startsWith("Add Member to")) {
                    final World world = BukkitAdapter.adapt(player.getWorld());
                    final RegionManager rgManager = ANOLC.getWg().getPlatform().getRegionContainer().get(world);
                    final Prompt prompt = new Prompt(
                        ChatColor.GOLD + "Who would you like to add as a member? " + ChatColor.RED +
                            "/lc cancel" + ChatColor.GOLD + " to cancel",
                        player,
                        "ADDMEMBER",
                        rgManager.getRegion(regionName)
                    );
                    prompt.sendPrompt();
                    player.closeInventory();
                }
                if (itemName.startsWith("Back")) guiManager.openOwnersMembersEditor(player, regionName);
                if (itemName.startsWith("Close")) player.closeInventory();
                return;
            }
            case "LandClaim Teleport" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());


                if (itemName.startsWith("Teleport to ")) Claim.teleportToClaim(player, regionName);
                if (itemName.equalsIgnoreCase("Set Teleport Point")) {
                    Claim.setClaimTeleport(player, regionName);
                }
                if (itemName.equalsIgnoreCase("Remove Teleport Point")) {
                    Claim.removeClaimTeleport(player, regionName);
                }
                if (itemName.startsWith("Back")) guiManager.openClaimInspector(player, regionName);
                if (itemName.startsWith("Close")) player.closeInventory();
                return;
            }
            case "LandClaim Flags" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(38).getItemMeta().getDisplayName());


                if (itemStack.getType().equals(Material.LIME_BANNER) || itemStack.getType().equals(Material.GRAY_BANNER)) {
                    if (GUIManager.editableClaimFlags.get(itemName) instanceof StateFlag)
                        guiManager.openStateFlagEditor(
                            player,
                            regionName,
                            itemName
                        );
                    if (GUIManager.editableClaimFlags.get(itemName) instanceof StringFlag)
                        guiManager.openStringFlagEditor(
                            player,
                            regionName,
                            itemName
                        );
                }
                if (ChatColor.stripColor(itemName).startsWith("Back")) guiManager.openClaimInspector(
                    player,
                    regionName
                );
                if (ChatColor.stripColor(itemName).startsWith("Close")) player.closeInventory();
                return;
            }
            case "LandClaim State Flag Editor" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());

                if (itemName.equalsIgnoreCase("Back")) {
                    guiManager.openFlagsGUI(player, regionName);
                    return;
                }
                if (itemName.equalsIgnoreCase("Close")) {
                    player.closeInventory();
                    return;
                }
                if (itemStack.getType().equals(Material.DARK_OAK_SIGN) || itemStack.getType().equals(Material.BIRCH_SIGN))
                    return;

                final boolean nonOwnerInspector = !Claim.getRegionOwners(
                    player,
                    regionName
                ).contains(player.getUniqueId());
                final boolean nonOwnerEditor = player.hasPermission("landclaim.edit.others");

                if (nonOwnerInspector && !nonOwnerEditor) {
                    player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
                    return;
                }

                final Flag flag = GUIManager.editableClaimFlags.get(clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName());
                final ProtectedRegion region = Claim.getRegion(player, regionName);

                if (itemName.equalsIgnoreCase("Delete Flag")) region.setFlag(flag, null);

                if (itemName.equalsIgnoreCase("Allow")) region.setFlag(flag, StateFlag.State.ALLOW);
                if (itemName.equalsIgnoreCase("Deny")) region.setFlag(flag, StateFlag.State.DENY);

                if (itemName.equalsIgnoreCase("Set for everyone")) region.setFlag(
                    flag.getRegionGroupFlag(),
                    RegionGroup.ALL
                );
                if (itemName.equalsIgnoreCase("Set for members")) region.setFlag(
                    flag.getRegionGroupFlag(),
                    RegionGroup.MEMBERS
                );
                if (itemName.equalsIgnoreCase("Set for owners")) region.setFlag(
                    flag.getRegionGroupFlag(),
                    RegionGroup.OWNERS
                );
                if (itemName.equalsIgnoreCase("Set for non-members")) region.setFlag(
                    flag.getRegionGroupFlag(),
                    RegionGroup.NON_MEMBERS
                );
                if (itemName.equalsIgnoreCase("Set for non-owners")) region.setFlag(
                    flag.getRegionGroupFlag(),
                    RegionGroup.NON_OWNERS
                );

                guiManager.openStateFlagEditor(
                    player,
                    regionName,
                    clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName()
                );
                return;
            }
            case "LandClaim String Flag Editor" -> {
                final String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());

                if (itemName.equalsIgnoreCase("Back")) {
                    guiManager.openFlagsGUI(player, regionName);
                    return;
                }
                if (itemName.equalsIgnoreCase("Close")) {
                    player.closeInventory();
                    return;
                }
                final boolean nonOwnerInspector = !Claim.getRegionOwners(
                    player,
                    regionName
                ).contains(player.getUniqueId());
                final boolean nonOwnerEditor = player.hasPermission("landclaim.edit.others");

                final String flagName = clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName();
                final Flag flag = GUIManager.editableClaimFlags.get(flagName);
                final ProtectedRegion region = Claim.getRegion(player, regionName);

                if (itemName.equalsIgnoreCase("Enter New Value")) {
                    if (nonOwnerInspector && !nonOwnerEditor) {
                        player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
                        return;
                    }
                    player.closeInventory();
                    new Prompt(
                        ChatColor.GOLD + "Enter a new value for the '" + flagName + "' flag",
                        player,
                        flagName,
                        region
                    ).sendPrompt();
                }
                if (itemName.equalsIgnoreCase("Delete Flag")) {
                    if (nonOwnerInspector && !nonOwnerEditor) {
                        player.sendMessage(ChatColor.GOLD + "You cannot edit this claim");
                        return;
                    }
                    region.setFlag(flag, null);
                    player.sendMessage(ChatColor.GOLD + "Removed flag '" + flagName + "' from " + ChatColor.AQUA + regionName);
                }
                return;
            }
        }

        if (inventoryTitle.equalsIgnoreCase("LandClaim Claim Region") || inventoryTitle.equalsIgnoreCase(
            "LandClaim Claim Plot")) {
            if (itemName.startsWith("Back")) guiManager.openMainGUI(player);
            if (itemName.startsWith("Close")) player.closeInventory();
            if ((itemName.equalsIgnoreCase("Are You Sure You Want To Claim This Region?"))
                || (itemName.equalsIgnoreCase("Are You Sure You Want To Claim This Plot?"))) {
                final Claim claim = ANOLC.getClaimMap().get(player.getUniqueId().toString());
                claim.saveClaim();
                if (!claim.isPlot()) player.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&',
                    "&6You successfully claimed a region of land for &b$" + claim.getClaimCost() + "&6!"
                ));
                else if (claim.isPlot()) player.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&',
                    "&6You successfully claimed a plot of land for &b$" + claim.getClaimCost() + "&6!"
                ));

                player.closeInventory();
            }
        }
    }

    public static boolean isLandClaimGui(final Player player) {
        return player.getOpenInventory().equals(GUIClickListener.playerLCInventory.get(player.getUniqueId().toString()));
    }
}
