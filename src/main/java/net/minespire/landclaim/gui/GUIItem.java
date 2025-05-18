package net.minespire.landclaim.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

class GUIItem {
    private GUIItem() {
    }

    public static ItemStack make(final Material material, final String title, final List<String> lore) {
        final ItemStack itemStack;
        itemStack = new ItemStack(material);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setDisplayName(title);
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
