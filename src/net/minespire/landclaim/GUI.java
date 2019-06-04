package net.minespire.landclaim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.task.RegionLister;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class GUI implements Listener{
	 
    // Create a new inventory, with no owner, a size of nine, called example
    private final Inventory inv;
 
    public GUI(Player player) {
        inv = Bukkit.createInventory(null, 9, "Example");
        World world = player.getWorld();
        RegionManager region = WorldGuard.getInstance().getPlatform().getRegionContainer().get(world);

        Map<String, ProtectedRegion> regions = region.getRegions();
        
        for (Map.Entry<String, ProtectedRegion> entry : regions.entrySet()) {
            if (entry.getValue().getOwners().getPlayers().contains(player.getName())) {
            	inv.addItem(createGuiItem(entry.getValue().getId(), new ArrayList<String>(Arrays.asList("This is an example!")), Material.DIAMOND_SWORD));
            }
        }
        inv.addItem(createGuiItem("Example Sword", new ArrayList<String>(Arrays.asList("This is an example!")), Material.DIAMOND_SWORD));
        inv.addItem(createGuiItem("Example Shovel", new ArrayList<String>(Arrays.asList("This is an example!")), Material.DIAMOND_SHOVEL));
    }


	// Nice little method to create a gui item with a custom name, and description
    public ItemStack createGuiItem(String name, ArrayList<String> desc, Material mat) {
        ItemStack i = new ItemStack(mat, 1);
        ItemMeta iMeta = i.getItemMeta();
        iMeta.setDisplayName(name);
        iMeta.setLore(desc);
        i.setItemMeta(iMeta);
        return i;
    }
 
    // You can open the inventory with this
    public void openInventory(Player p) {
    	((HumanEntity) p).openInventory(inv);
        return;
    }
 
    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String invName = e.getInventory().getTitle();
        if (!invName.equals(inv.getName())) {
            return;
        }
        if (e.getClick().equals(ClickType.NUMBER_KEY)){
             e.setCancelled(true); 
        }
        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();

        // verify current item is not null
        if (clickedItem == null || clickedItem.getType().equals(Material.AIR)) return;

        // Using slots click is a best option for your inventory click's
        if (e.getRawSlot() == 10) ((HumanEntity)p).sendMessage("You clicked at slot " + 10);
    }
}