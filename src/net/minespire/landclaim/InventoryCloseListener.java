package net.minespire.landclaim;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClose(InventoryCloseEvent closeEvent) {
		for(String title : GUI.inventoryNames) {
			if(closeEvent.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', title))) {
				LandClaim.claimMap.remove(closeEvent.getPlayer().getUniqueId().toString());
			}
		}

	}
}
