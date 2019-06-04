package net.minespire.landclaim;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.HashMap;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import com.sk89q.worldedit.world.World;
import org.bukkit.command.ConsoleCommandSender;
//import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CreateRegions
implements Listener {
    WorldEditPlugin worldEdit = (WorldEditPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
    WorldGuardPlugin worldGuard = (WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
    LandClaim plugin;
    private static HashMap<String, String> rgName = new HashMap<String, String>();
    private static HashMap<String, Boolean> timedOut = new HashMap<String, Boolean>();
    private static HashMap<String, Double> adjustedTownCost = new HashMap<String, Double>();
    private static HashMap<String, Integer> regionWidth = new HashMap<String, Integer>();
    private static HashMap<String, Integer> regionLength = new HashMap<String, Integer>();
    private static HashMap<String, ProtectedRegion> region = new HashMap<String, ProtectedRegion>();
    private static HashMap<String, World> world = new HashMap<String, World>();
    private static HashMap<String, RegionManager> regionManager = new HashMap<String, RegionManager>();
    private static HashMap<String, Region> WERegion = new HashMap<String, Region>();
    private int maxRegionWidth = 1000;
    private int minRegionWidth = 10;
    private int maxPlotWidth = 30;
    private double townCost = 1250.0;
    private double plotCost = 500.0;
    private Player player;
    private String rgname;
    ProtectedRegion existingRg;
    
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    public Vector minPoint;
    public Vector maxPoint;
    BlockVector min;
    BlockVector max;
    ProtectedRegion newRegion;

    public CreateRegions(LandClaim LandClaim) {
        this.plugin = LandClaim;
    }

    public double calculateTownCost(double townCost, int width, int length) {
        if (width * length < 2500) {
        	return townCost;
        }
        else {
        	return (double)(width * length) * 0.5;
        }
    }
        

    
    
    
    
    
    
    
    /*====================================================================================================================================
    									Monitors for commands to find any commands starting with
    									/rg claim /region claim /rg claimplot or /region claimplot
    ======================================================================================================================================
    */
    @EventHandler(priority=EventPriority.MONITOR)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage();
    	if (!(msg.startsWith("/rg claim") || msg.startsWith("/region claim") || msg.startsWith("/rg claimplot") || msg.startsWith("/region claimplot"))) return;
        
    	//cancels the /rg event so that LandClaim can handle it instead
        event.setCancelled(true);
    	//Region WERegion;
    	boolean rgIsPlot = true;
    	//World world;
    	//Get player and wraps it
        //event.getPlayer() = event.getPlayer();
    	
    	//New lines for updated region selection process
    	player = BukkitAdapter.adapt(event.getPlayer());
    	String playerName = player.getName();
        
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
        
        LocalPlayer localPlayer = this.worldGuard.wrapPlayer(event.getPlayer());
        
        //Searches the timedOut hashmap. If player previously timed out, reset timedOut value to false
        if (timedOut.containsKey(playerName) && timedOut.get(playerName).booleanValue()) {
            this.removePlayerFromHashmaps(playerName);
            timedOut.replace(playerName, false);
        }
        
        //stores the command that was typed
 
        
        //If command sender isn't a player, return
        if (!(player instanceof Player)) {
            event.getPlayer().sendRawMessage("You can't do that.");
            return;
        }
        
        //parse command using spaces into an array of strings
        String[] cmdTokens = msg.split(" ");
        

        
        //==============================================================================================================================
        //                                                   Plots
        //==============================================================================================================================
        if (msg.startsWith("/region claimplot") || msg.startsWith("/rg claimplot")) {
        	
        	
        	//If enablePlots = false in config, return
            if (!LandClaim.getEnablePlots()) {
                return;
            }
            
            //If player does not have the permission LandClaim.claimplot, return
            if (!event.getPlayer().hasPermission("LandClaim.claimplot")) {
                return;
            }
            
            //Checks and stores whether player has permission to bypass
            boolean bypassed = event.getPlayer().hasPermission("LandClaim.bypass.claimplot");
            
            

            
            //Gets balance of player
            double balance = LandClaim.econ.getBalance((OfflinePlayer)event.getPlayer());
            
            //If bal too low and player does not have permission to bypass, return
            if (balance < this.plotCost && !bypassed) {
            	event.getPlayer().sendRawMessage((Object)ChatColor.RED + "You need $" + this.plotCost + " to create a plot!");
                return;
            }
            
            //If there are less than 3 indices in array, player typed command incorrectly
            if (cmdTokens.length < 3) {
            	event.getPlayer().sendRawMessage((Object)ChatColor.GOLD + "Usage: /region claimplot <regionname>");
                return;
            }
            
            if (!cmdTokens[2].matches("[a-zA-Z0-9]+")) {
            	event.getPlayer().sendMessage("Please use only alphanumeric characters in your region name");
            	return;
            }
            
            /*
            *  If code reaches this point, the following lines run to create the region using worldedit and worldguard
            */
            
            //Puts player name and region name into hash map rgName
            rgName.put(playerName, msg.split(" ")[2]);
            
            
            world.put(playerName, player.getWorld());
            //regionManager =  WorldGuard.getInstance().getPlatform().getRegionContainer().get(world.get(playerName));
            //this.regionManager = this.worldGuard.getRegionManager(event.getPlayer().getWorld());
            regionManager.put(playerName,WorldGuard.getInstance().getPlatform().getRegionContainer().get(world.get(playerName)));
            try{
            	WERegion.put(playerName,session.getSelection(world.get(playerName)));
            }
            catch (IncompleteRegionException e)
            {
            	event.getPlayer().sendMessage("Make a selection first!");
            	return;
            }
            
            
            /*new code start
            ProtectedCuboidRegion tempRegion = new ProtectedCuboidRegion(
                    "GP_TEMP",
                    new BlockVector(lesserCorner.getX(), 0, lesserCorner.getZ()),
                    new BlockVector(greaterCorner.getX(), world.getMaxY(), greaterCorner.getZ()));

            ApplicableRegionSet overlaps = manager.getApplicableRegions(tempRegion);
            for (ProtectedRegion r : overlaps.getRegions()) {
                if (!manager.getApplicableRegions(r).testState(localPlayer, Flags.BUILD)) {
                    return false;
                }
            }
            */
            
            
            if (WERegion.get(playerName) != null) {
                ProtectedRegion existingRg = regionManager.get(playerName).getRegion(rgName.get(playerName));
                if (existingRg == null) {
                    //Location loc1 = selection.getMinimumPoint();
                    //Location loc2 = selection.getMaximumPoint();
                    this.minPoint = WERegion.get(playerName).getMinimumPoint();
                    this.maxPoint = WERegion.get(playerName).getMaximumPoint();
                    if (!(WERegion.get(playerName).getWidth() <= this.maxPlotWidth && WERegion.get(playerName).getLength() <= this.maxPlotWidth || bypassed)) {
                        String dimens = String.valueOf(this.maxPlotWidth) + "x" + this.maxPlotWidth;
                        event.getPlayer().sendRawMessage((Object)ChatColor.RED + "Plots are limited to " + dimens + ". Decrease the width/length of your selection.");
                        return;
                    }
                    ApplicableRegionSet regions = regionManager.get(playerName).getApplicableRegions(minPoint);
                    ApplicableRegionSet regions2 = regionManager.get(playerName).getApplicableRegions(maxPoint);
                    if (regions.size() < 1 || regions2.size() < 1) {
                    	event.getPlayer().sendRawMessage((Object)ChatColor.RED + "You can only make plots inside a region you own.");
                        return;
                    }
                    this.min = new BlockVector(this.minPoint.getBlockX(), 0, this.minPoint.getBlockZ());
                    this.max = new BlockVector(this.maxPoint.getBlockX(), 255, this.maxPoint.getBlockZ());
                    this.newRegion = new ProtectedCuboidRegion(rgName.get(playerName), this.min, this.max);
                    region.put(playerName, this.newRegion);
                    if (regionManager.get(playerName).overlapsUnownedRegion(region.get(playerName), localPlayer)) {
                    	event.getPlayer().sendRawMessage((Object)ChatColor.RED + "You can only make plots inside a region you own.");
                        this.removePlayerFromHashmaps(playerName);
                        return;
                    }
                    this.createNewRegion(player, rgIsPlot);
                } else {
                    event.getPlayer().sendRawMessage((Object)ChatColor.RED + "A region by that name already exists! Pick a different name.");
                    this.removePlayerFromHashmaps(playerName);
                }
            } else {
                event.getPlayer().sendRawMessage((Object)ChatColor.RED + "You need to select points first!");
                this.removePlayerFromHashmaps(playerName);
            }
          //==============================================================================================================================
          //                                                   Claims
          //==============================================================================================================================
        } else if (msg.startsWith("/region claim") || msg.startsWith("/rg claim")) {
            if (!event.getPlayer().hasPermission("LandClaim.claimregion")) {
                return;
            }
            if (event.getPlayer().hasPermission("LandClaim.bypass.claim")) {
                return;
            }
            
            //If there are less than 3 indices in array, player typed command incorrectly
            if (cmdTokens.length < 3) {
            	event.getPlayer().sendRawMessage((Object)ChatColor.GOLD + "Usage: /region claim <regionname>");
                return;
            }
            
            if (!cmdTokens[2].matches("[a-zA-Z0-9]+")) {
            	event.getPlayer().sendMessage("Please use only alphanumeric characters in your region name");
            	return;
            }
            
            if (rgName.containsKey(playerName)) {
                rgName.replace(playerName, msg.split(" ")[2]);
            } else {
                rgName.put(playerName, msg.split(" ")[2]);
            }
            
            world.put(playerName, player.getWorld());
            regionManager.put(playerName,WorldGuard.getInstance().getPlatform().getRegionContainer().get(world.get(playerName)));
            try{
            	WERegion.put(playerName,session.getSelection(world.get(playerName)));
            }
            catch (IncompleteRegionException e)
            {
            	event.getPlayer().sendMessage("Make a selection first!");
            	return;
            }
            
            //Old way of getting selection
            //Selection selection = this.worldEdit.getSelection(event.getPlayer());
            if (WERegion != null) {
                regionWidth.put(playerName, WERegion.get(playerName).getWidth());
                regionLength.put(playerName, WERegion.get(playerName).getLength());
                this.minPoint = WERegion.get(playerName).getMinimumPoint();
                this.maxPoint = WERegion.get(playerName).getMaximumPoint();
                if (regionWidth.get(playerName) > this.maxRegionWidth || regionLength.get(playerName) > this.maxRegionWidth) {
                    String dimens = String.valueOf(this.maxRegionWidth) + "x" + this.maxRegionWidth;
                    event.getPlayer().sendRawMessage((Object)ChatColor.RED + "Regions are limited to " + dimens + ". Decrease the width/length of your selection.");
                    return;
                }
                if (regionWidth.get(playerName) < this.minRegionWidth || regionLength.get(playerName) < this.minRegionWidth) {
                    String dimens = String.valueOf(this.minRegionWidth) + "x" + this.minRegionWidth;
                    event.getPlayer().sendRawMessage((Object)ChatColor.RED + "Regions must be at least " + dimens + ". Increase the width/length of your selection.");
                    return;
                }
                
                //world = WorldGuard.getInstance().getPlatform().getWorldByName(player.getWorld().toString());
                
                
                // this.regionManager = this.worldGuard.getRegionManager(event.getPlayer().getWorld());
                this.min = new BlockVector(this.minPoint.getBlockX(), 0, this.minPoint.getBlockZ());
                this.max = new BlockVector(this.maxPoint.getBlockX(), 255, this.maxPoint.getBlockZ());
                this.newRegion = new ProtectedCuboidRegion(rgName.get(event.getPlayer().getName()), this.min, this.max);
                region.put(playerName, this.newRegion);
                if (regionManager.get(playerName).overlapsUnownedRegion(region.get(playerName), localPlayer)) {
                    event.getPlayer().sendRawMessage((Object)ChatColor.RED + "Region overlaps a region you do not own!");
                    this.removePlayerFromHashmaps(playerName);
                    return;
                }
                adjustedTownCost.put(playerName, this.calculateTownCost(this.townCost, regionWidth.get(playerName), regionLength.get(playerName)));
                double balance = LandClaim.econ.getBalance((OfflinePlayer)event.getPlayer());
                if (balance < adjustedTownCost.get(playerName)) {
                    event.getPlayer().sendRawMessage((Object)ChatColor.RED + "You need $" + adjustedTownCost.get(playerName) + " to create this region!");
                    return;
                }
                timedOut.put(playerName, true);
                this.plugin.promptForConfirmation(event.getPlayer(), adjustedTownCost.get(playerName));
            } else {
                event.getPlayer().sendRawMessage((Object)ChatColor.RED + "You need to select points first!");
                this.removePlayerFromHashmaps(playerName);
            }
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void removePlayerFromHashmaps(String player) {
        if (rgName.containsKey(player)) rgName.remove(player);
        if (regionWidth.containsKey(player)) regionWidth.remove(player);
        if (regionLength.containsKey(player)) regionLength.remove(player);
        if (adjustedTownCost.containsKey(player)) adjustedTownCost.remove(player);
        if (region.containsKey(player)) region.remove(player);
        if (world.containsKey(player)) world.remove(player);
        if (WERegion.containsKey(player)) WERegion.remove(player);
        if (regionManager.containsKey(player)) regionManager.remove(player);
    }

    public void didNotTimeOut(Player player) {
        timedOut.replace(player.getName(), false);
    }

    public void createNewRegion(Player player, Boolean isPlot) {
    	String playerName = player.getName();
        if (!(player instanceof Player)) {
        	Bukkit.getPlayer(playerName).sendRawMessage("You can't do that.");
            return;
        }
        LocalPlayer localPlayer = this.worldGuard.wrapPlayer(Bukkit.getPlayer(playerName));
        
        this.existingRg = regionManager.get(playerName).getRegion(rgName.get(playerName));
        if (this.existingRg == null) {
            EconomyResponse r1;
            if (isPlot.booleanValue()) {
                this.min = new BlockVector(this.minPoint);
                this.max = new BlockVector(this.maxPoint);
                if (adjustedTownCost.containsKey(playerName)) {
                    adjustedTownCost.replace(playerName, this.plotCost);
                } else {
                    adjustedTownCost.put(playerName, this.plotCost);
                }
                region.put(playerName, (ProtectedRegion)new ProtectedCuboidRegion(rgName.get(playerName), this.min, this.max));
            }
            regionManager.get(playerName).addRegion(region.get(playerName));
            DefaultDomain domain = new DefaultDomain();
            domain.addPlayer(localPlayer);
            region.get(playerName).setOwners(domain);
            if (isPlot.booleanValue()) {
                region.get(playerName).setPriority(1);
            }
            if ((r1 = LandClaim.econ.withdrawPlayer((OfflinePlayer)BukkitAdapter.adapt(player), adjustedTownCost.get(playerName).doubleValue())).transactionSuccess()) {
            	Bukkit.getPlayer(playerName).sendRawMessage((Object)ChatColor.DARK_PURPLE + "Region saved successfully as " + rgName.get(playerName) + " and you have been charged " + (Object)ChatColor.WHITE + "$" + adjustedTownCost.get(playerName) + (Object)ChatColor.DARK_PURPLE + ".");
                this.removePlayerFromHashmaps(playerName);
                
            } else {
            	Bukkit.getPlayer(playerName).sendRawMessage((Object)ChatColor.RED + "An internal error occured. Try again.");
            }
        } else {
        	Bukkit.getPlayer(playerName).sendRawMessage((Object)ChatColor.RED + "A region by that name already exists! Pick a different name.");
        }
    }

}

