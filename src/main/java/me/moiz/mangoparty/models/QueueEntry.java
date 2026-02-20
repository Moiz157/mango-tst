package me.moiz.mangoparty.models;

import java.util.List;
import java.util.UUID;

public class QueueEntry {
    private final List<UUID> players;
    private final Kit kit;
    private final int teamSize;
    private final long timestamp;

    public QueueEntry(List<UUID> players, Kit kit, int teamSize) {
        this.players = players;
        this.kit = kit;
        this.teamSize = teamSize;
        this.timestamp = System.currentTimeMillis();
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public Kit getKit() {
        return kit;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
