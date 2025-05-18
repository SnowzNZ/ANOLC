package net.minespire.landclaim.command;

import net.minespire.landclaim.claim.Claim;
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
        COMMANDS.add("gui");
        COMMANDS.add("world");
        COMMANDS.add("inspect");
        COMMANDS.add("list");
        COMMANDS.add("nearby");
        COMMANDS.add("reload");
        COMMANDS.add("recountvotes");
        COMMANDS.add("teleport");
        COMMANDS.add("vote");
        COMMANDS.add("delete");

        REGIONNAME.add("[RegionName]");
        PLOTNAME.add("[PlotName]");
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length == 1) {
            final List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
            Collections.sort(completions);
            return completions;
        }
        if (args[0].equalsIgnoreCase("claim")) {
            if (args.length == 2) return REGIONNAME;
            else return BLANKLIST;
        } else if (args[0].equalsIgnoreCase("claimplot")) {
            if (args.length == 2) return PLOTNAME;
            else return BLANKLIST;
        } else if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("inspect")) {
            if (!(sender instanceof Player)) return BLANKLIST;
            final List<String> rgNames = new ArrayList<>();
            rgNames.addAll(Claim.getClaimListOwner((Player) sender, false));
            rgNames.addAll(Claim.getClaimListOwner((Player) sender, true));
            if (args.length == 2) return rgNames;
            else return BLANKLIST;
        } else if (args[0].equalsIgnoreCase("teleport")) {
            if (!(sender instanceof Player)) return BLANKLIST;
            final List<String> rgNames = new ArrayList<>();
            rgNames.addAll(Claim.getClaimListOwner((Player) sender, false));
            rgNames.addAll(Claim.getClaimListOwner((Player) sender, true));
            rgNames.addAll(Claim.getClaimListMember((Player) sender, false));
            rgNames.addAll(Claim.getClaimListMember((Player) sender, true));
            return rgNames;
        } else return BLANKLIST;

    }

}
