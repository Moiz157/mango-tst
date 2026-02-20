package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Match;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class SpectateCommand implements CommandExecutor {
    private final MangoParty plugin;

    public SpectateCommand(MangoParty plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HexUtils.colorize("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sender.sendMessage(HexUtils.colorize("&cUsage: /spectate <player>"));
            return true;
        }

        Match match = plugin.getMatchManager().getMatch(player);
        if (match == null) {
            sender.sendMessage(HexUtils.colorize("&cYou are not in a match!"));
            return true;
        }

        if (match.getAlivePlayers().contains(player.getUniqueId())) {
            sender.sendMessage(HexUtils.colorize("&cYou are still alive!"));
            return true;
        }

        if (!match.getSpectators().contains(player.getUniqueId())) {
             // Not a spectator?
             // Should be implied if in match but not alive.
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(HexUtils.colorize("&cPlayer not found."));
            return true;
        }

        if (!match.getAlivePlayers().contains(target.getUniqueId())) {
            sender.sendMessage(HexUtils.colorize("&cThat player is not alive in this match."));
            return true;
        }

        // Check if teammate
        boolean isTeammate = false;
        if (match.getType() == Match.Type.FFA) {
            isTeammate = true; // Anyone can spectate anyone in FFA? usually yes.
        } else {
            for (Set<UUID> team : match.getTeams()) {
                if (team.contains(player.getUniqueId()) && team.contains(target.getUniqueId())) {
                    isTeammate = true;
                    break;
                }
            }
        }

        if (!isTeammate && match.getType() != Match.Type.FFA) {
             // Maybe allow spectating enemies too?
             // Prompt says "teleport to a specific teammate's location".
             // Strict interpretation: only teammates.
             sender.sendMessage(HexUtils.colorize("&cYou can only spectate teammates."));
             return true;
        }

        player.teleport(target.getLocation());
        sender.sendMessage(HexUtils.colorize("&aTeleported to " + target.getName()));
        return true;
    }
}
