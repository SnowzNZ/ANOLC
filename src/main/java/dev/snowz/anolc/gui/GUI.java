package dev.snowz.anolc.gui;

import dev.snowz.anolc.ANOLC;
import dev.snowz.anolc.claim.Claim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public final class GUI {

    private Inventory inventory;
    private Player player;
    private final int guiSlots;
    public static Map<String, List<GUI>> playersGUIMap = new HashMap<>();
    private final List<GUIItem> guiItems;
    public static List<String> inventoryNames = new ArrayList<>();

    public GUI(final int guiSlots) {
        final int slotCountModifier = guiSlots % 9;
        if (slotCountModifier != 0) {
            this.guiSlots = (9 - slotCountModifier) + guiSlots;
        } else this.guiSlots = guiSlots;
        guiItems = new ArrayList<>();
    }

    public GUI() {
        this(ANOLC.getInstance().getConfig().getInt("GUI.Slots"));
    }

    public int getNumSlots() {
        return guiSlots;
    }

    public int getNumGUIItems() {
        return guiItems.size();
    }

    public void openGUI() {
        setItemsToSlots();
        player.openInventory(inventory);
    }

    public GUI setPlayer(final Player player) {
        this.player = player;
        //playersGUIMap.put(player.getUniqueId().toString(), playerMenus);
        return this;
    }

    public GUI setInventory(final String inventoryName) {
        inventory = Bukkit.createInventory(null, guiSlots, ChatColor.translateAlternateColorCodes('&', inventoryName));
        if (!inventoryNames.contains(inventoryName)) inventoryNames.add(inventoryName);
        return this;
    }

    public GUI addGUIItem(final GUIItem item) {
        guiItems.add(item);
        return this;
    }

    private void createFillerItems() {
        final int slotsToFill = this.getNumSlots() - this.getNumGUIItems();
        for (int x = 0; x < slotsToFill; x++) {
            this.addGUIItem(new GUIItem(Material.getMaterial(ANOLC.getInstance().getConfig().getString(
                "GUI.FillerItem"))).setDisplayName(" ").setMeta());
        }
    }

    public static void promptForRemoval(final String playerName) {
        if (Claim.awaitingRemovalConfirmation.containsKey(playerName)) {
            final GUI gui = new GUI(27);
            gui.setPlayer(Bukkit.getPlayer(playerName));
            gui.setInventory("Region Removal");
            final GUIItem button = new GUIItem(Material.getMaterial(ANOLC.getInstance().getConfig().getString(
                "GUI.RemoveClaimButton.Material")));
            button.setDisplayName("Remove " + Claim.awaitingRemovalConfirmation.get(playerName).getId() + "?");
            button.setLore(ChatColor.RED + "Warning:" + ChatColor.WHITE + " This cannot be undone");
            button.setSlot(13).setMeta();
            gui.addGUIItem(button);
            gui.openGUI();
        }
    }

    private void setItemsToSlots() {
        if (inventory.firstEmpty() < 0) return;
        createFillerItems();
        Iterator<GUIItem> iterator = guiItems.iterator();
        final List<GUIItem> fillerItems = new ArrayList<>();
        GUIItem tempHolder;
        while (iterator.hasNext()) {
            tempHolder = iterator.next();
            if (tempHolder.slotNum > 0 && tempHolder.slotNum < 54) {
                inventory.setItem(tempHolder.slotNum, tempHolder.item);
            } else fillerItems.add(tempHolder);
        }
        iterator = fillerItems.iterator();
        while (iterator.hasNext()) {
            inventory.setItem(inventory.firstEmpty(), iterator.next().item);
        }
    }

    public static final class GUIItem {
        private final ItemStack item;
        private final ItemMeta guiItemMeta;
        private final String displayName;
        private final List<String> lore;
        private int slotNum = -1;

        public GUIItem(final Material material) {
            item = new ItemStack(material);
            guiItemMeta = item.getItemMeta();
            displayName = "";
            lore = new ArrayList<>();
        }

        public GUIItem setDisplayName(final String name) {
            this.guiItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            return this;
        }

        public GUIItem setSlot(final int slot) {
            slotNum = slot;
            return this;
        }

        public GUIItem setLore(final List<String> lore) {
            guiItemMeta.setLore(lore);
            return this;
        }

        public GUIItem setLore(final String loreString) {
            guiItemMeta.setLore(parseLoreString(loreString));
            return this;
        }

        public GUIItem setMeta() {
            item.setItemMeta(guiItemMeta);
            return this;
        }

        private List<String> parseLoreString(final String loreString) {
            final String[] loreArray = loreString.split("\\|");
            final List<String> loreList = new ArrayList<>();
            for (int x = 0; x < loreArray.length; x++) {
                loreList.add(x, ChatColor.translateAlternateColorCodes('&', loreArray[x]));
            }
            return loreList;
        }
    }

    public static void saveGUIToPlayerMap(final Player player, final GUI gui, final boolean startNewList) {
        final List<GUI> playerGUIList;
        if (!GUI.playersGUIMap.containsKey(player.getUniqueId().toString()) || startNewList) {
            playerGUIList = new ArrayList<>();
            playerGUIList.add(gui);
            GUI.playersGUIMap.put(player.getUniqueId().toString(), playerGUIList);
        } else {
            playerGUIList = GUI.playersGUIMap.get(player.getUniqueId().toString());
            playerGUIList.add(gui);
            GUI.playersGUIMap.put(player.getUniqueId().toString(), playerGUIList);
        }
    }
}