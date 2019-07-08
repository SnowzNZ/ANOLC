package net.minespire.landclaim;



import java.util.Map;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class Claim {

	private ProtectedRegion region;
	private BlockVector3 minPoint;
	private BlockVector3 maxPoint;
	private Player player;
	RegionSelector rgSelector;
	RegionContainer container;
	RegionManager rgManager;
	private String rgName;
	private org.bukkit.entity.Player bukkitPlayer;
	private double claimCost;
	private int claimArea;
	
	public Claim(org.bukkit.entity.Player player, String rgName) {
		this.player = BukkitAdapter.adapt(player);
		this.bukkitPlayer = player;
		this.rgName = rgName;
	}
	
	public void createClaim() {
		container = LandClaim.wg.getPlatform().getRegionContainer();
		rgManager = container.get(this.player.getWorld());
		if(rgManager == null) return;
		Map<String, ProtectedRegion> regions = rgManager.getRegions();
		
		SessionManager sessionManager = new SessionManager(LandClaim.we);
		LocalSession session = sessionManager.get(this.player);
		Region selection = null;
		try {
			selection = session.getSelection(this.player.getWorld());
			
		}
		catch(IncompleteRegionException e) {
			bukkitPlayer.sendMessage("You must select two points first!");
		}
		
		if (selection != null) {
			minPoint = selection.getMinimumPoint();
			maxPoint = selection.getMaximumPoint();
			region = new ProtectedCuboidRegion(rgName, minPoint, maxPoint);
		}
		
		calculateClaimArea();
		calculateClaimCost();
		
		for(ProtectedRegion rg : regions.values()) {
			if(rg.getId().equals(region.getId())){
				bukkitPlayer.sendMessage("There is already a region with that name!");
				break;
			}
		}
	}
	
	public void saveClaim() {
		rgManager.addRegion(region);
	}
	
	private void calculateClaimCost() {
		claimCost = claimArea * 1.5D;
	}
	
	private void calculateClaimArea() {
		claimArea = (maxPoint.getX() - minPoint.getX()) * (maxPoint.getZ() - minPoint.getZ());
	}
	
	public int getClaimArea() {
		return claimArea;
	}
	
	public double getClaimCost() {
		return claimCost;
	}
	
}
