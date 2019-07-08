package net.minespire.landclaim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldGuardPlatform;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.md_5.bungee.api.ChatColor;

public class GUI{
	private Inventory GUI;
	private String rgName;
	private Player player;
	Claim claim;
	
	public GUI(Player player, String rgName) {
		this.rgName = rgName;
		GUI = Bukkit.createInventory(null, 54, "Claim Land");
	}
	
	
	public void makeGuiItems() {
		ItemStack guiItem1 = new ItemStack(Material.GRASS_BLOCK);
		List<String> itemLore = new ArrayList<>();
		ItemMeta meta = guiItem1.getItemMeta();
		claim = LandClaim.claimMap.get(player.getUniqueId().toString());
		
		itemLore.add("This claim will cost " + claim.getClaimCost() + " to claim.");
		
		meta.setDisplayName("Claim Area: " + claim.getClaimArea() + " square blocks");
		meta.setLore(itemLore);
		guiItem1.setItemMeta(meta);
		GUI.setItem(12, guiItem1);
		
	}

	
	public void openClaimGUI() {
		player.openInventory(GUI);
	}
	

}