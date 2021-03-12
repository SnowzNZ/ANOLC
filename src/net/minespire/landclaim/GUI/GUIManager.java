package net.minespire.landclaim.GUI;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class GUIManager {

    private static GUIManager inst = new GUIManager();
    private static Set<String> guiInventoryTitles = new HashSet<>();
    private static Set<String> guiButtonTitles = new HashSet<>();
    public static Map<String, Flag> editableClaimFlags = new LinkedHashMap<>();


    static{
        editableClaimFlags.put("build", Flags.BUILD); editableClaimFlags.put("interact", Flags.INTERACT); editableClaimFlags.put("block-break", Flags.BLOCK_BREAK);
        editableClaimFlags.put("block-place", Flags.BLOCK_PLACE); editableClaimFlags.put("use", Flags.USE); editableClaimFlags.put("damage-animals", Flags.DAMAGE_ANIMALS);
        editableClaimFlags.put("chest-access", Flags.CHEST_ACCESS); editableClaimFlags.put("ride", Flags.RIDE); editableClaimFlags.put("pvp", Flags.PVP);
        editableClaimFlags.put("sleep", Flags.SLEEP); editableClaimFlags.put("respawn-anchors", Flags.RESPAWN_ANCHORS); editableClaimFlags.put("tnt", Flags.TNT);
        editableClaimFlags.put("vehicle-place", Flags.PLACE_VEHICLE); editableClaimFlags.put("vehicle-destroy", Flags.DESTROY_VEHICLE); editableClaimFlags.put("lighter", Flags.LIGHTER);
        editableClaimFlags.put("block-trampling", Flags.TRAMPLE_BLOCKS); editableClaimFlags.put("entry", Flags.ENTRY); editableClaimFlags.put("entry-deny-message", Flags.ENTRY_DENY_MESSAGE);
        editableClaimFlags.put("greeting", Flags.GREET_MESSAGE); editableClaimFlags.put("greeting-title", Flags.GREET_TITLE); editableClaimFlags.put("farewell", Flags.FAREWELL_MESSAGE);
        editableClaimFlags.put("farewell-title", Flags.FAREWELL_TITLE); editableClaimFlags.put("enderpearl", Flags.ENDERPEARL); editableClaimFlags.put("chorus-fruit-teleport", Flags.CHORUS_TELEPORT);
        editableClaimFlags.put("item-pickup", Flags.ITEM_PICKUP); editableClaimFlags.put("item-drop", Flags.ITEM_DROP); editableClaimFlags.put("deny-message", Flags.DENY_MESSAGE);
        guiInventoryTitles.add(LandClaim.config.getString("GUI.GUITitle"));
        guiInventoryTitles.add("LandClaim Regions and Plots");
        guiInventoryTitles.add("Owner Regions");
        guiInventoryTitles.add("Owner Plots");
        guiInventoryTitles.add("Member Regions");
        guiInventoryTitles.add("Member Plots");
        guiInventoryTitles.add("LandClaim Teleport");
        guiInventoryTitles.add("LandClaim Inspector");
        guiInventoryTitles.add("LandClaim Claim Removal");
        guiInventoryTitles.add("LandClaim Flags");
        guiInventoryTitles.add("LandClaim Flag Editor");
        guiInventoryTitles.add("Add Player to Claim");
        guiInventoryTitles.add("Owners/Members Editor");
        guiInventoryTitles.add("View/Remove Players");
        guiInventoryTitles.add("Remove Player");
        guiButtonTitles.add("Claims");
        guiButtonTitles.add("Wand");
        guiButtonTitles.add("Back");
        guiButtonTitles.add("Next Page");
        guiButtonTitles.add("Previous Page");
        guiButtonTitles.add("Claim Limits");
        guiButtonTitles.add("Owner Regions");
        guiButtonTitles.add("Owner Plots");
        guiButtonTitles.add("Member Regions");
        guiButtonTitles.add("Member Plots");
    }
    public static GUIManager getInst(){
        return inst;
    }

    public void openMainGUI(Player player){
        NGUI mainGUI = new NGUI(9, LandClaim.config.getString("GUI.GUITitle"));
        mainGUI.addItem(Material.GRASS_BLOCK, ChatColor.GOLD + "Claims", createLore("View and edit your claimed", "regions and plots"));
        mainGUI.addItem(Material.WOODEN_AXE, ChatColor.GOLD + "Wand", createLore("Get a claim wand", "for claiming"));
        mainGUI.addItem(Material.OBSERVER, ChatColor.GOLD + "Claim Limits", createLore("Region Limits:", "You can claim 5 more regions", "Region Limits:", "You can claim 4 more plots"));
        mainGUI.addItem(Material.EMERALD, ChatColor.GOLD + "Popular Regions", createLore("View top-ranked regions"));

        mainGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 8);
        mainGUI.open(player);
    }

    public void openClaimsGUI(Player player){
        NGUI claimsGUI = new NGUI(9, "LandClaim Regions and Plots");
        claimsGUI.addItem(Material.DIAMOND_BLOCK, ChatColor.GOLD + "Owner Regions", createLore("View and edit", "regions you own"));
        claimsGUI.addItem(Material.DIAMOND_ORE, ChatColor.GOLD + "Owner Plots", createLore("View and edit", "plots you own"));
        claimsGUI.addItem(Material.IRON_BLOCK, ChatColor.GOLD + "Member Regions", createLore("View and edit regions", "you are a member of"));
        claimsGUI.addItem(Material.IRON_ORE, ChatColor.GOLD + "Member Plots", createLore("View and edit plots", "you are a member of"));
        claimsGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 7);
        claimsGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 8);
        claimsGUI.open(player);
    }


    public void openOwnerRegionsGUI(Player player){openOwnerRegionsGUI(player,0);}
    public void openOwnerRegionsGUI(Player player, int numRegionsToSkip){
        int nextSlot = 9;
        List<ProtectedRegion> ownerRegions = Claim.getClaimListOwner(player, false);
        Collections.sort(ownerRegions);
        int totalSlots = ((int) Math.ceil(ownerRegions.size()/7d)+2)*9;
        if(totalSlots > 54) totalSlots = 54;

        NGUI ownerRegionsGUI = new NGUI(totalSlots, "Owner Regions - Page " + (numRegionsToSkip/28 + 1));
        if(numRegionsToSkip>0) ownerRegionsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Previous Page", null, 48);
        int loopCounter = 0;
        for(ProtectedRegion rg : ownerRegions) {
            if(loopCounter++ < numRegionsToSkip) continue;
            if(++nextSlot > 43) {
                ownerRegionsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Next Page", null, 50);
                break;
            }
            if((nextSlot+1)%9 == 0) nextSlot = nextSlot + 2;
            ownerRegionsGUI.addItem(Material.DIAMOND_BLOCK, ChatColor.GOLD + "Inspect " + rg.getId(), null, nextSlot);
        }
        ownerRegionsGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, totalSlots-5);
        ownerRegionsGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, totalSlots-3);
        ownerRegionsGUI.open(player);
    }

    public void openOwnerPlotsGUI(Player player){openOwnerPlotsGUI(player,0);}
    public void openOwnerPlotsGUI(Player player, int numRegionsToSkip){
        int nextSlot = 9;
        List<ProtectedRegion> ownerPlots = Claim.getClaimListOwner(player, true);
        Collections.sort(ownerPlots);
        int totalSlots = ((int) Math.ceil(ownerPlots.size()/7d)+2)*9;
        if(totalSlots > 54) totalSlots = 54;

        NGUI ownerPlotsGUI = new NGUI(totalSlots, "Owner Plots - Page " + (numRegionsToSkip/28 + 1));
        if(numRegionsToSkip>0) ownerPlotsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Previous Page", null, 48);
        int loopCounter = 0;
        for(ProtectedRegion rg : ownerPlots) {
            if(loopCounter++ < numRegionsToSkip) continue;
            if(++nextSlot > 43) {
                ownerPlotsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Next Page", null, 50);
                break;
            }
            if((nextSlot+1)%9 == 0) nextSlot = nextSlot + 2;
            ownerPlotsGUI.addItem(Material.DIAMOND_ORE, ChatColor.GOLD + "Inspect " + rg.getId(), null, nextSlot);
        }
        ownerPlotsGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, totalSlots-5);
        ownerPlotsGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, totalSlots-3);
        ownerPlotsGUI.open(player);
    }

    public void openMemberRegionsGUI(Player player){openMemberRegionsGUI(player,0);}
    public void openMemberRegionsGUI(Player player, int numRegionsToSkip){
        int nextSlot = 9;
        List<ProtectedRegion> memberRegions = Claim.getClaimListMember(player, false);
        Collections.sort(memberRegions);
        int totalSlots = ((int) Math.ceil(memberRegions.size()/7d)+2)*9;
        if(totalSlots > 54) totalSlots = 54;

        NGUI memberRegionsGUI = new NGUI(totalSlots, "Member Regions - Page " + (numRegionsToSkip/28 + 1));
        if(numRegionsToSkip>0) memberRegionsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Previous Page", null, 48);
        int loopCounter = 0;
        for(ProtectedRegion rg : memberRegions) {
            if(loopCounter++ < numRegionsToSkip) continue;
            if(++nextSlot > 43) {
                memberRegionsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Next Page", null, 50);
                break;
            }
            if((nextSlot+1)%9 == 0) nextSlot = nextSlot + 2;
            memberRegionsGUI.addItem(Material.IRON_BLOCK, ChatColor.GOLD + "Inspect " + rg.getId(), null, nextSlot);
        }
        memberRegionsGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, totalSlots-5);
        memberRegionsGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, totalSlots-3);
        memberRegionsGUI.open(player);
    }

    public void openMemberPlotsGUI(Player player){openMemberPlotsGUI(player,0);}
    public void openMemberPlotsGUI(Player player, int numRegionsToSkip){
        int nextSlot = 9;
        List<ProtectedRegion> memberPlots = Claim.getClaimListMember(player, true);
        Collections.sort(memberPlots);
        int totalSlots = ((int) Math.ceil(memberPlots.size()/7d)+2)*9;
        if(totalSlots > 54) totalSlots = 54;

        NGUI memberPlotsGUI = new NGUI(totalSlots, "Member Plots - Page " + (numRegionsToSkip/28 + 1));
        if(numRegionsToSkip>0) memberPlotsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Previous Page", null, 48);
        int loopCounter = 0;
        for(ProtectedRegion rg : memberPlots) {
            if(loopCounter++ < numRegionsToSkip) continue;
            if(++nextSlot > 43) {
                memberPlotsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Next Page", null, 50);
                break;
            }
            if((nextSlot+1)%9 == 0) nextSlot = nextSlot + 2;
            memberPlotsGUI.addItem(Material.IRON_ORE, ChatColor.GOLD + "Inspect " + rg.getId(), null, nextSlot);
        }
        memberPlotsGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, totalSlots-5);
        memberPlotsGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, totalSlots-3);
        memberPlotsGUI.open(player);
    }

    public void handleWandClick(Player player){
        Bukkit.dispatchCommand(player, "/wand");
    }

    private List<String> createLore(String... lorePieces){
        List<String> completedLore = new ArrayList<>();
        for(String lore : lorePieces){
            completedLore.add(lore);
        }
        return completedLore;
    }

    public boolean isLandClaimGui(String title){
        if(guiInventoryTitles.contains(title)) return true;
        if(title.startsWith("Owner Regions") || title.startsWith("Owner Plots") || title.startsWith("Member Regions") || title.startsWith("Member Plots")) return true;
        else return false;
    }

    public boolean isLandClaimButton(String title){
        if(guiButtonTitles.contains(title)) return true;
        if(title.startsWith("Inspect")) return true;
        else return false;
    }

    public void openClaimInspector(Player player, String regionName) {
        ProtectedRegion region = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion(regionName);
        NGUI inspectorGUI = new NGUI(18, "LandClaim Inspector");

        inspectorGUI.addItem(Material.PLAYER_HEAD, ChatColor.GOLD + "Owners and Members", null);
        inspectorGUI.addItem(Material.ENDER_PEARL, ChatColor.GOLD + "Teleport", null);
        if(Claim.playerIsOwnerOrMember(player, regionName).equalsIgnoreCase("Owner")) inspectorGUI.addItem(Material.CYAN_BANNER, ChatColor.GOLD + "Flag Editor", null);
        if(Claim.playerIsOwnerOrMember(player, regionName).equalsIgnoreCase("Owner")) inspectorGUI.addItem(Material.STRUCTURE_VOID, ChatColor.GOLD + "Remove " + regionName, null);

        inspectorGUI.addItem(Material.BIRCH_SIGN, ChatColor.GOLD + regionName, null, 11);
        inspectorGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 13);
        inspectorGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 15);
        inspectorGUI.open(player);
    }

    public void openOwnersMembersEditor(Player player, String regionName){
        int firstSlot = 0;
        NGUI inspectorGUI = new NGUI(18, "Owners/Members Editor");

        inspectorGUI.addItem(Material.PLAYER_HEAD, ChatColor.GOLD + "View/Remove Players", null, firstSlot++);
        if(Claim.playerIsOwnerOrMember(player, regionName).equalsIgnoreCase("Owner")) inspectorGUI.addItem(Material.TOTEM_OF_UNDYING, ChatColor.GOLD + "Add Player to Claim", null, firstSlot++);

        inspectorGUI.addItem(Material.BIRCH_SIGN, ChatColor.GOLD + regionName, null, 11);
        inspectorGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 13);
        inspectorGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 15);
        inspectorGUI.open(player);
    }

    public void promptForRemoval(String playerName, String regionName){
        NGUI removalPrompt = new NGUI(36, "LandClaim Claim Removal");
        removalPrompt.addItem(Material.STRUCTURE_VOID, ChatColor.GOLD + "Remove " + regionName + "?", createLore("Warning:", "This cannot be undone"), 13);

        removalPrompt.addItem(Material.BIRCH_SIGN, ChatColor.GOLD + regionName, null, 29);
        removalPrompt.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        removalPrompt.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
        removalPrompt.open(Bukkit.getPlayer(playerName));
    }

    public void openAddPlayer(String playerName, String regionName){
        NGUI addPlayer = new NGUI(36, "Add Player to Claim");
        addPlayer.addItem(Material.WITHER_SKELETON_SKULL, "Add Owner to " + regionName, createLore("This will give the", "player full permissions", "on this claim"), 12);
        addPlayer.addItem(Material.SKELETON_SKULL, "Add Member to " + regionName, createLore("This will give the", "player partial permissions", "on this claim"), 14);
        addPlayer.addItem(Material.ARROW, "Back", null, 31);
        addPlayer.open(Bukkit.getPlayer(playerName));
    }

    public void openPlayersEditor(Player player, String regionName) {
        NGUI playersEditor = new NGUI(54, "View/Remove Players");
        Set<UUID> owners = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion(regionName).getOwners().getPlayerDomain().getUniqueIds();
        for(UUID uuid : owners){
            playersEditor.addItem(Material.WITHER_SKELETON_SKULL, Bukkit.getOfflinePlayer(uuid).getName(), createLore("UUID:" + uuid));
        }
        Set<UUID> members = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion(regionName).getMembers().getPlayerDomain().getUniqueIds();
        for(UUID uuid : members){
            playersEditor.addItem(Material.SKELETON_SKULL, Bukkit.getOfflinePlayer(uuid).getName(), createLore("UUID:" + uuid));
        }

        playersEditor.addItem(Material.BIRCH_SIGN, ChatColor.GOLD + regionName, null, 47);
        playersEditor.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 49);
        playersEditor.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 51);
        playersEditor.open(player);
    }

    public void openPlayerRemover(Player player, String uuid, String regionName) {
        NGUI removePlayer = new NGUI(36, "Remove Player");
        removePlayer.addItem(Material.BARRIER, "Are you sure?", createLore("Remove Player", "Claim: " + regionName, "Player Name: " + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName(), "UUID: " + uuid), 13);

        removePlayer.addItem(Material.BIRCH_SIGN, ChatColor.GOLD + regionName, null, 29);
        removePlayer.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        removePlayer.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
        removePlayer.open(player);
    }

    public void openTeleportGUI(Player player, String regionName) {
        NGUI teleportGUI = new NGUI(36, "LandClaim Teleport");
        if(Claim.playerIsOwnerOrMember(player, regionName).equalsIgnoreCase("Owner")) teleportGUI.addItem(Material.FURNACE, ChatColor.GOLD + "Remove Teleport Point", createLore("Remove the teleport", "point for " + regionName + "."), 11);
        teleportGUI.addItem(Material.ENDER_PEARL, ChatColor.GOLD + "Teleport to " + regionName, null, 13);
        if(Claim.playerIsOwnerOrMember(player, regionName).equalsIgnoreCase("Owner")) teleportGUI.addItem(Material.CRAFTING_TABLE, ChatColor.GOLD + "Set Teleport Point", createLore("Set the teleport point", "for " + regionName + " to", "your current location."), 15);
        teleportGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        teleportGUI.open(player);
    }

    public void openFlagsGUI(Player player, String regionName) {
        NGUI flagEditor = new NGUI(45, "LandClaim Flags");
        ProtectedRegion region = Claim.getRegion(player, regionName);

        editableClaimFlags.forEach((flagName, flag)-> {
            if(region.getFlags().containsKey(editableClaimFlags.get(flagName))){
                flagEditor.addItem(Material.LIME_BANNER, ChatColor.GOLD + flagName, null);
            } else flagEditor.addItem(Material.GRAY_BANNER, ChatColor.GOLD + flagName, null);
        });

        flagEditor.addItem(Material.BIRCH_SIGN, ChatColor.GOLD + regionName, null,  38);
        flagEditor.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 40);
        flagEditor.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 42);
        flagEditor.open(player);
    }

    public void openStateFlagEditor(Player player, String regionName, String flagName) {
        NGUI flagEditor = new NGUI(18, "LandClaim Flag Editor");

        flagEditor.addItem(Material.DARK_OAK_SIGN, flagName, null, 0);

        ProtectedRegion region = Claim.getRegion(player, regionName);

        if(region.getFlags().containsKey(editableClaimFlags.get(flagName))){
            if(region.getFlag(editableClaimFlags.get(flagName)).toString().equalsIgnoreCase("ALLOW")){
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Allow", null);
                flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Deny", null);
            } else {
                flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.GREEN + "Allow", null);
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.RED + "Deny", null);
            }

            flagEditor.addItem(Material.BARRIER, ChatColor.RED + "Delete Flag", null);
            RegionGroup regionGroup = region.getFlag(editableClaimFlags.get(flagName).getRegionGroupFlag());

            if(regionGroup == null || regionGroup.equals(RegionGroup.ALL)){
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for everyone", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for everyone", null);

            if(regionGroup != null && regionGroup.equals(RegionGroup.MEMBERS)){
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for members", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for members", null);

            if(regionGroup != null && regionGroup.equals(RegionGroup.OWNERS)){
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for owners", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for owners", null);

            if(regionGroup != null && regionGroup.equals(RegionGroup.NON_MEMBERS)){
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for non-members", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for non-members", null);

            if(regionGroup != null && regionGroup.equals(RegionGroup.NON_OWNERS)){
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for non-owners", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for non-owners", null);
        } else {
            flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.GREEN + "Allow", null);
            flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Deny", null);
            flagEditor.addItem(Material.BARRIER, ChatColor.RED + "Delete Flag", null);
            flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for everyone", null);
            flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for members", null);
            flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for owners", null);
            flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for non-members", null);
            flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for non-owners", null);
        }


        flagEditor.addItem(Material.BIRCH_SIGN, ChatColor.GOLD + regionName, null, 11);
        flagEditor.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 13);
        flagEditor.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 15);
        flagEditor.open(player);
    }
}
