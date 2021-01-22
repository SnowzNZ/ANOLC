package net.minespire.landclaim.GUI;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class GUIManager {

    private static GUIManager inst = new GUIManager();
    private static Set<String> guiInventoryTitles = new HashSet<>();
    private static Set<String> guiButtonTitles = new HashSet<>();

    static{
        guiInventoryTitles.add(LandClaim.config.getString("GUI.GUITitle"));
        guiInventoryTitles.add("LandClaim Regions and Plots");
        guiInventoryTitles.add("Owner Regions");
        guiInventoryTitles.add("Owner Plots");
        guiInventoryTitles.add("Member Regions");
        guiInventoryTitles.add("Member Plots");
        guiInventoryTitles.add("LandClaim Inspector");
        guiInventoryTitles.add("LandClaim Claim Removal");
        guiInventoryTitles.add("LandClaim Add Player");
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
        int firstSlot = 0;
        NGUI mainGUI = new NGUI(9, LandClaim.config.getString("GUI.GUITitle"));
        mainGUI.addItem(Material.PAPER, "Claims", createLore("View and edit your claimed", "regions and plots"), firstSlot++);
        mainGUI.addItem(Material.WOODEN_AXE, "Wand", createLore("Get a claim wand", "for claiming"), firstSlot++);
        mainGUI.addItem(Material.WOODEN_AXE, "Claim Limits", createLore("Region Limits:", "You can claim 5 more regions", "Region Limits:", "You can claim 4 more plots"), firstSlot++);
        mainGUI.open(player);
    }

    public void openClaimsGUI(Player player){
        int firstSlot = 0;
        NGUI claimsGUI = new NGUI(9, "LandClaim Regions and Plots");
        claimsGUI.addItem(Material.DIAMOND_BLOCK, "Owner Regions", createLore("View and edit", "regions you own"), firstSlot++);
        claimsGUI.addItem(Material.DIAMOND_ORE, "Owner Plots", createLore("View and edit", "plots you own"), firstSlot++);
        claimsGUI.addItem(Material.IRON_BLOCK, "Member Regions", createLore("View and edit regions", "you are a member of"), firstSlot++);
        claimsGUI.addItem(Material.IRON_ORE, "Member Plots", createLore("View and edit plots", "you are a member of"), firstSlot++);
        claimsGUI.addItem(Material.ARROW, "Back", createLore(""), claimsGUI.size()-1);
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
        if(numRegionsToSkip>0) ownerRegionsGUI.addItem(Material.PAPER, "Previous Page", null, 48);
        int loopCounter = 0;
        for(ProtectedRegion rg : ownerRegions) {
            if(loopCounter++ < numRegionsToSkip) continue;
            if(++nextSlot > 43) {
                ownerRegionsGUI.addItem(Material.PAPER, "Next Page", null, 50);
                break;
            }
            if((nextSlot+1)%9 == 0) nextSlot = nextSlot + 2;
            ownerRegionsGUI.addItem(Material.DIAMOND_BLOCK, "Inspect " + rg.getId(), null, nextSlot);
        }
        ownerRegionsGUI.addItem(Material.ARROW, "Back", null, totalSlots-5);
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
        if(numRegionsToSkip>0) ownerPlotsGUI.addItem(Material.PAPER, "Previous Page", null, 48);
        int loopCounter = 0;
        for(ProtectedRegion rg : ownerPlots) {
            if(loopCounter++ < numRegionsToSkip) continue;
            if(++nextSlot > 43) {
                ownerPlotsGUI.addItem(Material.PAPER, "Next Page", null, 50);
                break;
            }
            if((nextSlot+1)%9 == 0) nextSlot = nextSlot + 2;
            ownerPlotsGUI.addItem(Material.DIAMOND_ORE, "Inspect " + rg.getId(), null, nextSlot);
        }
        ownerPlotsGUI.addItem(Material.ARROW, "Back", null, totalSlots-5);
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
        if(numRegionsToSkip>0) memberRegionsGUI.addItem(Material.PAPER, "Previous Page", null, 48);
        int loopCounter = 0;
        for(ProtectedRegion rg : memberRegions) {
            if(loopCounter++ < numRegionsToSkip) continue;
            if(++nextSlot > 43) {
                memberRegionsGUI.addItem(Material.PAPER, "Next Page", null, 50);
                break;
            }
            if((nextSlot+1)%9 == 0) nextSlot = nextSlot + 2;
            memberRegionsGUI.addItem(Material.IRON_BLOCK, "Inspect " + rg.getId(), null, nextSlot);
        }
        memberRegionsGUI.addItem(Material.ARROW, "Back", null, totalSlots-5);
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
        if(numRegionsToSkip>0) memberPlotsGUI.addItem(Material.PAPER, "Previous Page", null, 48);
        int loopCounter = 0;
        for(ProtectedRegion rg : memberPlots) {
            if(loopCounter++ < numRegionsToSkip) continue;
            if(++nextSlot > 43) {
                memberPlotsGUI.addItem(Material.PAPER, "Next Page", null, 50);
                break;
            }
            if((nextSlot+1)%9 == 0) nextSlot = nextSlot + 2;
            memberPlotsGUI.addItem(Material.IRON_ORE, "Inspect " + rg.getId(), null, nextSlot);
        }
        memberPlotsGUI.addItem(Material.ARROW, "Back", null, totalSlots-5);
        memberPlotsGUI.open(player);
    }

    public void handleWandClick(Player player){

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
        int firstSlot = 0;
        ProtectedRegion region = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion(regionName);
        NGUI inspectorGUI = new NGUI(9, "LandClaim Inspector");
        inspectorGUI.addItem(Material.BIRCH_SIGN, regionName, createLore("Claim Volume: " + region.volume()), firstSlot++);
        inspectorGUI.addItem(Material.PLAYER_HEAD, "Add Player to Claim", null, firstSlot++);
        inspectorGUI.addItem(Material.STRUCTURE_VOID, "Remove " + regionName, null, firstSlot++);
        inspectorGUI.addItem(Material.ARROW, "Back", null, 8);
        inspectorGUI.open(player);
    }

    public void promptForRemoval(String playerName, String regionName){
        NGUI removalPrompt = new NGUI(36, "LandClaim Claim Removal");
        removalPrompt.addItem(Material.STRUCTURE_VOID, "Remove " + regionName + "?", createLore("Warning:", "This cannot be undone"), 13);
        removalPrompt.addItem(Material.ARROW, "Back", null, 31);
        removalPrompt.open(Bukkit.getPlayer(playerName));
    }

    public void openAddPlayer(String playerName, String regionName){
        NGUI addPlayer = new NGUI(36, "LandClaim Add Player");
        addPlayer.addItem(Material.WITHER_SKELETON_SKULL, "Add Owner to " + regionName, createLore("This will give the", "player full permissions", "on this claim"), 12);
        addPlayer.addItem(Material.SKELETON_SKULL, "Add Member to " + regionName, createLore("This will give the", "player partial permissions", "on this claim"), 14);
        addPlayer.addItem(Material.ARROW, "Back", null, 31);
        addPlayer.open(Bukkit.getPlayer(playerName));
    }
}
