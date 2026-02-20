package me.moiz.mangoparty.commands;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.gui.KitSelectorGUI;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QueueCommand implements CommandExecutor {
    private final MangoParty plugin;

    public QueueCommand(MangoParty plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(HexUtils.colorize("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;
        String cmd = label.toLowerCase();

        String context = "";
        if (cmd.equals("1v1queue")) {
            context = "Queue1v1";
        } else if (cmd.equals("2v2queue")) {
            context = "Queue2v2";
        } else if (cmd.equals("3v3queue")) {
            context = "Queue3v3";
        } else {
            return false;
        }

        // Check if already in queue
        if (plugin.getQueueManager().isInQueue(player)) {
            player.sendMessage(HexUtils.colorize("&cYou are already in a queue! Use /leavequeue to leave."));
            return true;
        }

        // Open Kit Selector
        KitSelectorGUI.open(player, plugin, context);
        return true;
    }
}
