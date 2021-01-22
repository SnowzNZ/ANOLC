package net.minespire.landclaim.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.protection.managers.RegionManager;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.GUI.GUIManager;
import net.minespire.landclaim.LandClaim;
import net.minespire.landclaim.Prompt.Prompt;
import org.bukkit.ChatColor;
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
        else itemName = itemMeta.getDisplayName();

        if(!GUIManager.getInst().isLandClaimGui(inventoryTitle)) return;
        //if(!GUIManager.getInst().isLandClaimButton(itemName)) return;

        GUIManager guiManager = GUIManager.getInst();
        Player player;
        if(clickEvent.getWhoClicked() instanceof Player) player = ((Player) clickEvent.getWhoClicked()).getPlayer();
        else return;
        clickEvent.setCancelled(true);

        if(inventoryTitle.equals(LandClaim.config.getString("GUI.GUITitle"))){
            if(itemName.equals("Claims")) guiManager.openClaimsGUI(player);
            if(itemName.equals("Wand")) guiManager.handleWandClick(player);
            else return;
        }

        if(inventoryTitle.equals("LandClaim Regions and Plots")){
            if(itemName.equals("Owner Regions")) guiManager.openOwnerRegionsGUI(player);
            if(itemName.equals("Owner Plots")) guiManager.openOwnerPlotsGUI(player);
            if(itemName.equals("Member Regions")) guiManager.openMemberRegionsGUI(player);
            if(itemName.equals("Member Plots")) guiManager.openMemberPlotsGUI(player);
            if(itemName.equals("Back")) guiManager.openMainGUI(player);
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
            if(itemName.equals("Back"))guiManager.openClaimsGUI(player);
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
            else return;
        }

        if(inventoryTitle.equals("LandClaim Inspector")){
            String regionName = clickEvent.getClickedInventory().getItem(0).getItemMeta().getDisplayName();
            if(itemName.startsWith("Remove")) guiManager.promptForRemoval(player.getDisplayName(), regionName);
            if(itemName.startsWith("Add Player to Claim")) guiManager.openAddPlayer(player.getDisplayName(), regionName);
            if(itemName.startsWith("Back")) guiManager.openClaimsGUI(player);
        }

        if(inventoryTitle.equals("LandClaim Claim Removal")){
            String regionName = clickEvent.getClickedInventory().getItem(13).getItemMeta().getDisplayName().substring(7);
            regionName = regionName.substring(0,regionName.length()-1);
            if(itemName.startsWith("Back")) guiManager.openClaimInspector(player, regionName);
            if(itemName.startsWith("Remove")) {
                Claim.removeRegion(player, regionName);
                player.closeInventory();
            }
        }

        if(inventoryTitle.equals("LandClaim Add Player")){
            if(itemName.startsWith("Add Owner to")){
                String regionName = clickEvent.getCurrentItem().getItemMeta().getDisplayName().substring(13);
                World world = BukkitAdapter.adapt(player.getWorld());
                RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                Prompt prompt = new Prompt("Who would you like to add as an owner?"  + ChatColor.RED +
                        " /lc cancel" + ChatColor.WHITE + " to cancel", player, "ADDOWNER", rgManager.getRegion(regionName));
                prompt.sendPrompt();
                player.closeInventory();
            }
            if(itemName.startsWith("Add Member to")){
                String regionName = clickEvent.getCurrentItem().getItemMeta().getDisplayName().substring(14);
                World world = BukkitAdapter.adapt(player.getWorld());
                RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
                Prompt prompt = new Prompt("Who would you like to add as a member?" + ChatColor.RED +
                        "/lc cancel" + ChatColor.WHITE + " to cancel", player, "ADDMEMBER", rgManager.getRegion(regionName));
                prompt.sendPrompt();
                player.closeInventory();
            }
            if(itemName.startsWith("Back")) guiManager.openClaimsGUI(player);
        }
    }
}
