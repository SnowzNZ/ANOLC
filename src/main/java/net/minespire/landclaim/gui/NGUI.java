package net.minespire.landclaim.gui;

import net.minespire.landclaim.listener.GUIClickListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class NGUI {

    private final Inventory inventory;
    private final int size;

    public NGUI(final int size, final String title) {
        this.size = size;
        inventory = Bukkit.createInventory(null, size, ChatColor.translateAlternateColorCodes('&', title));
    }

    public NGUI addItem(final Material mat, final String title, final List<String> lore, final Integer slot) {
        inventory.setItem(slot, GUIItem.make(mat, title, lore));
        return this;
    }

    public NGUI addItem(final Material mat, final String title, final List<String> lore) {
        return addItem(mat, title, lore, inventory.firstEmpty());
    }

    public void open(final Player player) {
        GUIClickListener.playerLCInventory.put(player.getUniqueId().toString(), player.openInventory(inventory));
    }

    public int size() {
        return size;
    }

}
