package net.minespire.landclaim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

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
		if(args[0].equalsIgnoreCase("claim") && args.length == 2) {
			if(args[1].length()==0) return REGIONNAME;
			else return BLANKLIST;
		} else if(args[0].equalsIgnoreCase("claimplot") && args.length == 2) {
			if(args[1].length()==0) return PLOTNAME;
			else return BLANKLIST;
		} else return BLANKLIST;

	}

}
