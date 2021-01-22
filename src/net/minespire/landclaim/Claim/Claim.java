package net.minespire.landclaim.Claim;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.minespire.landclaim.LandClaim;
import org.bukkit.Bukkit;

import java.util.*;

public class Claim {

	private ProtectedRegion region;
	private BlockVector3 minPoint;
	private BlockVector3 maxPoint;
	private Player player;
	private World world;
	private RegionManager rgManager;
	private String rgName;
	private org.bukkit.entity.Player bukkitPlayer;
	private double claimCost;
	private int claimArea;
	private int claimVolume;
	private boolean isPlot = false;
	private boolean regionAlreadyExists = false;
	public static Map<String,List<String>> playerClaimsMap = new HashMap<>();
	public static Map<String, ProtectedRegion> awaitingRemovalConfirmation = new HashMap<>();
	
	public Claim(org.bukkit.entity.Player player, String rgName) {
		this.player = BukkitAdapter.adapt(player);
		this.bukkitPlayer = player;
		this.rgName = rgName;
	}
	
	public Claim(org.bukkit.entity.Player player, String rgName, boolean isPlot) {
		this.player = BukkitAdapter.adapt(player);
		this.bukkitPlayer = player;
		this.rgName = rgName;
		this.isPlot = isPlot;
	}
	
	public Claim(org.bukkit.entity.Player player, String rgName, String worldName) {
		this.player = BukkitAdapter.adapt(player);
		this.bukkitPlayer = player;
		this.rgName = rgName;
		this.world = BukkitAdapter.adapt(Bukkit.getWorld(worldName));
	}
	
	public boolean createClaim() {
		world = this.player.getWorld();
		rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
		
		if(rgManager == null) return false;
		Map<String, ProtectedRegion> regions = rgManager.getRegions();
		
		LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
		
		Region selection = null;
		try {
			selection = session.getSelection(this.player.getWorld());
		}
		catch(IncompleteRegionException e) {
			bukkitPlayer.sendMessage("You must select two points first!");
			return false;
		}

		minPoint = selection.getMinimumPoint();
		maxPoint = selection.getMaximumPoint();

		if (selection != null) {
			if(!isPlot) {
				minPoint = BlockVector3.at(minPoint.getX(), (player.getWorld().getMaxY() + 1), minPoint.getZ());
				maxPoint = BlockVector3.at(maxPoint.getX(), 0, maxPoint.getZ());
				region = new ProtectedCuboidRegion(rgName, minPoint, maxPoint);
			} else region = new ProtectedCuboidRegion(rgName, minPoint, maxPoint);
		}
		
		for(ProtectedRegion rg : regions.values()) {
			if(rg.getId().equals(region.getId())){
				bukkitPlayer.sendMessage("A region with that name already exists!");
				return false;
			}
		}

		if(!isPlot) region.setFlag(LandClaim.LandClaimRegionFlag, "region");
		else region.setFlag(LandClaim.LandClaimRegionFlag, "plot");
		
		calculateClaimArea();
		calculateClaimCost();
		return true;
	}

	public static void queueForRemoval(String playerName, String regionName){
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getPlayer(playerName).getWorld()));
		if(regions!=null) {
			ProtectedRegion region = regions.getRegion(regionName);
			awaitingRemovalConfirmation.put(playerName, region);
		}
	}

	public void deleteRegion() {
		rgManager.removeRegion(rgName);
	}
	
	public static Set<UUID> getRegionOwners(String regionName, String worldName) {
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(Bukkit.getWorld(worldName)));
		if(regions!=null) {
			ProtectedRegion region = regions.getRegion(regionName);
			DefaultDomain owners;
			if(region != null) {
				owners = region.getOwners();
				return owners.getUniqueIds();
			}
			return null;
		} else return null;
	}
	
	public void setNewOwner() {
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(world);
		if(regions!=null) {
			ProtectedRegion region = regions.getRegion(rgName);
			DefaultDomain owners = new DefaultDomain();
			owners.addPlayer(player.getUniqueId());
			region.setOwners(owners);
		}
	}
	
	public void saveClaim() {
		rgManager.addRegion(region);
		DefaultDomain owner = new DefaultDomain();
		owner.addPlayer(player.getUniqueId());
		region.setOwners(owner);
	}
	
	public boolean overlapsUnownedRegion() {
		Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
		for(ProtectedRegion rg : region.getIntersectingRegions(regionCollection)) {
			if(!rg.getOwners().contains(player.getUniqueId())) return true;

		}
		return false;
	}
	
	private void calculateClaimCost() {
		if(isPlot) {
			calculateClaimVolume();
			double tmpCost = LandClaim.plugin.getConfig().getDouble("Claims.Plots.PricePerBlock") * claimVolume; 
			double baseCost = LandClaim.plugin.getConfig().getDouble("Claims.Plots.BaseCost");
			claimCost =  tmpCost >= baseCost ? tmpCost : baseCost;
		} else claimCost = claimArea * LandClaim.plugin.getConfig().getDouble("Claims.Regions.PricePerBlock");
	}
	
	private void calculateClaimArea() {
		claimArea = (maxPoint.getX() - minPoint.getX() + 1) * (maxPoint.getZ() - minPoint.getZ() + 1);
	}
	
	private void calculateClaimVolume() {
		claimVolume = (maxPoint.getX() - minPoint.getX() + 1) * (maxPoint.getY() - minPoint.getY() + 1) * (maxPoint.getZ() - minPoint.getZ() + 1);
	}
	
	public int getClaimArea() {
		return claimArea;
	}
	
	public int getClaimVolume() {
		return claimVolume;
	}
	
	public int getClaimLength() {
		int side1 = Math.abs(maxPoint.getX() - minPoint.getX() + 1);
		int side2 = Math.abs(maxPoint.getZ() - minPoint.getZ() + 1);
		return side1 >= side2 ? side1 : side2; 
	}
	
	public int getClaimWidth() {
		int side1 = Math.abs(maxPoint.getX() - minPoint.getX() + 1);
		int side2 = Math.abs(maxPoint.getZ() - minPoint.getZ() + 1);
		return side1 >= side2 ? side2 : side1;
	}
	
	public int getClaimHeight() {
		return Math.abs(maxPoint.getY() - minPoint.getY() + 1);
	}
	
	public boolean insideOwnedRegion() {
		Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
		for(ProtectedRegion rg : region.getIntersectingRegions(regionCollection)) {
			if(rg.getOwners().contains(player.getUniqueId())) {
				if(rg.contains(minPoint.getX(), 30, minPoint.getZ()) &&
							rg.contains(minPoint.getX(), 30, maxPoint.getZ()) &&
							rg.contains(maxPoint.getX(), 30, minPoint.getZ()) &&
							rg.contains(maxPoint.getX(), 30, maxPoint.getZ())) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static int numOwnedRegions(org.bukkit.entity.Player player, boolean getPlots) {
		World world = BukkitAdapter.adapt(player.getWorld());
		RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
		Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
		int numRegions = 0;
		int numPlots = 0;
		for(ProtectedRegion rg : regionCollection) {
			if(rg.getOwners().contains(player.getUniqueId())) {
				if(rg.getFlag(LandClaim.LandClaimRegionFlag)!=null) {
					if(rg.getFlag(LandClaim.LandClaimRegionFlag).equals("plot")) numPlots++;
				} else numRegions++;
				
			}
		}
		if (getPlots) return numPlots;
		else return numRegions;
			
	}
	
	public static boolean regionIsPlot(org.bukkit.World bukkitWorld, String regionName) {
		World world = BukkitAdapter.adapt(bukkitWorld);
		RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
		Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
		for(ProtectedRegion rg : regionCollection) {
			if(rg.getId().equals(regionName)) {
				if(rg.getFlag(LandClaim.LandClaimRegionFlag)!=null) {
					if(rg.getFlag(LandClaim.LandClaimRegionFlag).equals("plot")) return true;
				} else return false;
				
			}
		}
		return false;
	}
	
	public static String playerIsOwnerOrMember(org.bukkit.entity.Player player, String regionName) {
		World world = BukkitAdapter.adapt(player.getWorld());
		RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
		Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
		for(ProtectedRegion rg : regionCollection) {
			if(rg.getOwners().contains(player.getUniqueId())) return "Owner";
			else if(rg.getMembers().contains(player.getUniqueId())) return "Member";
		}
		return null;
	}
	
	public static void removeRegion(org.bukkit.entity.Player player) {
		World world = BukkitAdapter.adapt(player.getWorld());
		RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
		String regionName = Claim.awaitingRemovalConfirmation.get(player.getDisplayName()).getId();
		rgManager.removeRegion(regionName);
		player.sendMessage("You removed your claim " + regionName);
	}

	public static void removeRegion(org.bukkit.entity.Player player, String regionName) {
		World world = BukkitAdapter.adapt(player.getWorld());
		RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
		rgManager.removeRegion(regionName);
		player.sendMessage("You removed your claim " + regionName);
	}
	
	public static String parsePlaceholders(String string, Claim claim) {
		string = string.replace("{RegionCost}",  String.format("$%.2f", claim.getClaimCost()));
		string = string.replace("{RegionSize}", String.valueOf(claim.getClaimArea()));
		string = string.replace("{RegionName}", claim.getRegionName());
		string = string.replace("{PlayerName}", claim.getPlayerName());
		string = string.replace("{RegionLength}", String.valueOf(claim.getClaimLength()));
		string = string.replace("{RegionWidth}", String.valueOf(claim.getClaimWidth()));
		string = string.replace("{RegionHeight}", String.valueOf(claim.getClaimHeight()));
		return string;
	}
	
	public static List<ProtectedRegion> getClaimListOwner(org.bukkit.entity.Player player, boolean getPlots) {
		World world = BukkitAdapter.adapt(player.getWorld());
		RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
		Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
		List<ProtectedRegion> claimPlotList = new ArrayList<>();
		List<ProtectedRegion> claimRegionList = new ArrayList<>();
		for(ProtectedRegion rg : regionCollection) {
			if(rg.getOwners().contains(player.getUniqueId())) {
				if(getPlots) {
					if(rg.getFlag(LandClaim.LandClaimRegionFlag)!=null) {
						if(rg.getFlag(LandClaim.LandClaimRegionFlag).equals("plot")) claimPlotList.add(rg);
					}
				} else {
					if(rg.getFlag(LandClaim.LandClaimRegionFlag)!=null) {
						if(rg.getFlag(LandClaim.LandClaimRegionFlag).equals("region")) claimRegionList.add(rg);
					}
				}
			}
		}
		if(getPlots) return claimPlotList;
		else return claimRegionList;
	}
	
	public static List<ProtectedRegion> getClaimListMember(org.bukkit.entity.Player player, boolean getPlots) {
		World world = BukkitAdapter.adapt(player.getWorld());
		RegionManager rgManager = LandClaim.wg.getPlatform().getRegionContainer().get(world);
		Collection<ProtectedRegion> regionCollection = rgManager.getRegions().values();
		List<ProtectedRegion> claimPlotList = new ArrayList<>();
		List<ProtectedRegion> claimRegionList = new ArrayList<>();
		for(ProtectedRegion rg : regionCollection) {
			if(rg.getMembers().contains(player.getUniqueId())) {
				if(getPlots) {
					if(rg.getFlag(LandClaim.LandClaimRegionFlag)!=null) {
						if(rg.getFlag(LandClaim.LandClaimRegionFlag).equals("plot")) claimPlotList.add(rg);
					}
				} else {
					if(rg.getFlag(LandClaim.LandClaimRegionFlag)!=null) {
						if(rg.getFlag(LandClaim.LandClaimRegionFlag).equals("region")) claimRegionList.add(rg);
					}
				}
			}
		}
		if(getPlots) return claimPlotList;
		else return claimRegionList;
	}
	
	public double getClaimCost() {
		return claimCost;
	}
	
	public String getPlayerName() {
		return player.getDisplayName();
	}
	
	public String getRegionName() {
		return rgName;
	}
	public boolean regionExists() {
		return regionAlreadyExists;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void setClaimAsPlot() {
		this.isPlot = true;
	}
	
	public boolean isPlot() {
		return isPlot;
	}
	
	public static void saveClaimToPlayerMap(org.bukkit.entity.Player player, String claim) {
		List<String> playerClaimList;
		if(!playerClaimsMap.containsKey(player.getUniqueId().toString())) {
			playerClaimList = new ArrayList<>();
			playerClaimList.add(claim);
			playerClaimsMap.put(player.getUniqueId().toString(), playerClaimList);
		} else {
			playerClaimList = playerClaimsMap.get(player.getUniqueId().toString());
			playerClaimList.add(claim);
			playerClaimsMap.put(player.getUniqueId().toString(), playerClaimList);
		}
	}

	public static boolean addOwner(Player checkIfOwner, String personToAdd, ProtectedRegion region){
		DefaultDomain regionOwners = region.getOwners();
		if(regionOwners.contains(checkIfOwner.getDisplayName())){
			regionOwners.addPlayer(personToAdd);
			region.setOwners(regionOwners);
			return true;
		} else return false;
	}

	public static boolean addMember(Player checkIfOwner, String personToAdd, ProtectedRegion region){
		DefaultDomain regionMembers = region.getMembers();
		if(region.getOwners().contains(checkIfOwner.getDisplayName())){
			regionMembers.addPlayer(personToAdd);
			region.setOwners(regionMembers);
			return true;
		} else return false;
	}
	
}
