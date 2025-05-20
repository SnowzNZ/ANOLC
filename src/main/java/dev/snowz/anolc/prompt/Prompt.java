package dev.snowz.anolc.prompt;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import dev.snowz.anolc.ANOLC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Prompt {

    private final Player player;
    private int serviceTaskID;
    @Setter
    @Getter
    private String answer;
    private final String promptMessage;
    private int ticksPassed = 1;
    @Getter
    private final String promptType; //ADDMEMBER, ADDOWNER, [flagname key fom GUIManager.editableClaimFlags]
    @Getter
    private final ProtectedRegion region;
    private static final Set<String> playersWithPrompts = new HashSet<>();
    private static final Map<String, Prompt> playerPrompts = new HashMap<>();

    public Prompt(final String message, final Player player, final String promptType, final ProtectedRegion region) {
        this.promptMessage = message;
        this.player = player;
        this.promptType = promptType;
        this.region = region;
    }

    private void awaitResponse() {

        answer = null;
        playersWithPrompts.add(player.getName());
        serviceTaskID = Bukkit.getScheduler().runTaskTimer(
            ANOLC.getInstance(), () -> {
                ticksPassed += 5;
                if (ticksPassed == 200 - 60 + 1) player.sendMessage(ChatColor.RED + "Prompt expires in..");
                if (ticksPassed == 200 - 60 + 1) player.sendMessage(ChatColor.RED + "3");
                if (ticksPassed == 200 - 40 + 1) player.sendMessage(ChatColor.RED + "2");
                if (ticksPassed == 200 - 20 + 1) player.sendMessage(ChatColor.RED + "1");

                if (answer != null || ticksPassed > 200) {
                    Bukkit.getScheduler().cancelTask(serviceTaskID);
                    playersWithPrompts.remove(player.getName());
                    playerPrompts.remove(player.getName());
                }
                if (answer == null && ticksPassed > 200) player.sendMessage(ChatColor.RED + "Prompt expired.");

            }, 1L, 5L
        ).getTaskId();
    }

    public static boolean hasActivePrompt(final Player player) {
        return playersWithPrompts.contains(player.getName());
    }

    public boolean sendPrompt() {
        if (hasActivePrompt(player)) return false;
        else {
            player.sendMessage(promptMessage);
            awaitResponse();
            savePrompt(this);
            return true;
        }
    }

    public void cancelPrompt() {
        Bukkit.getScheduler().cancelTask(serviceTaskID);
        playersWithPrompts.remove(player.getName());
        playerPrompts.remove(player.getName());
        player.sendMessage(ChatColor.RED + "Prompt cancelled");
    }

    public void savePrompt(final Prompt prompt) {
        playerPrompts.put(player.getName(), this);
    }

    public static Prompt getPrompt(final String playerName) {
        return playerPrompts.get(playerName);
    }
}
