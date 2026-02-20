package me.moiz.mangoparty.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

public class Party {
    private UUID leader;
    private final Set<UUID> members;
    private final Map<UUID, Long> invites;

    public Party(UUID leader) {
        this.leader = leader;
        this.members = new HashSet<>();
        this.invites = new HashMap<>();
        addMember(leader);
    }

    public UUID getLeader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void addMember(UUID member) {
        members.add(member);
    }

    public void removeMember(UUID member) {
        members.remove(member);
    }

    public boolean isMember(UUID member) {
        return members.contains(member);
    }

    public Map<UUID, Long> getInvites() {
        return invites;
    }

    public void addInvite(UUID player) {
        invites.put(player, System.currentTimeMillis());
    }

    public void removeInvite(UUID player) {
        invites.remove(player);
    }

    public boolean hasInvite(UUID player) {
        return invites.containsKey(player);
    }
}
