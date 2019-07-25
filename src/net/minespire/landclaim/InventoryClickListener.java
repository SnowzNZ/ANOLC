package net.minespire.landclaim;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.tags.ItemTagType;


public class InventoryClickListener implements Listener {

	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent clickEvent) {
		boolean LandClaimGUI = false;
		for(String title : GUI.inventoryNames) {
			if(clickEvent.getWhoClicked().getOpenInventory().getTitle().equals(ChatColor.translateAlternateColorCodes('&', title))) {
				clickEvent.setCancelled(true);
				LandClaimGUI = true;
			}
		}
		if(!LandClaimGUI) return;
			
		Player player = (Player) clickEvent.getWhoClicked();
		ItemStack clickedItem = clickEvent.getCurrentItem();
		
		if((clickedItem != null) && ((clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.ClaimItem.ItemName"))) 
				|| (clickedItem.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.ClaimItem.ItemName"))))))){
			Claim claim = LandClaim.claimMap.get(player.getUniqueId().toString());
			claim.saveClaim();
			//DeedListener deed = new DeedListener(player, claim.getRegionName(), claim.getWorld().getName(), true);
			//player.getInventory().addItem(deed.createDeed());
			player.sendMessage("You successfully claimed a region of land for $" + claim.getClaimCost() + "!");
			LandClaim.econ.withdrawPlayer(player, claim.getClaimCost());
			player.getOpenInventory().close();			
		}
		
		if((clickedItem != null) && (clickedItem.getItemMeta().getDisplayName().equals("Yes, Activate this deed!"))) {
			Claim claim = new Claim(player, clickedItem.getItemMeta().getCustomTagContainer().getCustomTag(DeedListener.regionNameKey, ItemTagType.STRING), clickedItem.getItemMeta().getCustomTagContainer().getCustomTag(DeedListener.worldNameKey, ItemTagType.STRING));
			claim.setNewOwner();
			player.sendMessage("You successfully claimed land!");
			player.getOpenInventory().close();			
		}
		
		if((clickedItem != null) && (clickedItem.getItemMeta().getDisplayName().equals("No. Do not activate."))) {
			player.sendMessage("You cancelled the deed activation");
			player.getOpenInventory().close();			
		}
		
		
		
	}
}
