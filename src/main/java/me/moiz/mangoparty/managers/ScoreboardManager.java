package me.moiz.mangoparty.managers;

import fr.mrmicky.fastboard.FastBoard;
import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import me.moiz.mangoparty.models.MatchState;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardManager {
    private final MangoParty plugin;
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    public ScoreboardManager(MangoParty plugin) {
        this.plugin = plugin;
    }

    public void updateScoreboard(Match match) {
        for (UUID uuid : match.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            FastBoard board = boards.computeIfAbsent(uuid, k -> new FastBoard(player));
            updateBoard(board, match, player);
        }
    }

    public void removeScoreboard(Player player) {
        FastBoard board = boards.remove(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    private void updateBoard(FastBoard board, Match match, Player player) {
        FileConfiguration config = plugin.getConfigManager().getScoreboardConfig();
        String typeKey = "default";

        switch (match.getType()) {
            case DUEL: typeKey = "duel"; break;
            case SPLIT: typeKey = "team"; break; // Or "split" if defined, prompt says "team" in example? No, "team" in my scoreboard.yml.
            case FFA: typeKey = "ffa"; break;
            case QUEUE:
                typeKey = (match.getTeams().size() > 1 && match.getTeams().get(0).size() > 1) ? "team" : "duel"; // Simple heuristic
                break;
        }

        // Fallback
        if (!config.contains(typeKey)) typeKey = "default";

        ConfigurationSection section = config.getConfigurationSection(typeKey);
        if (section == null) return;

        String title = section.getString("title", "MangoParty");
        board.updateTitle(HexUtils.colorize(title));

        List<String> lines = section.getStringList("lines");
        List<String> updatedLines = new ArrayList<>();

        for (String line : lines) {
            updatedLines.add(HexUtils.colorize(replacePlaceholders(line, match, player)));
        }

        board.updateLines(updatedLines);
    }

    private String replacePlaceholders(String text, Match match, Player player) {
        text = text.replace("%arena%", match.getArena().getName());
        text = text.replace("%kit%", match.getKit().getName());

        text = text.replace("%alive%", String.valueOf(match.getAlivePlayers().size()));
        text = text.replace("%total%", String.valueOf(match.getPlayers().size()));

        int kills = match.getKills().getOrDefault(player.getUniqueId(), 0);
        text = text.replace("%kills%", String.valueOf(kills));

        long elapsed = 0;
        if (match.getState() == MatchState.ACTIVE) {
            elapsed = (System.currentTimeMillis() - match.getStateStartTime()) / 1000;
        } else if (match.getState() == MatchState.COUNTDOWN) {
             // Show countdown?
             // Prompt says "%time% Elapsed time".
             // Maybe show remaining countdown if in countdown?
             // Usually elapsed implies from start.
             // I'll show 00:00 or countdown if wanted.
             // Let's stick to elapsed time of match.
        }
        text = text.replace("%time%", formatTime(elapsed));

        // Team counts
        if (match.getTeams().size() >= 1) {
             text = text.replace("%team1_alive%", String.valueOf(countAlive(match, match.getTeams().get(0))));
        } else {
             text = text.replace("%team1_alive%", "0");
        }

        if (match.getTeams().size() >= 2) {
             text = text.replace("%team2_alive%", String.valueOf(countAlive(match, match.getTeams().get(1))));
        } else {
             text = text.replace("%team2_alive%", "0");
        }

        text = text.replace("%round%", String.valueOf(match.getRound()));
        text = text.replace("%total_rounds%", String.valueOf(match.getMaxRounds()));

        // Opponent (for Duel)
        if (match.getType() == Match.Type.DUEL) {
            // Find opponent
            UUID opponentId = null;
            for (Set<UUID> team : match.getTeams()) {
                if (!team.contains(player.getUniqueId())) {
                     if (!team.isEmpty()) opponentId = team.iterator().next(); // Simple 1v1 logic
                     break;
                }
            }
            String opponentName = "None";
            if (opponentId != null) {
                Player op = Bukkit.getPlayer(opponentId);
                opponentName = (op != null) ? op.getName() : "Unknown";
            }
            text = text.replace("%opponent%", opponentName);
        }

        return text;
    }

    private long countAlive(Match match, Set<UUID> team) {
        long count = 0;
        for (UUID uuid : team) {
            if (match.getAlivePlayers().contains(uuid)) {
                count++;
            }
        }
        return count;
    }

    private String formatTime(long seconds) {
        long m = seconds / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
