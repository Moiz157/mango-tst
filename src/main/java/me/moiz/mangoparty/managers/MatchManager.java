package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.*;
import me.moiz.mangoparty.utils.HexUtils;
import me.moiz.mangoparty.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class MatchManager {
    private final MangoParty plugin;
    private final Map<UUID, Match> activeMatches = new HashMap<>();
    private final Map<UUID, Match> playerMatchMap = new HashMap<>();

    public MatchManager(MangoParty plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                gameLoop();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public Match getMatch(Player player) {
        return playerMatchMap.get(player.getUniqueId());
    }

    public Match getMatch(UUID matchId) {
        return activeMatches.get(matchId);
    }

    public void startQueueMatch(QueueEntry entry1, QueueEntry entry2, Kit kit, int teamSize) {
        Arena arena = plugin.getArenaManager().getAvailableArena("default"); // Basic selection logic
        if (arena == null) {
            // Find ANY arena
            for (Arena a : plugin.getArenaManager().getArenas()) {
                 arena = plugin.getArenaManager().getAvailableArena(a.getName());
                 if (arena != null) break;
            }
        }

        if (arena == null) {
            // Should refund queue? Or notify.
            // Players were removed from queue.
            // Ideally re-add or error.
            return;
        }

        List<Set<UUID>> teams = new ArrayList<>();
        teams.add(new HashSet<>(entry1.getPlayers()));
        teams.add(new HashSet<>(entry2.getPlayers()));

        startMatch(arena, kit, teams, Match.Type.QUEUE, 1);
    }

    public void startDuelMatch(Duel duel) {
        // Find arena
        Arena arena = plugin.getArenaManager().getAvailableArena("default");
        if (arena == null) {
            for (Arena a : plugin.getArenaManager().getArenas()) {
                arena = plugin.getArenaManager().getAvailableArena(a.getName());
                if (arena != null) break;
            }
        }

        if (arena == null) {
            Player challenger = Bukkit.getPlayer(duel.getChallenger());
            if (challenger != null) challenger.sendMessage(HexUtils.colorize("&cNo arenas available!"));
            return;
        }

        List<Set<UUID>> teams = new ArrayList<>();

        if (duel.isPartyDuel()) {
            Party p1 = plugin.getPartyManager().getPartyByLeader(duel.getChallenger());
            Party p2 = plugin.getPartyManager().getPartyByLeader(duel.getOpponent());
            if (p1 == null || p2 == null) {
                // Fail
                return;
            }
            teams.add(new HashSet<>(p1.getMembers()));
            teams.add(new HashSet<>(p2.getMembers()));
        } else {
            teams.add(new HashSet<>(Collections.singleton(duel.getChallenger())));
            teams.add(new HashSet<>(Collections.singleton(duel.getOpponent())));
        }

        startMatch(arena, duel.getKit(), teams, Match.Type.DUEL, duel.getRounds());
    }

    public void startMatch(Arena arena, Kit kit, List<Set<UUID>> teams, Match.Type type, int maxRounds) {
        Match match = new Match(arena, kit, type, maxRounds);
        match.getTeams().addAll(teams);

        // Add players
        for (Set<UUID> team : teams) {
            for (UUID uuid : team) {
                match.getPlayers().add(uuid);
                match.getAlivePlayers().add(uuid);
                match.getKills().put(uuid, 0);
                playerMatchMap.put(uuid, match);
            }
        }

        activeMatches.put(match.getId(), match);
        arena.setActive(true);

        // Setup players
        setupRound(match);
    }

    private void setupRound(Match match) {
        match.setState(MatchState.PREPARING);

        Location spawn1 = match.getArena().getSpawn1();
        Location spawn2 = match.getArena().getSpawn2();

        // Assign spawns
        // Team 0 -> Spawn 1, Team 1 -> Spawn 2.
        // If FFA or split, maybe distribute?
        // Basic logic: Team 0 -> Spawn 1, Others -> Spawn 2 (or cycle).

        int teamIndex = 0;
        for (Set<UUID> team : match.getTeams()) {
            Location spawn = (teamIndex % 2 == 0) ? spawn1 : spawn2;
            if (spawn == null) spawn = match.getArena().getCenter();

            for (UUID uuid : team) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    // Save inventory only on first round?
                    if (match.getRound() == 1 && !match.getSavedInventories().containsKey(uuid)) {
                         match.getSavedInventories().put(uuid, InventoryUtils.saveInventory(player));
                         match.getOriginalLocations().put(uuid, player.getLocation());
                    }

                    player.teleport(spawn);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setHealth(20);
                    player.setFoodLevel(20);
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        player.removePotionEffect(effect.getType());
                    }

                    // Apply Kit
                    plugin.getKitManager().applyKit(player, match.getKit());

                    player.sendMessage(HexUtils.colorize("&eMatch starting in " + plugin.getConfig().getInt("match.countdown") + " seconds!"));
                }
            }
            teamIndex++;
        }

        // Start Countdown
        match.setState(MatchState.COUNTDOWN);
    }

    private void gameLoop() {
        long now = System.currentTimeMillis();
        int countdownTime = plugin.getConfig().getInt("match.countdown");
        int maxDuration = plugin.getConfig().getInt("match.max-duration");

        List<Match> matches = new ArrayList<>(activeMatches.values());
        for (Match match : matches) {
            if (match.getState() == MatchState.COUNTDOWN) {
                long elapsed = (now - match.getStateStartTime()) / 1000;
                long remaining = countdownTime - elapsed;

                if (remaining <= 0) {
                    match.setState(MatchState.ACTIVE);
                    broadcast(match, "&aMatch Started!");
                    for (UUID uuid : match.getPlayers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                    }
                } else {
                    // Countdown messages/sounds
                    if (remaining <= 5) {
                        broadcast(match, "&e" + remaining + "...");
                        for (UUID uuid : match.getPlayers()) {
                            Player p = Bukkit.getPlayer(uuid);
                            if (p != null) p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1f);
                        }
                    }
                }
            } else if (match.getState() == MatchState.ACTIVE) {
                long elapsed = (now - match.getStateStartTime()) / 1000;
                if (elapsed >= maxDuration) {
                    endMatch(match, null); // Time limit reached, draw?
                }
            } else if (match.getState() == MatchState.ENDING) {
                // Wait a bit before cleanup?
                // For now, cleanup immediately in endMatch
            }

            // Update Scoreboard
             if (plugin.getScoreboardManager() != null) {
                 plugin.getScoreboardManager().updateScoreboard(match);
             }
        }
    }

    public void handleDeath(Player player) {
        Match match = getMatch(player);
        if (match == null || match.getState() != MatchState.ACTIVE) return;

        match.getAlivePlayers().remove(player.getUniqueId());
        match.getSpectators().add(player.getUniqueId());

        player.setGameMode(GameMode.SPECTATOR);
        player.sendMessage(HexUtils.colorize("&cYou have been eliminated!"));
        broadcast(match, "&c" + player.getName() + " has been eliminated.");

        // Check win condition
        checkWinCondition(match);
    }

    public void handleQuit(Player player) {
        Match match = getMatch(player);
        if (match == null) return;

        match.getAlivePlayers().remove(player.getUniqueId());
        // Treat as elimination
        checkWinCondition(match);

        // Remove from map if match ends?
        // Match end logic handles map cleanup.
    }

    private void checkWinCondition(Match match) {
        // Logic depends on match type
        // If FFA: 1 alive player wins.
        // If Teams: 1 team with alive players wins.

        List<Set<UUID>> teamsWithAlivePlayers = new ArrayList<>();
        for (Set<UUID> team : match.getTeams()) {
            boolean hasAlive = false;
            for (UUID uuid : team) {
                if (match.getAlivePlayers().contains(uuid)) {
                    hasAlive = true;
                    break;
                }
            }
            if (hasAlive) {
                teamsWithAlivePlayers.add(team);
            }
        }

        if (teamsWithAlivePlayers.size() <= 1) {
            Set<UUID> winners = teamsWithAlivePlayers.isEmpty() ? null : teamsWithAlivePlayers.get(0);

            if (match.getType() == Match.Type.DUEL && match.getMaxRounds() > 1) {
                // Round over
                // Update scores? Match object doesn't track scores yet.
                // Assuming simple duel: Best of N logic requires tracking wins.
                // For now, let's just end the match or next round.

                // If I want to implement multi-round, I need to track wins per team.
                // Simplified: Just end match for now.
                endMatch(match, winners);
            } else {
                endMatch(match, winners);
            }
        }
    }

    public void endMatch(Match match, Set<UUID> winners) {
        match.setState(MatchState.ENDING);

        if (winners != null) {
            StringBuilder winnerNames = new StringBuilder();
            for (UUID uuid : winners) {
                Player p = Bukkit.getPlayer(uuid);
                winnerNames.append(p != null ? p.getName() : "Unknown").append(", ");
            }
            if (winnerNames.length() > 2) winnerNames.setLength(winnerNames.length() - 2);
            broadcast(match, "&aWinners: " + winnerNames.toString());
        } else {
            broadcast(match, "&eDraw!");
        }

        // Cleanup
        for (UUID uuid : match.getPlayers()) {
            playerMatchMap.remove(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                if (plugin.getScoreboardManager() != null) {
                    plugin.getScoreboardManager().removeScoreboard(player);
                }

                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();

                // Restore inventory
                if (match.getSavedInventories().containsKey(uuid)) {
                    InventoryUtils.restoreInventory(player, match.getSavedInventories().get(uuid));
                }

                // Teleport back
                Location original = match.getOriginalLocations().get(uuid);
                if (original != null) {
                    player.teleport(original);
                } else {
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                }
            }
        }

        activeMatches.remove(match.getId());
        match.getArena().setActive(false);

        // Regenerate arena
        plugin.getArenaManager().pasteSchematic(match.getArena());
        // Note: pasteSchematic is async-compatible if using FAWE, but my implementation uses WorldEdit synchronous API wrapped.
        // If FAWE is installed, it handles it.
    }

    private void broadcast(Match match, String message) {
        for (UUID uuid : match.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendMessage(HexUtils.colorize(message));
        }
    }
}
