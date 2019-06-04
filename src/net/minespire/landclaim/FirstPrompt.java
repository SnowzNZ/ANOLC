package net.minespire.landclaim;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.FixedSetPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;

public class FirstPrompt
extends FixedSetPrompt {
    LandClaim plugin;
    CreateRegions regionCreator;
    Player player;

    public FirstPrompt(LandClaim LandClaim, CreateRegions create, Player p) {
        super(new String[]{"confirm", "Confirm", "CONFIRM", "cancel", "Cancel", "CANCEL"});
        this.plugin = LandClaim;
        this.regionCreator = create;
        this.player = p;
    }

    public String getPromptText(ConversationContext arg0) {
        return (Object)ChatColor.GOLD + "Type " + (Object)ChatColor.WHITE + (Object)ChatColor.ITALIC + "confirm (no slash) " + (Object)ChatColor.RESET + (Object)ChatColor.GOLD + "to proceed or " + (Object)ChatColor.WHITE + (Object)ChatColor.ITALIC + "cancel " + (Object)ChatColor.RESET + (Object)ChatColor.GOLD + "to cancel this operation. It will auto-cancel in 15 seconds.";
    }

    protected Prompt acceptValidatedInput(ConversationContext c, String s) {
        Conversable cpo = c.getForWhom();
        if (s.equalsIgnoreCase("confirm")) {
            this.regionCreator.createNewRegion(BukkitAdapter.adapt(player), false);
            this.regionCreator.didNotTimeOut(BukkitAdapter.adapt(player));
        } else if (s.equalsIgnoreCase("cancel")) {
            cpo.sendRawMessage((Object)ChatColor.RED + "The operation was cancelled");
            this.regionCreator.removePlayerFromHashmaps(this.player.getName());
            this.regionCreator.didNotTimeOut(BukkitAdapter.adapt(player));
            return END_OF_CONVERSATION;
        }
        return END_OF_CONVERSATION;
    }
}

