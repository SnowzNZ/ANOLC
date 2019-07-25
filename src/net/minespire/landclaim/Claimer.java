package net.minespire.landclaim;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class Claimer {

	
	public static boolean permissionToClaimRegion(Player player) {
		if(player.hasPermission("landclaim.claimregion")) {
			if(player.hasPermission("landclaim.regions.*")) return true;
        	int numRegionsAllowed = 0;
        	Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
        	for(PermissionAttachmentInfo perm : playerPermissions) {
        		if(perm.getPermission().startsWith("landclaim.regions.")) {
        			int permRegions = Integer.valueOf(perm.getPermission().substring(18));
        			numRegionsAllowed = permRegions > numRegionsAllowed ? permRegions : numRegionsAllowed;	
        		}
        		
        	}
        	if (Claim.numOwnedRegions(player, false)>=numRegionsAllowed) {
        		return false;
        	} else return true;
		} else return false;
		
	}
	
	
	public static boolean permissionToClaimPlot(Player player) {
		if(player.hasPermission("landclaim.claimplot")) {
			if(player.hasPermission("landclaim.plots.*")) return true;
        	int numPlotsAllowed = 0;
        	Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
        	for(PermissionAttachmentInfo perm : playerPermissions) {
        		if(perm.getPermission().startsWith("landclaim.plots.")) {
        			int permPlots = Integer.valueOf(perm.getPermission().substring(16));
        			numPlotsAllowed = permPlots > numPlotsAllowed ? permPlots : numPlotsAllowed;	
        		}
        		
        	}
        	if (Claim.numOwnedRegions(player, true)>=numPlotsAllowed) {
        		return false;
        	} else return true;
		} else return false;
		
	}
	
	public static boolean permissionToClaimInWorld(Player player) {
		if(player.hasPermission("landclaim.world.*")) return true;
    	Set<PermissionAttachmentInfo> playerPermissions = player.getEffectivePermissions();
    	for(PermissionAttachmentInfo perm : playerPermissions) {
    		if(perm.getPermission().startsWith("landclaim.world.")) {
    			String worldPerm = perm.getPermission().substring(16);
    			if(worldPerm.equalsIgnoreCase((player.getWorld().getName().toString()))) {
    				return true;
    			}
    		}	
    	}
    	return false;
	}
}
