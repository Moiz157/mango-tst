package me.moiz.mangoparty.models;

import java.util.*;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Match {
    public enum Type {
        SPLIT, FFA, DUEL, QUEUE
    }

    private final UUID id;
    private final Arena arena;
    private final Kit kit;
    private final Type type;
    private MatchState state;

    private final Set<UUID> players;
    private final Set<UUID> alivePlayers;
    private final Set<UUID> spectators;
    private final List<Set<UUID>> teams;

    private final Map<UUID, Integer> kills;
    private final Map<UUID, ItemStack[]> savedInventories;
    private final Map<UUID, Location> originalLocations;

    private int round;
    private final int maxRounds;
    private long startTime;
    private long stateStartTime;

    public Match(Arena arena, Kit kit, Type type, int maxRounds) {
        this.id = UUID.randomUUID();
        this.arena = arena;
        this.kit = kit;
        this.type = type;
        this.state = MatchState.PREPARING;
        this.maxRounds = maxRounds;
        this.round = 1;

        this.players = new HashSet<>();
        this.alivePlayers = new HashSet<>();
        this.spectators = new HashSet<>();
        this.teams = new ArrayList<>();
        this.kills = new HashMap<>();
        this.savedInventories = new HashMap<>();
        this.originalLocations = new HashMap<>();
    }

    public UUID getId() { return id; }
    public Arena getArena() { return arena; }
    public Kit getKit() { return kit; }
    public Type getType() { return type; }
    public MatchState getState() { return state; }
    public void setState(MatchState state) {
        this.state = state;
        this.stateStartTime = System.currentTimeMillis();
    }

    public Set<UUID> getPlayers() { return players; }
    public Set<UUID> getAlivePlayers() { return alivePlayers; }
    public Set<UUID> getSpectators() { return spectators; }
    public List<Set<UUID>> getTeams() { return teams; }

    public Map<UUID, Integer> getKills() { return kills; }
    public Map<UUID, ItemStack[]> getSavedInventories() { return savedInventories; }
    public Map<UUID, Location> getOriginalLocations() { return originalLocations; }

    public int getRound() { return round; }
    public void nextRound() { round++; }
    public int getMaxRounds() { return maxRounds; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public long getStateStartTime() { return stateStartTime; }
}
