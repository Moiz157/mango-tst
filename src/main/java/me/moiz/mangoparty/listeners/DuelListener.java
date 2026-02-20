package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DuelListener implements Listener {
    private final MangoParty plugin;

    public DuelListener(MangoParty plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Clean up received requests?
        // DuelManager logic handles expiration, but explicit removal is cleaner.
        // However, DuelManager doesn't expose a way to remove requests by player easily unless I iterate.
        // Given expiration exists, I'll rely on that.
        // Or adding `removeRequests(UUID)` method to DuelManager.
    }
}
