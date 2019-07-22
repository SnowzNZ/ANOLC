package net.minespire.landclaim;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;


public class MainCommand implements CommandExecutor {

	private Player player = null;
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
        if (sender instanceof Player) {
        	player = (Player) sender;
        }
            
            
            
        
            
        try {
    		switch(args[0].toLowerCase()) {
    		case "claim": 
    			if (player != null) {
    				if(args[1]==null) return false;
    				if(!Claimer.permissionToClaimRegion(player)) {
    					player.sendMessage("You don't have permission to claim or have claimed too many regions!");
    					return true;
    				}
                    Claim claim = new Claim(player, args[1]);
                    if(!claim.createClaim()) return true;
                    if(claim.overlapsUnownedRegion()) {
                    	player.sendMessage("Your selection overlaps a region belonging to someone else!");
                    	return true;
                    }
                    if(!meetsClaimLimits(claim)) return true;
                    if(LandClaim.econ.getBalance(player)<claim.getClaimCost()) {
                    	player.sendMessage("You don't have enough money to claim this region for $" + claim.getClaimCost());
                    	return true;
                    }
                    LandClaim.claimMap.put(player.getUniqueId().toString(), claim);
            		GUI gui = new GUI(LandClaim.plugin.getConfig().getInt("GUI.Slots"));
            		gui.setInventory(LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.Title"));
            		GUI.GUIItem claimItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.ClaimItem.Material")));
            		gui.setPlayer(player);
            		gui.addGUIItem(claimItem.setDisplayName(Claim.parsePlaceholders(LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.ClaimItem.ItemName"), claim))
            				.setLore(Claim.parsePlaceholders(LandClaim.plugin.getConfig().getString("GUI.ClaimRegion.ClaimItem.Lore"), claim))
            				.setSlot(LandClaim.plugin.getConfig().getInt("GUI.ClaimRegion.ClaimItem.Slot")-1)
            				.setMeta());
            		
            		int slotsToFill = gui.getNumSlots()-gui.getNumGUIItems();
            		for(int x = 0; x < slotsToFill; x++) {
            			gui.addGUIItem(gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.FillerItem"))));
            		}
                    gui.openGUI();
    			} else sender.sendMessage("You must be a player to use that command!");
    			break;
    		case "claimplot": 
    			if (player != null) {
    				if(args[1]==null) return false;
    				if(!Claimer.permissionToClaimPlot(player)) {
    					player.sendMessage("You don't have permission to claim or have claimed too many plots!");
    					return true;
    				}
                    Claim claim = new Claim(player, args[1], true);

                    if(!claim.createClaim()) return true;
                    if(!claim.insideOwnedRegion()) {
                    	player.sendMessage("You may only claim a plot inside a region that you own!");
                    	return true;
                    }
                    if(claim.overlapsUnownedRegion()) {
                    	player.sendMessage("Your selection overlaps a region belonging to someone else!");
                    	return true;
                    }
                    if(LandClaim.econ.getBalance(player)<claim.getClaimCost()) {
                    	player.sendMessage("You don't have enough money to claim this region for $" + claim.getClaimCost());
                    	return true;
                    }
                    LandClaim.claimMap.put(player.getUniqueId().toString(), claim);
            		GUI gui = new GUI(LandClaim.plugin.getConfig().getInt("GUI.Slots"));
            		gui.setInventory(LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.Title"));
            		GUI.GUIItem claimItem = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.ClaimItem.Material")));
            		gui.setPlayer(player);
            		gui.addGUIItem(claimItem.setDisplayName(Claim.parsePlaceholders(LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.ClaimItem.ItemName"), claim))
            				.setLore(Claim.parsePlaceholders(LandClaim.plugin.getConfig().getString("GUI.ClaimPlot.ClaimItem.Lore"), claim))
            				.setSlot(LandClaim.plugin.getConfig().getInt("GUI.ClaimPlot.ClaimItem.Slot")-1)
            				.setMeta());
            		
            		int slotsToFill = gui.getNumSlots()-gui.getNumGUIItems();
            		for(int x = 0; x < slotsToFill; x++) {
            			gui.addGUIItem(gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.FillerItem"))));
            		}
                    gui.openGUI();
    			} else sender.sendMessage("You must be a player to use that command!");
    			break;
    		case "list": 
    			if (player != null) {
    				if(!player.hasPermission("landclaim.list")) return true;
    				
            		GUI gui = new GUI(LandClaim.plugin.getConfig().getInt("GUI.Slots"));
            		gui.setInventory(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.Title"));
            		gui.setPlayer(player);
            		GUI.GUIItem ownerRegions = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerRegions.Material")));
            		GUI.GUIItem memberRegions = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberRegions.Material")));
            		GUI.GUIItem ownerPlots = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerPlots.Material")));
            		GUI.GUIItem memberPlots = gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberPlots.Material")));
            		
            		List<String> regionList = new ArrayList<>();
            		
            		for(ProtectedRegion rg : Claim.getClaimListOwner(player, false)) {
            			regionList.add(ChatColor.DARK_AQUA + rg.getId() + ChatColor.WHITE + rg.getMinimumPoint().toString() + rg.getMaximumPoint().toString());
            		}
            		gui.addGUIItem(ownerRegions.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerRegions.ItemName"))
            				.setLore(regionList).setMeta().setSlot(LandClaim.plugin.getConfig().getInt("GUI.ClaimsList.OwnerRegions.Slot")-1));
            		regionList.clear();
            		
            		for(ProtectedRegion rg : Claim.getClaimListMember(player, false)) {
            			regionList.add(ChatColor.DARK_AQUA + rg.getId() + ChatColor.WHITE + rg.getMinimumPoint().toString() + rg.getMaximumPoint().toString());
            		}
            		gui.addGUIItem(memberRegions.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberRegions.ItemName"))
            				.setLore(regionList).setMeta().setSlot(LandClaim.plugin.getConfig().getInt("GUI.ClaimsList.MemberRegions.Slot")-1));
            		regionList.clear();
            		
            		for(ProtectedRegion rg : Claim.getClaimListOwner(player, true)) {
            			regionList.add(ChatColor.DARK_AQUA + rg.getId() + ChatColor.WHITE + rg.getMinimumPoint().toString() + rg.getMaximumPoint().toString());
            		}
            		gui.addGUIItem(ownerPlots.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.OwnerPlots.ItemName"))
            				.setLore(regionList).setMeta().setSlot(LandClaim.plugin.getConfig().getInt("GUI.ClaimsList.OwnerPlots.Slot")-1));
            		regionList.clear();
            		
            		for(ProtectedRegion rg : Claim.getClaimListMember(player, true)) {
            			regionList.add(ChatColor.DARK_AQUA + rg.getId() + ChatColor.WHITE + rg.getMinimumPoint().toString() + rg.getMaximumPoint().toString());
            		}
            		gui.addGUIItem(memberPlots.setDisplayName(LandClaim.plugin.getConfig().getString("GUI.ClaimsList.MemberPlots.ItemName"))
            				.setLore(regionList).setMeta().setSlot(LandClaim.plugin.getConfig().getInt("GUI.ClaimsList.MemberPlots.Slot")-1));
            		
            		int slotsToFill = gui.getNumSlots()-gui.getNumGUIItems();
            		for(int x = 0; x < slotsToFill; x++) {
            			gui.addGUIItem(gui.new GUIItem(Material.getMaterial(LandClaim.plugin.getConfig().getString("GUI.FillerItem"))));
            		}
                    gui.openGUI();
    			} else sender.sendMessage("You must be a player to use that command!");
    			break;
			case "reload": 
				LandClaim.plugin.reloadConfig();
				LandClaim.plugin.getCommand("lc").setTabCompleter(new CommandCompleter());
				LandClaim.plugin.getCommand("lc").setExecutor(new MainCommand());
				sender.sendMessage("LandClaim config reloaded!");
				break;
    		/*
    		case "blankdeed": 
    			if (player != null) {
            		player.getInventory().addItem(DeedListener.getBlankDeed());
    			} else sender.sendMessage("You must be a player to use that command!");
    			break;*/
    		default: 
    			return false;
    		}
        } catch(IndexOutOfBoundsException e) {
        	return false;
        }
            
        return true;
	}
	
	public boolean meetsClaimLimits(Claim claim) {
		if(claim.isPlot()) {
			return true;
		} else {
			int area = claim.getClaimArea();
			int length = claim.getClaimLength();
			int width = claim.getClaimWidth();
			if(area<LandClaim.plugin.getConfig().getInt("Claims.Regions.MinSize")) {
				player.sendMessage("Your selection is too small!");
				return false;
			} else if (area>LandClaim.plugin.getConfig().getInt("Claims.Regions.MaxSize")) {
				player.sendMessage("Your selection is too large!");
				return false;
			} else if ((length/width)>LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxLWRatio") || (width/length)>LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxLWRatio")) {
				player.sendMessage("Your selection doesn't meet proportion requirements!");
				return false;
			} else return true;
		}

	}

	
}