package net.minespire.landclaim;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.entity.Player;


public class MainCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            
            
            GUI gui = new GUI(player);
            gui.openInventory(player);
            
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