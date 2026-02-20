package me.moiz.mangoparty.models;

import java.util.UUID;

public class Duel {
    private final UUID challenger;
    private final UUID opponent;
    private final Kit kit;
    private final int rounds;
    private final boolean partyDuel;
    private final long timestamp;

    public Duel(UUID challenger, UUID opponent, Kit kit, int rounds, boolean partyDuel) {
        this.challenger = challenger;
        this.opponent = opponent;
        this.kit = kit;
        this.rounds = rounds;
        this.partyDuel = partyDuel;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getChallenger() {
        return challenger;
    }

    public UUID getOpponent() {
        return opponent;
    }

    public Kit getKit() {
        return kit;
    }

    public int getRounds() {
        return rounds;
    }

    public boolean isPartyDuel() {
        return partyDuel;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
