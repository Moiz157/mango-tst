package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Party;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PartyListener implements Listener {
    private final MangoParty plugin;

    public PartyListener(MangoParty plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPartyManager().leaveParty(event.getPlayer());
        plugin.getQueueManager().removeFromQueue(event.getPlayer());
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.startsWith("@") || message.startsWith("!")) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party != null) {
                event.setCancelled(true);
                String chatMsg = message.substring(1).trim();
                plugin.getPartyManager().broadcast(party, "&9[Party] &f" + player.getName() + ": " + chatMsg);
            }
        }
    }
}
