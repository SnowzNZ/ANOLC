package net.minespire.landclaim.Command;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.minespire.landclaim.Claim.*;
import net.minespire.landclaim.GUI.GUI;
import net.minespire.landclaim.GUI.GUIManager;
import net.minespire.landclaim.LandClaim;
import net.minespire.landclaim.Prompt.Prompt;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainCommand implements CommandExecutor {

	private Player player = null;
	private boolean isPlayer = false;
	private GUIManager guiManager = GUIManager.getInst();
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
        	player = (Player) sender;
        	isPlayer = true;
        }

        if(args.length < 1) {
			if(isPlayer) GUIManager.getInst().openMainGUI(player);
			return true;
		};
        try {
    		switch(args[0].toLowerCase()) {
				case "gui":
					if(isPlayer) GUIManager.getInst().openMainGUI(player);
					break;
				case "claim":
					if (player != null) {
						if(args[1]==null) return false;
						if(!Claimer.permissionToClaimRegion(player) && !player.isOp()) {
							player.sendMessage(ChatColor.GOLD + "You don't have permission to claim or have claimed too many regions!");
							return true;
						}
						if(!Claimer.permissionToClaimInWorld(player) && !player.isOp()) {
							player.sendMessage(ChatColor.GOLD + "You don't have permission to claim in this world!");
							return true;
						}
						if(!ProtectedRegion.isValidId(args[1])){
							player.sendMessage(ChatColor.GOLD + "That is not a valid region name.");
							return true;
						}

						Claim claim = new Claim(player, args[1]);
						if(!claim.createClaim()) return true;

						if(claim.overlapsUnownedRegion()) {
							player.sendMessage(ChatColor.GOLD + "Your selection overlaps a region belonging to someone else!");
							return true;
						}
						if(!meetsClaimLimits(claim)) return true;
						if(LandClaim.econ.getBalance(player) < claim.getClaimCost()) {
							player.sendMessage(ChatColor.GOLD + "You don't have enough money to claim this region for $" + claim.getClaimCost());
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

						gui.openGUI();
					} else sender.sendMessage("You must be a player to use that command!");
					break;
				case "claimplot":
					if (player != null) {
						if(args[1]==null) return false;
						if(!Claimer.permissionToClaimPlot(player) && !player.isOp()) {
							player.sendMessage(ChatColor.GOLD + "You don't have permission to claim or have claimed too many plots.");
							return true;
						}
						if(!Claimer.permissionToClaimInWorld(player) && !player.isOp()) {
							player.sendMessage(ChatColor.GOLD + "You don't have permission to claim in this world.");
							return true;
						}

						if(!ProtectedRegion.isValidId(args[1])){
							player.sendMessage(ChatColor.GOLD + "That is not a valid plot name.");
							return true;
						}

						Claim claim = new Claim(player, args[1], true);
						if(!claim.createClaim()) return true;

						if(!claim.insideOwnedRegion()) {
							player.sendMessage(ChatColor.GOLD + "You may only claim a plot inside a region that you own.");
							return true;
						}
						if(claim.overlapsUnownedRegion()) {
							player.sendMessage(ChatColor.GOLD + "Your selection overlaps a region belonging to someone else.");
							return true;
						}
						if(LandClaim.econ.getBalance(player)<claim.getClaimCost()) {
							player.sendMessage(ChatColor.GOLD + "You don't have enough money to claim this region for $" + claim.getClaimCost());
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

						gui.openGUI();
					} else sender.sendMessage("You must be a player to use that command!");
					break;
				case "reload":
					if((sender instanceof Player) && !player.hasPermission("landclaim.reload") && !player.isOp()) {
						sender.sendMessage(ChatColor.GOLD + "You don't have permission to do that!");
						return true;
					}
					LandClaim.plugin.reloadConfig();
					LandClaim.plugin.getCommand("lc").setTabCompleter(new CommandCompleter());
					LandClaim.plugin.getCommand("lc").setExecutor(new MainCommand());
					sender.sendMessage(ChatColor.GOLD + "LandClaim config reloaded!");
					break;
				case "cancel":
					if((sender instanceof Player) && !player.hasPermission("landclaim.cancel") && !player.isOp()) {
						sender.sendMessage(ChatColor.GOLD + "You don't have permission to do that!");
						return true;
					}
					if(Prompt.hasActivePrompt(player)){
						Prompt.getPrompt(player.getDisplayName()).cancelPrompt();
					}
					break;
				case "remove":
					if(!(sender instanceof Player)) {
						sender.sendMessage("You must be a player to do that.");
						return true;
					}
					if(!player.hasPermission("landclaim.remove") && !player.isOp()) {
						sender.sendMessage(ChatColor.GOLD + "You don't have permission to do that!");
						return true;
					}
					if(args.length < 2) {
						player.sendMessage(ChatColor.GOLD + "You need to specify the name of the claim you want to remove");
						return true;
					}
					Set<UUID> regionOwners = Claim.getRegionOwners(args[1], player.getWorld().getName());
					if(regionOwners != null && regionOwners.contains(player.getUniqueId())) {
						//Claim.queueForRemoval(player.getName(), args[1]);
						//GUI.promptForRemoval(player.getDisplayName());
						guiManager.promptForRemoval(player.getDisplayName(), args[1]);
						return true;
					} else player.sendMessage(ChatColor.GOLD + "You don't own that region.");
					break;
				case "upvote":
					if(!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.GOLD + "You must be a player to do that.");
						return true;
					}
					if(!player.hasPermission("landclaim.upvote") && !player.isOp()) {
						sender.sendMessage(ChatColor.GOLD + "You don't have permission to do that!");
						return true;
					}
					if(args.length < 2) {
						List<ProtectedRegion> regionsList = Claims.getRegionsAtLocation(player.getLocation());
						if(regionsList.size() == 0) {
							player.sendMessage(ChatColor.GOLD + "You are not standing in any regions. /lc upvote [REGION] to upvote a region.");
							return true;
						}
						if(regionsList.size() == 1) {
							VoteFile voteFile = VoteFile.get();
							Vote lastVote = voteFile.getLatestVote(regionsList.get(0).getId(), player.getWorld().getName(), player.getUniqueId().toString());
							if(lastVote != null && !lastVote.dayHasPassed()){
								player.sendMessage(ChatColor.GOLD + "You must wait " + (1-lastVote.daysSinceLastVote()) + " days before voting again");
								return true;
							}
							VoteFile.get().addVote(regionsList.get(0).getId(), player.getWorld().getName(), player.getUniqueId().toString()).save();
							player.sendMessage(ChatColor.GOLD + "Vote registered successfully!");
							Votes.tallyVotes();
							return true;
						}
						if(regionsList.size() > 1) {
							String message = ChatColor.GOLD + "You are standing in regions: ";
							StringBuilder extraMessage = new StringBuilder();
							for(ProtectedRegion region : regionsList){
								extraMessage.append(region.getId()).append(", ");
							}
							extraMessage.delete(extraMessage.length()-2, extraMessage.length());
							player.sendMessage(message + extraMessage + ".");
							player.sendMessage(ChatColor.GOLD + "/lc upvote [REGION] to pick which region");
						}
						return true;
					} else {
						ProtectedRegion region = Claims.getRegionByName(args[1], BukkitAdapter.adapt(player.getWorld()));
						if(region != null) {
							VoteFile voteFile = VoteFile.get();
							Vote lastVote = voteFile.getLatestVote(region.getId(), player.getWorld().getName(), player.getUniqueId().toString());
							if(lastVote != null && !lastVote.dayHasPassed()){
								player.sendMessage(ChatColor.GOLD + "You must wait " + (1-lastVote.daysSinceLastVote()) + " days before voting again");
								return true;
							}
							VoteFile.get().addVote(region.getId(), player.getWorld().getName(), player.getUniqueId().toString()).save();
							player.sendMessage(ChatColor.GOLD + "Vote registered successfully!");
							Votes.tallyVotes();
							return true;
						} else player.sendMessage(ChatColor.GOLD + "That is not a valid region.");
					}
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
				player.sendMessage(ChatColor.GOLD + "Your selection is too small!");
				return false;
			} else if (area>LandClaim.plugin.getConfig().getInt("Claims.Regions.MaxSize")) {
				player.sendMessage(ChatColor.GOLD + "Your selection is too large!");
				return false;
			} else if ((length/width)>LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxLWRatio") || (width/length)>LandClaim.plugin.getConfig().getDouble("Claims.Regions.MaxLWRatio")) {
				player.sendMessage(ChatColor.GOLD + "Your selection doesn't meet proportion requirements!");
				return false;
			} else return true;
		}

	}
	
}