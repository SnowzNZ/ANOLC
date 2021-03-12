package net.minespire.landclaim.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.GUI.GUIManager;
import net.minespire.landclaim.LandClaim;
import net.minespire.landclaim.Prompt.Prompt;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GUIClick implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent clickEvent) {
        String inventoryTitle = clickEvent.getView().getTitle();
        ItemStack itemStack = clickEvent.getCurrentItem();
        ItemMeta itemMeta;
        String itemName;
        if(itemStack == null) return;
        else itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return;
        else itemName = ChatColor.stripColor(itemMeta.getDisplayName());

        if(!GUIManager.getInst().isLandClaimGui(inventoryTitle)) return;
        //if(!GUIManager.getInst().isLandClaimButton(itemName)) return;

        GUIManager guiManager = GUIManager.getInst();
        Player player;
        if(clickEvent.getWhoClicked() instanceof Player) player = ((Player) clickEvent.getWhoClicked()).getPlayer();
        else return;
        clickEvent.setCancelled(true);

        if(inventoryTitle.equals(LandClaim.config.getString("GUI.GUITitle"))){
            if(ChatColor.stripColor(itemName).equals("Claims")) guiManager.openClaimsGUI(player);
            if(ChatColor.stripColor(itemName).equals("Wand")) guiManager.handleWandClick(player);
            if(ChatColor.stripColor(itemName).equals("Close")) player.closeInventory();
            else return;
        }

        if(inventoryTitle.equals("LandClaim Regions and Plots")){
            if(ChatColor.stripColor(itemName).equals("Owner Regions")) guiManager.openOwnerRegionsGUI(player);
            if(ChatColor.stripColor(itemName).equals("Owner Plots")) guiManager.openOwnerPlotsGUI(player);
            if(ChatColor.stripColor(itemName).equals("Member Regions")) guiManager.openMemberRegionsGUI(player);
            if(ChatColor.stripColor(itemName).equals("Member Plots")) guiManager.openMemberPlotsGUI(player);

            if(ChatColor.stripColor(itemName).equals("Back")) guiManager.openMainGUI(player);
            if(ChatColor.stripColor(itemName).equals("Close")) player.closeInventory();
            else return;
        }

        if(inventoryTitle.startsWith("Owner Regions")){
            if(itemName.startsWith("Inspect")) guiManager.openClaimInspector(player, itemName.substring(8));
            if(itemName.equals("Next Page")) {
                int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(21));
                guiManager.openOwnerRegionsGUI(player,(numRegionsToSkip*28));
            }
            if(itemName.equals("Previous Page")) {
                int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(21));
                guiManager.openOwnerRegionsGUI(player,(numRegionsToSkip-2)*28);
            }
            if(itemName.equals("Back")) guiManager.openClaimsGUI(player);
            if(itemName.equals("Close")) player.closeInventory();
            else return;
        }

        if(inventoryTitle.startsWith("Owner Plots")){
            if(itemName.startsWith("Inspect")) guiManager.openClaimInspector(player, itemName.substring(8));
            if(itemName.equals("Next Page")) {
                int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(19));
                guiManager.openOwnerPlotsGUI(player,(numRegionsToSkip*28));
            }
            if(itemName.equals("Previous Page")) {
                int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(19));
                guiManager.openOwnerPlotsGUI(player,(numRegionsToSkip-2)*28);
            }
            if(itemName.equals("Back")) guiManager.openClaimsGUI(player);
            if(itemName.equals("Close")) player.closeInventory();
            else return;
        }

        if(inventoryTitle.startsWith("Member Regions")){
            if(itemName.startsWith("Inspect")) guiManager.openClaimInspector(player, itemName.substring(8));
            if(itemName.equals("Next Page")) {
                int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(22));
                guiManager.openMemberRegionsGUI(player,(numRegionsToSkip*28));
            }
            if(itemName.equals("Previous Page")) {
                int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(22));
                guiManager.openMemberRegionsGUI(player,(numRegionsToSkip-2)*28);
            }
            if(itemName.equals("Back")) guiManager.openClaimsGUI(player);
            if(itemName.equals("Close")) player.closeInventory();
            else return;
        }

        if(inventoryTitle.startsWith("Member Plots")){
            if(itemName.startsWith("Inspect")) guiManager.openClaimInspector(player, itemName.substring(8));
            if(itemName.equals("Next Page")) {
                int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(20));
                guiManager.openMemberPlotsGUI(player,(numRegionsToSkip*28));
            }
            if(itemName.equals("Previous Page")) {
                int numRegionsToSkip = Integer.parseInt(inventoryTitle.substring(20));
                guiManager.openMemberPlotsGUI(player,(numRegionsToSkip-2)*28);
            }
            if(itemName.equals("Back")) guiManager.openClaimsGUI(player);
            if(itemName.equals("Close")) player.closeInventory();
            else return;
        }

        if(inventoryTitle.equals("LandClaim Inspector")){
            String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
            if(itemName.startsWith("Remove")) guiManager.promptForRemoval(player.getDisplayName(), regionName);
            if(itemName.startsWith("Owners and Members")) guiManager.openOwnersMembersEditor(player, regionName);
            if(itemName.startsWith("Flag Editor")) guiManager.openFlagsGUI(player, regionName);
            if(itemName.startsWith("Teleport")) guiManager.openTeleportGUI(player, regionName);

            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) guiManager.openClaimsGUI(player);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) player.closeInventory();
        }

        if(inventoryTitle.equals("Owners/Members Editor")){
            String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
            if(itemName.startsWith("View/Remove Players")) guiManager.openPlayersEditor(player, regionName);
            if(itemName.startsWith("Add Player to Claim")) guiManager.openAddPlayer(player.getDisplayName(), regionName);

            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) guiManager.openClaimInspector(player, regionName);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) player.closeInventory();
        }

        if(inventoryTitle.equals("LandClaim Claim Removal")){
            String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(29).getItemMeta().getDisplayName());

            if(itemName.startsWith("Remove")) {
                Claim.removeRegion(player, regionName);
                player.closeInventory();
            }

            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) guiManager.openClaimInspector(player, regionName);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) player.closeInventory();

        }

        if(inventoryTitle.equals("View/Remove Players")){
            String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(47).getItemMeta().getDisplayName());

            ItemMeta clickedItemMeta = clickEvent.getCurrentItem().getItemMeta();
            if(clickedItemMeta != null && clickedItemMeta.getLore() != null && clickedItemMeta.getLore().get(0).startsWith("UUID")){
                if(Claim.playerIsOwnerOrMember(player, regionName).equalsIgnoreCase("Owner")) {
                    guiManager.openPlayerRemover(player, clickedItemMeta.getLore().get(0).substring(5), regionName);
                }
                else player.sendMessage(ChatColor.GOLD + "Only claim owners can remove players from claims.");
            }

            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Back")) guiManager.openOwnersMembersEditor(player, regionName);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Close")) player.closeInventory();
        }

        if(inventoryTitle.equals("Remove Player")){
            String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getDisplayName());
            String uuid = "";
            for (String lorePiece : clickEvent.getClickedInventory().getItem(13).getItemMeta().getLore()){
                if(lorePiece.startsWith("Claim")) regionName = lorePiece.substring(7);
                if(lorePiece.startsWith("UUID")) uuid = lorePiece.substring(6);
            }
            if(itemName.startsWith("Are you sure")) {
                World world = BukkitAdapter.adapt(player.getWorld());
                RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                if(Claim.removeMember(player, itemMeta.getLore().get(3).substring(6), rgManager.getRegion(regionName))){
                    player.sendMessage(ChatColor.GOLD + "You removed " + ChatColor.AQUA + itemMeta.getLore().get(2).substring(13) + ChatColor.GOLD + " from " + ChatColor.AQUA + regionName + ChatColor.GOLD + ".");
                }
                player.closeInventory();
            }
            if(ChatColor.stripColor(itemName).startsWith("Back")) guiManager.openPlayersEditor(player, regionName);
            if(ChatColor.stripColor(itemName).startsWith("Close")) player.closeInventory();
        }

        if(inventoryTitle.equals("Add Player to Claim")){
            String regionName = clickEvent.getClickedInventory().getItem(12).getItemMeta().getDisplayName().substring(13);
            if(itemName.startsWith("Add Owner to")){
                World world = BukkitAdapter.adapt(player.getWorld());
                RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                Prompt prompt = new Prompt(ChatColor.GOLD + "Who would you like to add as an owner? "  + ChatColor.RED +
                        "/lc cancel" + ChatColor.GOLD + " to cancel", player, "ADDOWNER", rgManager.getRegion(regionName));
                prompt.sendPrompt();
                player.closeInventory();
            }
            if(itemName.startsWith("Add Member to")){
                World world = BukkitAdapter.adapt(player.getWorld());
                RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                Prompt prompt = new Prompt(ChatColor.GOLD + "Who would you like to add as a member? " + ChatColor.RED +
                        "/lc cancel" + ChatColor.GOLD + " to cancel", player, "ADDMEMBER", rgManager.getRegion(regionName));
                prompt.sendPrompt();
                player.closeInventory();
            }
            if(itemName.startsWith("Back")) guiManager.openOwnersMembersEditor(player, regionName);
        }

        if(inventoryTitle.equals("LandClaim Teleport")){
            String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(13).getItemMeta().getDisplayName()).substring(12);
            if(itemName.startsWith("Teleport to ")) Claim.teleportToClaim(player, regionName);
            if(itemName.equalsIgnoreCase("Set Teleport Point")) {
                Claim.setClaimTeleport(player, regionName);
                player.closeInventory();
            }
            if(itemName.equalsIgnoreCase("Remove Teleport Point")) {
                Claim.removeClaimTeleport(player, regionName);
                player.closeInventory();
            }
            if(itemName.startsWith("Back")) guiManager.openClaimInspector(player, regionName);
        }

        if(inventoryTitle.equals("LandClaim Flags")){
            String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(38).getItemMeta().getDisplayName());
            if(itemStack.getType().equals(Material.LIME_BANNER) || itemStack.getType().equals(Material.GRAY_BANNER)) {
                if(GUIManager.editableClaimFlags.get(itemName) instanceof StateFlag) guiManager.openStateFlagEditor(player, regionName, itemName);
            }
            if(ChatColor.stripColor(itemName).startsWith("Back")) guiManager.openClaimInspector(player, regionName);
            if(ChatColor.stripColor(itemName).startsWith("Close")) player.closeInventory();
        }

        if(inventoryTitle.equals("LandClaim Flag Editor")){
            String regionName = ChatColor.stripColor(clickEvent.getClickedInventory().getItem(11).getItemMeta().getDisplayName());
            Flag flag = GUIManager.editableClaimFlags.get(clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName());
            ProtectedRegion region = Claim.getRegion(player, regionName);

            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Delete Flag")) region.setFlag(flag, null);

            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Allow")) region.setFlag(flag, StateFlag.State.ALLOW);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Deny")) region.setFlag(flag, StateFlag.State.DENY);

            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Set for everyone")) region.setFlag(flag.getRegionGroupFlag(), RegionGroup.ALL);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Set for members")) region.setFlag(flag.getRegionGroupFlag(), RegionGroup.MEMBERS);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Set for owners")) region.setFlag(flag.getRegionGroupFlag(), RegionGroup.OWNERS);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Set for non-members")) region.setFlag(flag.getRegionGroupFlag(), RegionGroup.NON_MEMBERS);
            if(ChatColor.stripColor(itemName).equalsIgnoreCase("Set for non-owners")) region.setFlag(flag.getRegionGroupFlag(), RegionGroup.NON_OWNERS);

            guiManager.openStateFlagEditor(player, regionName, clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName());

            if(itemName.equalsIgnoreCase("Back")) guiManager.openFlagsGUI(player, regionName);
            if(itemName.equalsIgnoreCase("Close")) player.closeInventory();
        }

    }


}
