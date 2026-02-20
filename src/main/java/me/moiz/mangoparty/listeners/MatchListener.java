package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.KitRules;
import me.moiz.mangoparty.models.Match;
import me.moiz.mangoparty.models.MatchState;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Set;
import java.util.UUID;

public class MatchListener implements Listener {
    private final MangoParty plugin;

    public MatchListener(MangoParty plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();
        Match match = plugin.getMatchManager().getMatch(victim);

        if (match == null) return;

        // Friendly Fire Check
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Match attackerMatch = plugin.getMatchManager().getMatch(attacker);

            if (attackerMatch != null && attackerMatch.equals(match)) {
                if (match.getState() != MatchState.ACTIVE) {
                    event.setCancelled(true);
                    return;
                }

                // Check teams
                if (areTeammates(match, victim, attacker)) {
                    event.setCancelled(true);
                    attacker.sendMessage(HexUtils.colorize("&cYou cannot attack teammates!"));
                    return;
                }

                // Damage Multiplier
                double multiplier = match.getKit().getRules().getDamageMultiplier();
                if (multiplier != 1.0) {
                    event.setDamage(event.getDamage() * multiplier);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageSelf(EntityDamageEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Match match = plugin.getMatchManager().getMatch(player);
        if (match == null) return;

        if (match.getState() != MatchState.ACTIVE) {
            event.setCancelled(true);
            return;
        }

        // Lethal damage check
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            player.setHealth(20);
            player.setFoodLevel(20);
            // Handle death logic without actual death event
            plugin.getMatchManager().handleDeath(player);

            // Register kill if caused by entity
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) event;
                if (damageEvent.getDamager() instanceof Player) {
                    Player killer = (Player) damageEvent.getDamager();
                    Match killerMatch = plugin.getMatchManager().getMatch(killer);
                    if (killerMatch != null && killerMatch.equals(match)) {
                         match.getKills().put(killer.getUniqueId(), match.getKills().getOrDefault(killer.getUniqueId(), 0) + 1);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getMatch(player);
        if (match == null) return;

        if (match.getState() != MatchState.ACTIVE || !match.getKit().getRules().isAllowBlockBreak()) {
            event.setCancelled(true);
            player.sendMessage(HexUtils.colorize("&cBlock breaking is disabled in this match."));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getMatch(player);
        if (match == null) return;

        if (match.getState() != MatchState.ACTIVE || !match.getKit().getRules().isAllowBlockPlace()) {
            event.setCancelled(true);
            player.sendMessage(HexUtils.colorize("&cBlock placing is disabled in this match."));
            return;
        }

        // Instant TNT
        if (event.getBlock().getType().name().contains("TNT") && match.getKit().getRules().isInstantTNT()) {
            event.getBlock().setType(org.bukkit.Material.AIR);
            TNTPrimed tnt = (TNTPrimed) event.getBlock().getWorld().spawnEntity(event.getBlock().getLocation().add(0.5, 0, 0.5), EntityType.TNT);
            tnt.setFuseTicks(0); // Instant? or short fuse? usually instant means explodes on place or ignites immediately.
            // Prompt says "Instant TNT explosion".
            // Usually means ignites instantly with short fuse? Or explodes instantly?
            // "Instant TNT" usually implies auto-ignite.
            // "Explosion" implies immediate boom.
            // I'll set fuse to 0 (immediate boom).
        }
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Match match = plugin.getMatchManager().getMatch(player);
        if (match == null) return;

        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                && !match.getKit().getRules().isNaturalRegen()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Match match = plugin.getMatchManager().getMatch(player);
        if (match != null && match.getState() != MatchState.ACTIVE) {
            event.setCancelled(true);
        }
        // Maybe prevent drops in active too? Prompt doesn't say.
        // Usually allow dropping items like potions/soup.
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getMatchManager().handleQuit(event.getPlayer());
    }

    private boolean areTeammates(Match match, Player p1, Player p2) {
        if (match.getType() == Match.Type.FFA) return false;
        for (Set<UUID> team : match.getTeams()) {
            if (team.contains(p1.getUniqueId()) && team.contains(p2.getUniqueId())) {
                return true;
            }
        }
        return false;
    }
}
