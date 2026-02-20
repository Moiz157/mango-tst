package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.gui.MatchTypeGUI;
import me.moiz.mangoparty.models.Party;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyCommand implements CommandExecutor {
    private final MangoParty plugin;

    public PartyCommand(MangoParty plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HexUtils.colorize("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        String sub = args[0];

        if (sub.equalsIgnoreCase("create")) {
            plugin.getPartyManager().createParty(player);
            return true;
        }

        if (sub.equalsIgnoreCase("disband")) {
            plugin.getPartyManager().disbandParty(player);
            return true;
        }

        if (sub.equalsIgnoreCase("leave")) {
            plugin.getPartyManager().leaveParty(player);
            return true;
        }

        if (sub.equalsIgnoreCase("info")) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party == null) {
                player.sendMessage(HexUtils.colorize("&cYou are not in a party."));
                return true;
            }
            player.sendMessage(HexUtils.colorize("&eParty Leader: &f" + getName(party.getLeader())));
            player.sendMessage(HexUtils.colorize("&eMembers: &f(" + party.getMembers().size() + ")"));
            for (UUID uuid : party.getMembers()) {
                player.sendMessage(HexUtils.colorize(" &7- " + getName(uuid)));
            }
            return true;
        }

        if (sub.equalsIgnoreCase("match")) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party == null || !party.getLeader().equals(player.getUniqueId())) {
                player.sendMessage(HexUtils.colorize("&cYou must be the party leader to start a match!"));
                return true;
            }
            MatchTypeGUI.open(player);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(HexUtils.colorize("&cUsage: /party " + sub + " <player>"));
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);

        if (sub.equalsIgnoreCase("invite")) {
            if (target == null) {
                player.sendMessage(HexUtils.colorize("&cPlayer not found."));
                return true;
            }
            plugin.getPartyManager().invitePlayer(player, target);
            return true;
        }

        if (sub.equalsIgnoreCase("join")) {
            // targetName is leader name
            if (target == null) {
                player.sendMessage(HexUtils.colorize("&cLeader not found or offline."));
                return true;
            }
            plugin.getPartyManager().joinParty(player, target);
            return true;
        }

        if (sub.equalsIgnoreCase("kick")) {
            if (target == null) {
                // Try to find offline player logic? Assuming online.
                player.sendMessage(HexUtils.colorize("&cPlayer not found."));
                return true;
            }
            plugin.getPartyManager().kickPlayer(player, target);
            return true;
        }

        if (sub.equalsIgnoreCase("transfer")) {
            if (target == null) {
                player.sendMessage(HexUtils.colorize("&cPlayer not found."));
                return true;
            }
            plugin.getPartyManager().transferLeadership(player, target);
            return true;
        }

        if (sub.equalsIgnoreCase("challenge")) {
            // Challenge another party leader
            if (target == null) {
                player.sendMessage(HexUtils.colorize("&cLeader not found."));
                return true;
            }

            Party myParty = plugin.getPartyManager().getParty(player);
            Party targetParty = plugin.getPartyManager().getParty(target);

            if (myParty == null || !myParty.getLeader().equals(player.getUniqueId())) {
                player.sendMessage(HexUtils.colorize("&cYou must be a party leader!"));
                return true;
            }

            if (targetParty == null || !targetParty.getLeader().equals(target.getUniqueId())) {
                player.sendMessage(HexUtils.colorize("&cThat player is not a party leader!"));
                return true;
            }

            if (myParty.equals(targetParty)) {
                player.sendMessage(HexUtils.colorize("&cYou cannot challenge your own party!"));
                return true;
            }

            // Open Kit Selector to choose kit for duel
            // We need to store that this selection is for a party duel against 'target'.
            // Simple way: store a temporary state or pass it via metadata?
            // GUIListener needs to know context.
            // Context string: "PartyDuel:<TargetName>"

            me.moiz.mangoparty.gui.KitSelectorGUI.open(player, plugin, "PartyDuel:" + target.getName());
            return true;
        }

        if (sub.equalsIgnoreCase("acceptduel")) {
            plugin.getDuelManager().acceptDuel(player, targetName);
            return true;
        }

        if (sub.equalsIgnoreCase("declineduel")) {
            plugin.getDuelManager().declineDuel(player, targetName);
            return true;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(HexUtils.colorize("&e&lParty Commands:"));
        player.sendMessage(HexUtils.colorize("&7/party create"));
        player.sendMessage(HexUtils.colorize("&7/party invite <player>"));
        player.sendMessage(HexUtils.colorize("&7/party join <leader>"));
        player.sendMessage(HexUtils.colorize("&7/party leave"));
        player.sendMessage(HexUtils.colorize("&7/party kick <player>"));
        player.sendMessage(HexUtils.colorize("&7/party transfer <player>"));
        player.sendMessage(HexUtils.colorize("&7/party disband"));
        player.sendMessage(HexUtils.colorize("&7/party match"));
        player.sendMessage(HexUtils.colorize("&7/party info"));
        player.sendMessage(HexUtils.colorize("&7/party challenge <leader>"));
    }

    private String getName(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        return p != null ? p.getName() : "Offline";
    }
}
