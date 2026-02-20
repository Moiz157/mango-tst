package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Party;
import me.moiz.mangoparty.utils.HexUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyManager {
    private final MangoParty plugin;
    private final Map<UUID, Party> partiesByLeader = new HashMap<>();
    private final Map<UUID, Party> playerPartyMap = new HashMap<>();

    public PartyManager(MangoParty plugin) {
        this.plugin = plugin;
    }

    public Party getParty(Player player) {
        return playerPartyMap.get(player.getUniqueId());
    }

    public Party getPartyByLeader(UUID leaderId) {
        return partiesByLeader.get(leaderId);
    }

    public boolean isInParty(Player player) {
        return playerPartyMap.containsKey(player.getUniqueId());
    }

    public void createParty(Player leader) {
        if (isInParty(leader)) {
            leader.sendMessage(HexUtils.colorize("&cYou are already in a party!"));
            return;
        }

        Party party = new Party(leader.getUniqueId());
        partiesByLeader.put(leader.getUniqueId(), party);
        playerPartyMap.put(leader.getUniqueId(), party);

        leader.sendMessage(HexUtils.colorize("&aParty created!"));
    }

    public void disbandParty(Player leader) {
        Party party = getParty(leader);
        if (party == null || !party.getLeader().equals(leader.getUniqueId())) {
            leader.sendMessage(HexUtils.colorize("&cYou are not the leader of a party!"));
            return;
        }

        for (UUID memberId : party.getMembers()) {
            playerPartyMap.remove(memberId);
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage(HexUtils.colorize("&cThe party has been disbanded."));
            }
        }
        partiesByLeader.remove(leader.getUniqueId());
    }

    public void invitePlayer(Player inviter, Player target) {
        Party party = getParty(inviter);
        if (party == null || !party.getLeader().equals(inviter.getUniqueId())) {
            inviter.sendMessage(HexUtils.colorize("&cYou must be the party leader to invite players!"));
            return;
        }

        if (isInParty(target)) {
            inviter.sendMessage(HexUtils.colorize("&cThat player is already in a party!"));
            return;
        }

        if (party.hasInvite(target.getUniqueId())) {
            inviter.sendMessage(HexUtils.colorize("&cYou have already invited this player!"));
            return;
        }

        int maxSize = plugin.getConfig().getInt("party.max-size", 8);
        if (party.getMembers().size() >= maxSize) {
            inviter.sendMessage(HexUtils.colorize("&cParty is full!"));
            return;
        }

        party.addInvite(target.getUniqueId());
        inviter.sendMessage(HexUtils.colorize("&aInvited " + target.getName() + " to the party."));

        TextComponent message = new TextComponent(HexUtils.colorize("&aYou have been invited to join " + inviter.getName() + "'s party. "));
        TextComponent accept = new TextComponent(HexUtils.colorize("&2[Accept]"));
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + inviter.getName()));
        TextComponent space = new TextComponent(" ");
        TextComponent decline = new TextComponent(HexUtils.colorize("&c[Decline]")); // No command for decline, just ignore

        message.addExtra(accept);
        message.addExtra(space);
        message.addExtra(decline);

        target.spigot().sendMessage(message);

        // Expire invite after timeout
        int timeout = plugin.getConfig().getInt("party.invite-timeout", 60);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (party.hasInvite(target.getUniqueId())) {
                party.removeInvite(target.getUniqueId());
                if (target.isOnline()) {
                    target.sendMessage(HexUtils.colorize("&cInvite from " + inviter.getName() + " expired."));
                }
                if (inviter.isOnline()) {
                    inviter.sendMessage(HexUtils.colorize("&cInvite to " + target.getName() + " expired."));
                }
            }
        }, timeout * 20L);
    }

    public void joinParty(Player player, Player leader) {
        if (isInParty(player)) {
            player.sendMessage(HexUtils.colorize("&cYou are already in a party!"));
            return;
        }

        Party party = getParty(leader);
        if (party == null) {
            player.sendMessage(HexUtils.colorize("&cThat player is not in a party!"));
            return;
        }

        if (!party.hasInvite(player.getUniqueId())) {
            player.sendMessage(HexUtils.colorize("&cYou have not been invited to this party!"));
            return;
        }

        int maxSize = plugin.getConfig().getInt("party.max-size", 8);
        if (party.getMembers().size() >= maxSize) {
            player.sendMessage(HexUtils.colorize("&cParty is full!"));
            return;
        }

        party.removeInvite(player.getUniqueId());
        party.addMember(player.getUniqueId());
        playerPartyMap.put(player.getUniqueId(), party);

        broadcast(party, "&a" + player.getName() + " joined the party!");
    }

    public void leaveParty(Player player) {
        Party party = getParty(player);
        if (party == null) {
            player.sendMessage(HexUtils.colorize("&cYou are not in a party!"));
            return;
        }

        party.removeMember(player.getUniqueId());
        playerPartyMap.remove(player.getUniqueId());

        if (party.getMembers().isEmpty()) {
            partiesByLeader.remove(party.getLeader());
        } else {
            if (party.getLeader().equals(player.getUniqueId())) {
                UUID newLeaderId = party.getMembers().iterator().next();
                party.setLeader(newLeaderId);
                partiesByLeader.remove(player.getUniqueId());
                partiesByLeader.put(newLeaderId, party);

                Player newLeader = Bukkit.getPlayer(newLeaderId);
                String newLeaderName = (newLeader != null) ? newLeader.getName() : "someone";
                broadcast(party, "&a" + player.getName() + " left. Leadership transferred to " + newLeaderName + ".");
            } else {
                broadcast(party, "&c" + player.getName() + " left the party.");
            }
        }
        player.sendMessage(HexUtils.colorize("&cYou left the party."));
    }

    public void kickPlayer(Player leader, Player target) {
        Party party = getParty(leader);
        if (party == null || !party.getLeader().equals(leader.getUniqueId())) {
            leader.sendMessage(HexUtils.colorize("&cYou are not the leader!"));
            return;
        }

        if (!party.isMember(target.getUniqueId())) {
            leader.sendMessage(HexUtils.colorize("&cThat player is not in your party!"));
            return;
        }

        if (leader.equals(target)) {
            leader.sendMessage(HexUtils.colorize("&cYou cannot kick yourself! Use /party disband instead."));
            return;
        }

        party.removeMember(target.getUniqueId());
        playerPartyMap.remove(target.getUniqueId());
        target.sendMessage(HexUtils.colorize("&cYou were kicked from the party."));
        broadcast(party, "&c" + target.getName() + " was kicked from the party.");
    }

    public void transferLeadership(Player leader, Player target) {
        Party party = getParty(leader);
        if (party == null || !party.getLeader().equals(leader.getUniqueId())) {
            leader.sendMessage(HexUtils.colorize("&cYou are not the leader!"));
            return;
        }

        if (!party.isMember(target.getUniqueId())) {
            leader.sendMessage(HexUtils.colorize("&cThat player is not in your party!"));
            return;
        }

        partiesByLeader.remove(leader.getUniqueId());
        party.setLeader(target.getUniqueId());
        partiesByLeader.put(target.getUniqueId(), party);

        broadcast(party, "&aLeadership transferred to " + target.getName() + ".");
    }

    public void broadcast(Party party, String message) {
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage(HexUtils.colorize(message));
            }
        }
    }
}
