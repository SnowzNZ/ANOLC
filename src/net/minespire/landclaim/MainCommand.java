package net.minespire.landclaim;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.regions.RegionSelector;





public class MainCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
        	Player player = null;
        	
        	if(sender instanceof Player) {
        		player = (Player)sender;
        	}
            
            
            
            
            
            
			switch(args[0].toLowerCase()) {
			case "claim": 
				if (player != null) {
					if(args[1]==null) return false;
	                Claim claim = new Claim(player, args[1]);
	                LandClaim.claimMap.put(player.getUniqueId().toString(), claim);
	        		GUI gui = new GUI(player, args[1]);
	                gui.openClaimGUI();
	                
					player.sendMessage("Reached here");
				} else sender.sendMessage("You must be a player to use that command!");
				break;
			default: return false;
		}
            
            
            
            /*
            // Create a new ItemStack (type: diamond)
            ItemStack diamond = new ItemStack(Material.DIAMOND);

            // Create a new ItemStack (type: brick)
            ItemStack bricks = new ItemStack(Material.BRICK);

            // Set the amount of the ItemStack
            bricks.setAmount(20);

            // Give the player our items (comma-seperated list of all ItemStack)
            player.getInventory().addItem(bricks, diamond);
            */
        }

        // If the player (or console) uses our command correct, we can return true
        return true;
	}
	
}