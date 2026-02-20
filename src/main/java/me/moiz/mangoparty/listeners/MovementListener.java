package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.models.Match;
import me.moiz.mangoparty.models.MatchState;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {
    private final MangoParty plugin;

    public MovementListener(MangoParty plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return; // Ignore rotation
        }

        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getMatch(player);
        if (match == null) return;

        if (match.getState() == MatchState.COUNTDOWN) {
            Location newTo = event.getFrom().clone();
            newTo.setYaw(event.getTo().getYaw());
            newTo.setPitch(event.getTo().getPitch());
            event.setTo(newTo);
            return;
        }

        if (match.getState() == MatchState.ACTIVE && match.getAlivePlayers().contains(player.getUniqueId())) {
            Arena arena = match.getArena();
            if (arena == null || arena.getCorner1() == null || arena.getCorner2() == null) return;

            Location to = event.getTo();
            double minX = Math.min(arena.getCorner1().getX(), arena.getCorner2().getX());
            double maxX = Math.max(arena.getCorner1().getX(), arena.getCorner2().getX());
            double minZ = Math.min(arena.getCorner1().getZ(), arena.getCorner2().getZ());
            double maxZ = Math.max(arena.getCorner1().getZ(), arena.getCorner2().getZ());
            double minY = Math.min(arena.getCorner1().getY(), arena.getCorner2().getY());
            double maxY = Math.max(arena.getCorner1().getY(), arena.getCorner2().getY());

            // Add some buffer? 1 block?
            // "if a player in a match leaves the arena region"
            // Usually strict bounds.

            if (to.getX() < minX || to.getX() > maxX + 1 ||
                to.getZ() < minZ || to.getZ() > maxZ + 1 || // +1 for block bounds
                to.getY() < minY || to.getY() > maxY + 1) {

                // Outside
                Location center = arena.getCenter();
                if (center != null) {
                    player.teleport(center);
                } else {
                    player.teleport(event.getFrom());
                }
                player.sendMessage(HexUtils.colorize("&cYou cannot leave the arena!"));
            }
        }
    }
}
