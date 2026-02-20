package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.gui.KitSelectorGUI;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {
    private final MangoParty plugin;

    public DuelCommand(MangoParty plugin) {
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
            sender.sendMessage(HexUtils.colorize("&cUsage: /duel <player>"));
            return true;
        }

        String targetName = args[0];

        if (targetName.equalsIgnoreCase("accept")) {
            if (args.length < 2) {
                sender.sendMessage(HexUtils.colorize("&cUsage: /duel accept <challenger>"));
                return true;
            }
            plugin.getDuelManager().acceptDuel(player, args[1]);
            return true;
        }

        if (targetName.equalsIgnoreCase("decline")) {
            if (args.length < 2) {
                sender.sendMessage(HexUtils.colorize("&cUsage: /duel decline <challenger>"));
                return true;
            }
            plugin.getDuelManager().declineDuel(player, args[1]);
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(HexUtils.colorize("&cPlayer not found."));
            return true;
        }

        if (target.equals(player)) {
            sender.sendMessage(HexUtils.colorize("&cYou cannot duel yourself!"));
            return true;
        }

        // Open Kit Selector to choose kit for duel
        // Context: "Duel:<TargetName>"
        KitSelectorGUI.open(player, plugin, "Duel:" + target.getName());
        return true;
    }
}
