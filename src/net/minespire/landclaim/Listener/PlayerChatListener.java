package net.minespire.landclaim.Listener;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import net.minespire.landclaim.Claim.Claim;
import net.minespire.landclaim.Prompt.Prompt;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    private void onAnswerPrompt(AsyncPlayerChatEvent chatEvent){
        Bukkit.broadcastMessage("got here");
        if(!Prompt.hasActivePrompt(chatEvent.getPlayer())) return;
        chatEvent.setCancelled(true);
        Player player = chatEvent.getPlayer();
        Prompt prompt = Prompt.getPrompt(player.getDisplayName());
        if(prompt.getPromptType().equals("ADDMEMBER")) {
            if(!Claim.addMember(BukkitAdapter.adapt(player), chatEvent.getMessage(), prompt.getRegion())) player.sendMessage("Could not add member to region");
            else player.sendMessage("Added new member to claim");
        }
        if(prompt.getPromptType().equals("ADDOWNER")) {
            if(!Claim.addOwner(BukkitAdapter.adapt(player), chatEvent.getMessage(), prompt.getRegion())) player.sendMessage("Could not add owner to region");
            else player.sendMessage("Added new owner to claim");
        }

    }
}
