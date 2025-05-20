package dev.snowz.anolc.listener;

import dev.snowz.anolc.claim.Claim;
import dev.snowz.anolc.gui.GUIManager;
import dev.snowz.anolc.prompt.Prompt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public final class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    private void onAnswerPrompt(final AsyncPlayerChatEvent chatEvent) {
        if (!Prompt.hasActivePrompt(chatEvent.getPlayer())) return;
        chatEvent.setCancelled(true);
        final Player player = chatEvent.getPlayer();
        final Prompt prompt = Prompt.getPrompt(player.getName());
        final String chatMessage = chatEvent.getMessage();
        prompt.setAnswer(chatMessage);
        if (prompt.getPromptType().equals("ADDMEMBER")) {
            if (!Claim.addMember(player, chatMessage, prompt.getRegion()))
                player.sendMessage(ChatColor.RED + "Could not add member to region. Player must be online.");
            else
                player.sendMessage(ChatColor.GOLD + "Added " + ChatColor.AQUA + Bukkit.getPlayer(chatMessage).getDisplayName() + ChatColor.GOLD + " as new member to claim " + ChatColor.AQUA + prompt.getRegion().getId());
        }

        if (prompt.getPromptType().equals("ADDOWNER")) {
            if (!Claim.addOwner(player, chatMessage, prompt.getRegion()))
                player.sendMessage(ChatColor.RED + "Could not add owner to region. Player must be online.");
            else
                player.sendMessage(ChatColor.GOLD + "Added " + ChatColor.AQUA + Bukkit.getPlayer(chatMessage).getDisplayName() + ChatColor.GOLD + " as new owner to claim " + ChatColor.AQUA + prompt.getRegion().getId());
        }

        if (GUIManager.editableClaimFlags.containsKey(prompt.getPromptType())) {
            prompt.getRegion().setFlag(GUIManager.editableClaimFlags.get(prompt.getPromptType()), prompt.getAnswer());
            player.sendMessage(ChatColor.GOLD + "The new value for '" + prompt.getPromptType() + "' was set on " + ChatColor.AQUA + prompt.getRegion().getId());
        }
    }
}
