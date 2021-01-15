package net.minespire.landclaim.Command;

import net.minespire.landclaim.Claim.Claim;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandCompleter implements TabCompleter {

	private static final List<String> COMMANDS = new ArrayList<>();
	private static final List<String> REGIONNAME = new ArrayList<>();
	private static final List<String> PLOTNAME = new ArrayList<>();
	private static final List<String> BLANKLIST = new ArrayList<>();
	
	public CommandCompleter() {
		COMMANDS.add("claim");
		COMMANDS.add("claimplot");
		COMMANDS.add("list");
		COMMANDS.add("reload");
		COMMANDS.add("remove");
		
		REGIONNAME.add("[RegionName]");
		PLOTNAME.add("[PlotName]");
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 1) {
			final List<String> completions = new ArrayList<>();
			StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
			Collections.sort(completions);
			return completions;
		}
		if(args[0].equalsIgnoreCase("claim")) {
			if(args.length == 2) return REGIONNAME;
			else return BLANKLIST;
		} else if(args[0].equalsIgnoreCase("claimplot")) {
			if(args.length == 2) return PLOTNAME;
			else return BLANKLIST;
		} else if(args[0].equalsIgnoreCase("remove")) {
			if(!(sender instanceof Player)) return BLANKLIST;
			List<String> rgNames = new ArrayList<>();
			Claim.getClaimListOwner((Player)sender, false).forEach(region -> rgNames.add(region.getId()));
			Claim.getClaimListOwner((Player)sender, true).forEach(region -> rgNames.add(region.getId()));
			if(args.length == 2) return rgNames;
			else return BLANKLIST;
		} else return BLANKLIST;

	}

}
