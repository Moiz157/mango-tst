package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Duel;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.Party;
import me.moiz.mangoparty.utils.HexUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelManager {
    private final MangoParty plugin;
    private final Map<UUID, Duel> duelRequests = new HashMap<>(); // Opponent -> Duel

    public DuelManager(MangoParty plugin) {
        this.plugin = plugin;
    }

    public void challenge(Player challenger, Player opponent, Kit kit, int rounds, boolean partyDuel) {
        if (duelRequests.containsKey(opponent.getUniqueId())) {
            Duel existing = duelRequests.get(opponent.getUniqueId());
            if (existing.getChallenger().equals(challenger.getUniqueId())) {
                challenger.sendMessage(HexUtils.colorize("&cYou have already challenged this player!"));
                return;
            }
        }

        Duel duel = new Duel(challenger.getUniqueId(), opponent.getUniqueId(), kit, rounds, partyDuel);
        duelRequests.put(opponent.getUniqueId(), duel);

        String type = partyDuel ? "Party Duel" : "Duel";
        challenger.sendMessage(HexUtils.colorize("&aSent " + type + " request to " + opponent.getName() + "."));

        TextComponent message = new TextComponent(HexUtils.colorize("&a" + challenger.getName() + " has challenged you to a " + type + " (" + kit.getName() + "). "));
        TextComponent accept = new TextComponent(HexUtils.colorize("&2[Accept]"));
        String cmd = partyDuel ? "/party acceptduel " + challenger.getName() : "/duel accept " + challenger.getName();
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));

        TextComponent space = new TextComponent(" ");
        TextComponent decline = new TextComponent(HexUtils.colorize("&c[Decline]"));
        String declineCmd = partyDuel ? "/party declineduel " + challenger.getName() : "/duel decline " + challenger.getName();
        decline.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, declineCmd));

        message.addExtra(accept);
        message.addExtra(space);
        message.addExtra(decline);

        opponent.spigot().sendMessage(message);

        // Expire after 60s
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (duelRequests.containsKey(opponent.getUniqueId()) && duelRequests.get(opponent.getUniqueId()).equals(duel)) {
                duelRequests.remove(opponent.getUniqueId());
                if (opponent.isOnline()) opponent.sendMessage(HexUtils.colorize("&cDuel request from " + challenger.getName() + " expired."));
                if (challenger.isOnline()) challenger.sendMessage(HexUtils.colorize("&cDuel request to " + opponent.getName() + " expired."));
            }
        }, 60 * 20L);
    }

    public void acceptDuel(Player player, String challengerName) {
        Duel duel = duelRequests.get(player.getUniqueId());
        if (duel == null) {
            player.sendMessage(HexUtils.colorize("&cYou have no pending duel requests!"));
            return;
        }

        Player challenger = Bukkit.getPlayer(duel.getChallenger());
        if (challenger == null || !challenger.getName().equalsIgnoreCase(challengerName)) {
             // Maybe multiple requests? For now only one active request per opponent.
             // If name mismatch, maybe expired or replaced.
             player.sendMessage(HexUtils.colorize("&cThat duel request is no longer valid."));
             return;
        }

        duelRequests.remove(player.getUniqueId());

        // Start Match
        if (plugin.getMatchManager() != null) {
            plugin.getMatchManager().startDuelMatch(duel);
        }
    }

    public void declineDuel(Player player, String challengerName) {
        Duel duel = duelRequests.remove(player.getUniqueId());
        if (duel != null) {
            player.sendMessage(HexUtils.colorize("&cYou declined the duel."));
            Player challenger = Bukkit.getPlayer(duel.getChallenger());
            if (challenger != null) {
                challenger.sendMessage(HexUtils.colorize("&c" + player.getName() + " declined your duel request."));
            }
        } else {
             player.sendMessage(HexUtils.colorize("&cNo pending duel found."));
        }
    }
}
