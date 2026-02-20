package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.Party;
import me.moiz.mangoparty.models.QueueEntry;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {
    private final MangoParty plugin;
    // Kit Name -> Team Size -> List of Entries
    private final Map<String, Map<Integer, LinkedList<QueueEntry>>> queues = new ConcurrentHashMap<>();
    private final Map<UUID, QueueEntry> playerQueueMap = new ConcurrentHashMap<>();

    public QueueManager(MangoParty plugin) {
        this.plugin = plugin;
    }

    public synchronized void addToQueue(Player player, Kit kit, int teamSize) {
        if (isInQueue(player)) {
            player.sendMessage(HexUtils.colorize("&cYou are already in a queue!"));
            return;
        }

        List<UUID> members = new ArrayList<>();
        Party party = plugin.getPartyManager().getParty(player);

        if (teamSize > 1) {
            if (party == null) {
                player.sendMessage(HexUtils.colorize("&cYou must be in a party of " + teamSize + " to join this queue!"));
                return;
            }
            if (party.getMembers().size() != teamSize) {
                player.sendMessage(HexUtils.colorize("&cYour party must have exactly " + teamSize + " members to join this queue!"));
                return;
            }
            if (!party.getLeader().equals(player.getUniqueId())) {
                player.sendMessage(HexUtils.colorize("&cOnly the party leader can join the queue!"));
                return;
            }
            members.addAll(party.getMembers());
        } else {
            // 1v1
            if (party != null) {
                 player.sendMessage(HexUtils.colorize("&cYou must leave your party to join 1v1 queue!"));
                 return;
            }
            members.add(player.getUniqueId());
        }

        QueueEntry entry = new QueueEntry(members, kit, teamSize);

        queues.computeIfAbsent(kit.getName(), k -> new ConcurrentHashMap<>())
              .computeIfAbsent(teamSize, t -> new LinkedList<>())
              .add(entry);

        for (UUID uuid : members) {
            playerQueueMap.put(uuid, entry);
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) {
                p.sendMessage(HexUtils.colorize("&aJoined the " + teamSize + "v" + teamSize + " queue for kit " + kit.getName() + "."));
            }
        }

        checkQueue(kit, teamSize);
    }

    public synchronized void removeFromQueue(Player player) {
        QueueEntry entry = playerQueueMap.remove(player.getUniqueId());
        if (entry == null) {
            player.sendMessage(HexUtils.colorize("&cYou are not in a queue!"));
            return;
        }

        Map<Integer, LinkedList<QueueEntry>> teamMap = queues.get(entry.getKit().getName());
        if (teamMap != null) {
            LinkedList<QueueEntry> list = teamMap.get(entry.getTeamSize());
            if (list != null) {
                list.remove(entry);
            }
        }

        for (UUID uuid : entry.getPlayers()) {
            playerQueueMap.remove(uuid);
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null) {
                p.sendMessage(HexUtils.colorize("&cRemoved from queue."));
            }
        }
    }

    public boolean isInQueue(Player player) {
        return playerQueueMap.containsKey(player.getUniqueId());
    }

    private synchronized void checkQueue(Kit kit, int teamSize) {
        Map<Integer, LinkedList<QueueEntry>> teamMap = queues.get(kit.getName());
        if (teamMap == null) return;

        LinkedList<QueueEntry> list = teamMap.get(teamSize);
        if (list == null || list.size() < 2) return;

        QueueEntry entry1 = list.poll();
        QueueEntry entry2 = list.poll();

        // Clear from map
        for (UUID uuid : entry1.getPlayers()) playerQueueMap.remove(uuid);
        for (UUID uuid : entry2.getPlayers()) playerQueueMap.remove(uuid);

        // Start match
        // Need MatchManager
        // Since MatchManager is not yet available, we rely on plugin.getMatchManager() which might be null during init
        // But queue check only happens after init.

        if (plugin.getMatchManager() != null) {
            plugin.getMatchManager().startQueueMatch(entry1, entry2, kit, teamSize);
        } else {
            // Should not happen
            plugin.getLogger().severe("MatchManager not initialized!");
        }
    }

    public int getQueueSize(Kit kit, int teamSize) {
        Map<Integer, LinkedList<QueueEntry>> teamMap = queues.get(kit.getName());
        if (teamMap == null) return 0;
        LinkedList<QueueEntry> list = teamMap.get(teamSize);
        return list == null ? 0 : list.size(); // Returns number of ENTRIES (parties/players), not individual players
    }
}
