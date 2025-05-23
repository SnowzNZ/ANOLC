package dev.snowz.anolc.gui;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.snowz.anolc.ANOLC;
import dev.snowz.anolc.claim.Claim;
import dev.snowz.anolc.claim.Claimer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public final class GUIManager {

    @Getter
    private static final GUIManager inst = new GUIManager();
    public static Map<String, Flag> editableClaimFlags = new LinkedHashMap<>();

    static {
        editableClaimFlags.put("build", Flags.BUILD);
        editableClaimFlags.put("interact", Flags.INTERACT);
        editableClaimFlags.put("block-break", Flags.BLOCK_BREAK);
        editableClaimFlags.put("block-place", Flags.BLOCK_PLACE);
        editableClaimFlags.put("use", Flags.USE);
        editableClaimFlags.put("damage-animals", Flags.DAMAGE_ANIMALS);
        editableClaimFlags.put("chest-access", Flags.CHEST_ACCESS);
        editableClaimFlags.put("ride", Flags.RIDE);
        editableClaimFlags.put("pvp", Flags.PVP);
        editableClaimFlags.put("sleep", Flags.SLEEP);
        editableClaimFlags.put("respawn-anchors", Flags.RESPAWN_ANCHORS);
        editableClaimFlags.put("tnt", Flags.TNT);
        editableClaimFlags.put("vehicle-place", Flags.PLACE_VEHICLE);
        editableClaimFlags.put("vehicle-destroy", Flags.DESTROY_VEHICLE);
        editableClaimFlags.put("lighter", Flags.LIGHTER);
        editableClaimFlags.put("block-trampling", Flags.TRAMPLE_BLOCKS);
        editableClaimFlags.put("entry", Flags.ENTRY);
        editableClaimFlags.put("entry-deny-message", Flags.ENTRY_DENY_MESSAGE);
        editableClaimFlags.put("greeting", Flags.GREET_MESSAGE);
        editableClaimFlags.put("greeting-title", Flags.GREET_TITLE);
        editableClaimFlags.put("farewell", Flags.FAREWELL_MESSAGE);
        editableClaimFlags.put("farewell-title", Flags.FAREWELL_TITLE);
        editableClaimFlags.put("enderpearl", Flags.ENDERPEARL);
        editableClaimFlags.put("chorus-fruit-teleport", Flags.CHORUS_TELEPORT);
        editableClaimFlags.put("item-pickup", Flags.ITEM_PICKUP);
        editableClaimFlags.put("item-drop", Flags.ITEM_DROP);
        editableClaimFlags.put("deny-message", Flags.DENY_MESSAGE);
    }

    public void openMainGUI(final Player player) {
        final NGUI mainGUI = new NGUI(9, "LandClaim Main Menu");
        mainGUI.addItem(
            Material.GRASS_BLOCK,
            colorize("&3Claims"),
            parseLoreString("&fView and edit your|&fregions and plots")
        );
        mainGUI.addItem(Material.WOODEN_AXE, colorize("&3Wand"), parseLoreString("&fGet a claim wand"));
//        mainGUI.addItem(Material.OBSERVER, colorize("&3Claim Limits"), parseLoreString("&fView your claim limits"));

        mainGUI.addItem(Material.BIRCH_DOOR, colorize("&6Close"), null, 8);
        mainGUI.open(player);
    }

    public void openClaimLimitsGUI(final Player player) {
        final NGUI claimLimitsGUI = new NGUI(36, "LandClaim Claim Limits");
        claimLimitsGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        claimLimitsGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
        final int numOwnedRegions;
        final int numOwnedPlots;
        final int numAllowedRegions;
        final int numAllowedPlots;
        numOwnedRegions = Claim.getClaimListOwner(player, false).size();
        numOwnedPlots = Claim.getClaimListOwner(player, true).size();
        numAllowedRegions = Claimer.getNumAllowedRegions(player);
        numAllowedPlots = Claimer.getNumAllowedPlots(player);
        final String[] claimableWorlds = Claimer.claimableWorlds(player).stream().map(world -> colorize("&9" + world.getName())).toList().toArray(
            new String[0]);
        claimLimitsGUI.addItem(
            Material.FILLED_MAP,
            colorize("&3Allowed Claim Worlds"),
            createLore(claimableWorlds),
            11
        );
        claimLimitsGUI.addItem(
            Material.GRASS_BLOCK,
            colorize("&3Regions"),
            parseLoreString("&fAllowed: " + numAllowedRegions + "|&fOwned: " + numOwnedRegions + "|&fRemaining: " + (numAllowedRegions - numOwnedRegions)),
            13
        );
        claimLimitsGUI.addItem(
            Material.DIRT,
            colorize("&3Plots"),
            parseLoreString("&fAllowed: " + numAllowedPlots + "|&fOwned: " + numOwnedPlots + "|&fRemaining: " + (numAllowedPlots - numOwnedPlots)),
            15
        );

        claimLimitsGUI.open(player);
    }

    public void openAllClaimsGUI(final Player player) {
        openAllClaimsGUI(player, 0);
    }

    public void openAllClaimsGUI(final Player player, final int numRegionsToSkip) {
        int nextSlot = 9;
        final List<String> allClaims = new LinkedList<>();
        final List<String> ownerRegions = Claim.getClaimListOwner(player, false);
        final List<String> ownerPlots = Claim.getClaimListOwner(player, true);
        Collections.sort(ownerRegions);
        Collections.sort(ownerPlots);
        allClaims.addAll(ownerRegions);
        allClaims.addAll(ownerPlots);

        final List<String> memberRegions = Claim.getClaimListMember(player, false);
        final List<String> memberPlots = Claim.getClaimListMember(player, true);

        memberRegions.removeIf(allClaims::contains);
        memberPlots.removeIf(allClaims::contains);

        Collections.sort(memberRegions);
        Collections.sort(memberPlots);
        allClaims.addAll(memberRegions);
        allClaims.addAll(memberPlots);


        int totalSlots = ((int) Math.ceil(allClaims.size() / 7d) + 2) * 9;
        if (totalSlots > 54) totalSlots = 54;

        final NGUI allClaimsGUI = new NGUI(totalSlots, "LandClaim Claims - Page " + (numRegionsToSkip / 28 + 1));
        if (numRegionsToSkip > 0) allClaimsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Previous Page", null, 48);
        int loopCounter = 0;
        for (final String rg : allClaims) {
            if (loopCounter++ < numRegionsToSkip) continue;
            if (++nextSlot > 43) {
                allClaimsGUI.addItem(Material.PAPER, ChatColor.GOLD + "Next Page", null, 50);
                break;
            }
            if ((nextSlot + 1) % 9 == 0) nextSlot = nextSlot + 2;

            final String ownerOrMember = Claim.playerIsOwnerOrMember(player, rg);
            if (ownerOrMember == null) {
                continue;
            }

            if (ownerOrMember.equalsIgnoreCase("Owner")) {
                if (!Claim.regionIsPlot(player, rg)) allClaimsGUI.addItem(
                    Material.DIAMOND_BLOCK,
                    colorize("&5" + rg),
                    parseLoreString(""),
                    nextSlot
                );
                else allClaimsGUI.addItem(
                    Material.DIAMOND_ORE,
                    colorize("&5" + rg),
                    parseLoreString(""),
                    nextSlot
                );
            } else if (ownerOrMember.equalsIgnoreCase("Member")) {
                if (!Claim.regionIsPlot(player, rg)) allClaimsGUI.addItem(
                    Material.IRON_BLOCK,
                    colorize("&5" + rg),
                    parseLoreString(""),
                    nextSlot
                );
                else allClaimsGUI.addItem(
                    Material.IRON_ORE,
                    colorize("&5" + rg),
                    parseLoreString(""),
                    nextSlot
                );
            }
        }
        allClaimsGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, totalSlots - 5);
        allClaimsGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, totalSlots - 3);
        allClaimsGUI.open(player);
    }

    public void handleWandClick(final Player player) {
        Bukkit.dispatchCommand(player, "/wand");
    }

    public List<String> createLore(final String... lorePieces) {
        final List<String> completedLore = new ArrayList<>();
        for (final String lore : lorePieces) {
            completedLore.add(ChatColor.translateAlternateColorCodes('&', lore));
        }
        return completedLore;
    }

    public List<String> parseLoreString(final String loreString) {
        final String[] loreArray = loreString.split("\\|");
        final List<String> loreList = new ArrayList<>();
        for (int x = 0; x < loreArray.length; x++) {
            loreList.add(x, ChatColor.translateAlternateColorCodes('&', loreArray[x]));
        }
        return loreList;
    }

    public static String colorize(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public void openClaimInspector(final Player player, final String regionName) {
        //ProtectedRegion region = LandClaim.wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion(regionName);
        final NGUI inspectorGUI = new NGUI(18, "LandClaim Inspector");

        inspectorGUI.addItem(Material.PLAYER_HEAD, colorize("&3Players"), null);
        if (player.hasPermission("landclaim.teleport") || player.hasPermission("landclaim.inspect.others"))
            inspectorGUI.addItem(Material.ENDER_PEARL, colorize("&3Teleport"), null);
        String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName);
        if (ownerOrMember == null) ownerOrMember = "";
        final boolean isOwner = ownerOrMember.equalsIgnoreCase("Owner");
        if ((isOwner && player.hasPermission("landclaim.flageditor")) || player.hasPermission("landclaim.inspect.others"))
            inspectorGUI.addItem(Material.CYAN_BANNER, colorize("&3Flag Editor"), null);
        if ((isOwner && player.hasPermission("landclaim.remove.own")) || player.hasPermission("landclaim.edit.others"))
            inspectorGUI.addItem(Material.STRUCTURE_VOID, colorize("&3Remove &5" + regionName), null);

        inspectorGUI.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            11
        );
        inspectorGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 13);
        inspectorGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 15);
        inspectorGUI.open(player);
    }

    public void openOwnersMembersEditor(final Player player, final String regionName) {
        int firstSlot = 0;
        final NGUI inspectorGUI = new NGUI(18, "Owners/Members Editor");

        inspectorGUI.addItem(Material.PLAYER_HEAD, colorize("&3View/Remove Players"), null, firstSlot++);
        final String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName);
        if ((ownerOrMember != null && ownerOrMember.equalsIgnoreCase("Owner") && player.hasPermission(
            "landclaim.addplayer")) || player.hasPermission("landclaim.edit.others"))
            inspectorGUI.addItem(Material.TOTEM_OF_UNDYING, colorize("&3Add Player to Claim"), null, firstSlot);

        inspectorGUI.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            11
        );
        inspectorGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 13);
        inspectorGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 15);
        inspectorGUI.open(player);
    }

    public void promptForRemoval(final String playerName, final String regionName) {
        final NGUI removalPrompt = new NGUI(36, "LandClaim Claim Removal");
        removalPrompt.addItem(
            Material.STRUCTURE_VOID,
            colorize("&3Remove &5" + regionName + "&3?"),
            parseLoreString("&cWarning:|&fThis will permanently|&fremove your claim"),
            13
        );

        removalPrompt.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            29
        );
        removalPrompt.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        removalPrompt.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
        removalPrompt.open(Bukkit.getPlayer(playerName));
    }

    public void openAddPlayer(final String playerName, final String regionName) {
        final NGUI addPlayer = new NGUI(36, "Add Player to Claim");
        addPlayer.addItem(
            Material.WITHER_SKELETON_SKULL,
            colorize("&3Add Owner to &5" + regionName),
            parseLoreString("&fThis will give the|&fplayer full permissions|&fon this claim"),
            12
        );
        addPlayer.addItem(
            Material.SKELETON_SKULL,
            colorize("&3Add Member to &5" + regionName),
            parseLoreString("&fThis will give the|&fplayer partial permissions|&fon this claim"),
            14
        );

        addPlayer.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            29
        );
        addPlayer.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        addPlayer.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
        addPlayer.open(Bukkit.getPlayer(playerName));
    }

    public void openPlayersEditor(final Player player, final String regionName) {
        final NGUI playersEditor = new NGUI(54, "View/Remove Players");
        final Set<UUID> owners = ANOLC.getWg().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion(
            regionName).getOwners().getPlayerDomain().getUniqueIds();
        for (final UUID uuid : owners) {
            playersEditor.addItem(
                Material.WITHER_SKELETON_SKULL,
                colorize("&b" + Bukkit.getOfflinePlayer(uuid).getName()),
                parseLoreString("&7UUID:" + uuid)
            );
        }
        final Set<UUID> members = ANOLC.getWg().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld())).getRegion(
            regionName).getMembers().getPlayerDomain().getUniqueIds();
        for (final UUID uuid : members) {
            playersEditor.addItem(
                Material.SKELETON_SKULL,
                colorize("&b" + Bukkit.getOfflinePlayer(uuid).getName()),
                parseLoreString("&7UUID:" + uuid)
            );
        }

        playersEditor.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            47
        );
        playersEditor.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 49);
        playersEditor.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 51);
        playersEditor.open(player);
    }

    public void openMemberRemover(
        final Player player,
        final String uuid,
        final String regionName
    ) {
        final NGUI removeMember = new NGUI(36, "Remove Member");
        removeMember.addItem(
            Material.BARRIER,
            colorize("&3Are you sure?"),
            parseLoreString("&fRemove Member: &b" + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName() + "|" + "&7UUID: " + uuid),
            13
        );

        removeMember.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            29
        );
        removeMember.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        removeMember.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
        removeMember.open(player);
    }

    public void openOwnerRemover(
        final Player player,
        final String uuid,
        final String regionName
    ) {
        final NGUI removeOwner = new NGUI(36, "Remove Owner");
        removeOwner.addItem(
            Material.BARRIER,
            ChatColor.RED + "Are you sure?",
            parseLoreString("&fRemove Owner: &b" + Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName() + "|" + "&7UUID: " + uuid),
            13
        );

        removeOwner.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            29
        );
        removeOwner.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        removeOwner.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
        removeOwner.open(player);
    }

    public void openTeleportGUI(final Player player, final String regionName) {
        final NGUI teleportGUI = new NGUI(36, "LandClaim Teleport");
        final String ownerOrMember = Claim.playerIsOwnerOrMember(player, regionName);
        if ((ownerOrMember != null && ownerOrMember.equalsIgnoreCase("Owner")) || player.hasPermission(
            "landclaim.edit.others")) teleportGUI.addItem(
            Material.FURNACE,
            colorize("&3Remove Teleport Point"),
            parseLoreString("&fRemove the teleport|&fpoint for &5" + regionName + "&f."),
            11
        );
        teleportGUI.addItem(Material.ENDER_PEARL, colorize("&3Teleport to &5" + regionName), null, 13);
        if ((ownerOrMember != null && ownerOrMember.equalsIgnoreCase("Owner")) || player.hasPermission(
            "landclaim.edit.others")) teleportGUI.addItem(
            Material.CRAFTING_TABLE,
            colorize("&3Set Teleport Point"),
            parseLoreString("&fSet the teleport|&fpoint for &5" + regionName + "&f to|&fyour current location."),
            15
        );
        teleportGUI.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            29
        );
        teleportGUI.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 31);
        teleportGUI.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 33);
        teleportGUI.open(player);
    }

    public void openFlagsGUI(final Player player, final String regionName) {
        final NGUI flagEditor = new NGUI(45, "LandClaim Flags");
        final ProtectedRegion region = Claim.getRegion(player, regionName);

        editableClaimFlags.forEach((flagName, flag) -> {
            if (player.hasPermission("landclaim.flag." + flagName)) {
                if (region.getFlags().containsKey(editableClaimFlags.get(flagName))) {
                    flagEditor.addItem(Material.LIME_BANNER, ChatColor.GOLD + flagName, null);
                } else flagEditor.addItem(Material.GRAY_BANNER, ChatColor.GOLD + flagName, null);
            }

        });

        flagEditor.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            38
        );
        flagEditor.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 40);
        flagEditor.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 42);
        flagEditor.open(player);
    }

    public void openStateFlagEditor(
        final Player player,
        final String regionName,
        final String flagName
    ) {
        final NGUI flagEditor = new NGUI(18, "LandClaim State Flag Editor");

        flagEditor.addItem(Material.DARK_OAK_SIGN, flagName, null, 0);

        final ProtectedRegion region = Claim.getRegion(player, regionName);

        if (region.getFlags().containsKey(editableClaimFlags.get(flagName))) {
            if (region.getFlag(editableClaimFlags.get(flagName)).toString().equalsIgnoreCase("ALLOW")) {
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Allow", null);
                flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Deny", null);
            } else {
                flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.GREEN + "Allow", null);
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.RED + "Deny", null);
            }

            flagEditor.addItem(Material.BARRIER, ChatColor.RED + "Delete Flag", null);
            final RegionGroup regionGroup = region.getFlag(editableClaimFlags.get(flagName).getRegionGroupFlag());

            if (regionGroup == null || regionGroup.equals(RegionGroup.ALL)) {
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for everyone", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for everyone", null);

            if (regionGroup != null && regionGroup.equals(RegionGroup.MEMBERS)) {
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for members", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for members", null);

            if (regionGroup != null && regionGroup.equals(RegionGroup.OWNERS)) {
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for owners", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for owners", null);

            if (regionGroup != null && regionGroup.equals(RegionGroup.NON_MEMBERS)) {
                flagEditor.addItem(Material.EMERALD_BLOCK, ChatColor.AQUA + "Set for non-members", null);
            } else flagEditor.addItem(Material.REDSTONE_BLOCK, ChatColor.AQUA + "Set for non-members", null);

            if (regionGroup != null && regionGroup.equals(RegionGroup.NON_OWNERS)) {
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


        flagEditor.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            11
        );
        flagEditor.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 13);
        flagEditor.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 15);
        flagEditor.open(player);
    }

    public void openStringFlagEditor(
        final Player player,
        final String regionName,
        final String flagName
    ) {
        final NGUI flagEditor = new NGUI(18, "LandClaim String Flag Editor");
        final List<String> currentFlagText = new ArrayList<>();
        final ProtectedRegion region = Claim.getRegion(player, regionName);
        if (region.getFlags().containsKey(editableClaimFlags.get(flagName))) {
            currentFlagText.add(colorize("&fCurrent Flag Text:"));
            final String[] flagTextTokens = region.getFlag(editableClaimFlags.get(flagName)).toString().split(" ");
            int countLineLength = 0;
            final StringBuilder stringBuilder = new StringBuilder(50);
            for (final String token : flagTextTokens) {
                countLineLength += token.length() + 1;
                stringBuilder.append(token).append(" ");
                if (countLineLength > 28) {
                    currentFlagText.add(stringBuilder.toString());
                    stringBuilder.setLength(0);
                    countLineLength = 0;
                }
            }
            if (!stringBuilder.isEmpty()) currentFlagText.add(stringBuilder.toString());
        }
        flagEditor.addItem(Material.DARK_OAK_SIGN, flagName, currentFlagText, 0);
        flagEditor.addItem(Material.WRITABLE_BOOK, colorize("&3Enter New Value"), null, 4);
        flagEditor.addItem(Material.BARRIER, ChatColor.RED + "Delete Flag", null, 8);


        flagEditor.addItem(
            Material.BIRCH_SIGN,
            colorize("&5" + regionName),
            parseLoreString(""),
            11
        );
        flagEditor.addItem(Material.ARROW, ChatColor.GOLD + "Back", null, 13);
        flagEditor.addItem(Material.BIRCH_DOOR, ChatColor.GOLD + "Close", null, 15);
        flagEditor.open(player);
    }

    private void doGlassPattern1(final NGUI topYearRegions) {
        for (int i = 0; i < 27; i++) {
            if (i == 10) i += 7;
            topYearRegions.addItem(Material.GLASS_PANE, " ", null, i);
        }
    }
}
