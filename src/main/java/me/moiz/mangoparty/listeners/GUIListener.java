package me.moiz.mangoparty.listeners;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.gui.ArenaEditorGUI;
import me.moiz.mangoparty.gui.KitEditorGUI;
import me.moiz.mangoparty.gui.KitSelectorGUI;
import me.moiz.mangoparty.gui.MatchTypeGUI;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.Match;
import me.moiz.mangoparty.models.Party;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GUIListener implements Listener {
    private final MangoParty plugin;

    public GUIListener(MangoParty plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(MatchTypeGUI.TITLE)) {
            event.setCancelled(true);
            handleMatchTypeClick(player, event.getCurrentItem());
        } else if (title.startsWith(KitSelectorGUI.TITLE_PREFIX)) {
            event.setCancelled(true);
            handleKitSelectorClick(player, title, event.getCurrentItem());
        } else if (title.equals(ArenaEditorGUI.TITLE)) {
            event.setCancelled(true);
            handleArenaEditorClick(player, event.getCurrentItem());
        } else if (title.equals(KitEditorGUI.TITLE_LIST)) {
            event.setCancelled(true);
            handleKitEditorListClick(player, event.getCurrentItem());
        } else if (title.startsWith(KitEditorGUI.TITLE_SETTINGS_PREFIX)) {
            event.setCancelled(true);
            handleKitSettingsClick(player, title, event.getSlot());
        }
    }

    private void handleMatchTypeClick(Player player, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;

        Party party = plugin.getPartyManager().getParty(player);
        if (party == null || !party.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(HexUtils.colorize("&cYou must be a party leader."));
            player.closeInventory();
            return;
        }

        // Check if party has enough members?
        // Split: need >= 2.
        // FFA: need >= 2.

        if (item.getType() == Material.DIAMOND_SWORD) { // Split
            if (party.getMembers().size() < 2) {
                player.sendMessage(HexUtils.colorize("&cNeed at least 2 members for Split Match."));
                return;
            }
            // Open Kit Selector for Split
            KitSelectorGUI.open(player, plugin, "Split");
        } else if (item.getType() == Material.GOLDEN_SWORD) { // FFA
             if (party.getMembers().size() < 2) {
                player.sendMessage(HexUtils.colorize("&cNeed at least 2 members for FFA."));
                return;
            }
            KitSelectorGUI.open(player, plugin, "FFA");
        } else if (item.getType() == Material.IRON_SWORD) { // Duel
            player.sendMessage(HexUtils.colorize("&eTo challenge another party, use /party challenge <leader>"));
            player.closeInventory();
        }
    }

    private void handleKitSelectorClick(Player player, String title, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String kitName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) return;

        // Parse context
        // Title: "Select Kit (Context)"
        String context = title.substring(title.indexOf("(") + 1, title.lastIndexOf(")"));

        player.closeInventory();

        if (context.startsWith("Queue")) {
            int teamSize = 1;
            if (context.equals("Queue2v2")) teamSize = 2;
            else if (context.equals("Queue3v3")) teamSize = 3;

            plugin.getQueueManager().addToQueue(player, kit, teamSize);

        } else if (context.startsWith("Duel:")) {
            String targetName = context.substring(5);
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) {
                plugin.getDuelManager().challenge(player, target, kit, 1, false); // Default 1 round
            } else {
                player.sendMessage(HexUtils.colorize("&cPlayer not found."));
            }

        } else if (context.startsWith("PartyDuel:")) {
            String targetName = context.substring(10);
            Player target = Bukkit.getPlayer(targetName);
            if (target != null) {
                plugin.getDuelManager().challenge(player, target, kit, 1, true); // Party duel
            } else {
                player.sendMessage(HexUtils.colorize("&cPlayer not found."));
            }

        } else if (context.equals("Split")) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party != null) {
                // Create random teams
                List<UUID> members = new ArrayList<>(party.getMembers());
                java.util.Collections.shuffle(members);

                Set<UUID> team1 = new HashSet<>();
                Set<UUID> team2 = new HashSet<>();

                for (int i = 0; i < members.size(); i++) {
                    if (i < members.size() / 2) team1.add(members.get(i));
                    else team2.add(members.get(i));
                }

                List<Set<UUID>> teams = new ArrayList<>();
                teams.add(team1);
                teams.add(team2);

                Arena arena = plugin.getArenaManager().getAvailableArena("default"); // Basic
                // Find ANY arena
                if (arena == null) {
                    for (Arena a : plugin.getArenaManager().getArenas()) {
                        arena = plugin.getArenaManager().getAvailableArena(a.getName());
                        if (arena != null) break;
                    }
                }

                if (arena != null) {
                    plugin.getMatchManager().startMatch(arena, kit, teams, Match.Type.SPLIT, 1);
                } else {
                    player.sendMessage(HexUtils.colorize("&cNo arenas available."));
                }
            }

        } else if (context.equals("FFA")) {
            Party party = plugin.getPartyManager().getParty(player);
            if (party != null) {
                List<Set<UUID>> teams = new ArrayList<>();
                for (UUID uuid : party.getMembers()) {
                    Set<UUID> team = new HashSet<>();
                    team.add(uuid);
                    teams.add(team);
                }

                Arena arena = plugin.getArenaManager().getAvailableArena("default");
                if (arena == null) {
                    for (Arena a : plugin.getArenaManager().getArenas()) {
                        arena = plugin.getArenaManager().getAvailableArena(a.getName());
                        if (arena != null) break;
                    }
                }

                if (arena != null) {
                    plugin.getMatchManager().startMatch(arena, kit, teams, Match.Type.FFA, 1);
                } else {
                    player.sendMessage(HexUtils.colorize("&cNo arenas available."));
                }
            }
        }
    }

    private void handleArenaEditorClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (name.equals("Create New Arena")) {
             player.closeInventory();
             player.sendMessage(HexUtils.colorize("&eUse /mango arena create <name> to create an arena."));
             return;
        }

        Arena arena = plugin.getArenaManager().getArena(name);
        if (arena != null) {
            if (arena.getCenter() != null) {
                player.teleport(arena.getCenter());
                player.sendMessage(HexUtils.colorize("&aTeleported to " + arena.getName()));
                player.closeInventory();
            } else {
                player.sendMessage(HexUtils.colorize("&cCenter not set for " + arena.getName()));
            }
        }
    }

    private void handleKitEditorListClick(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Kit kit = plugin.getKitManager().getKit(name);
        if (kit != null) {
            KitEditorGUI.openKitSettings(player, kit);
        }
    }

    private void handleKitSettingsClick(Player player, String title, int slot) {
        String kitName = title.substring(KitEditorGUI.TITLE_SETTINGS_PREFIX.length());
        Kit kit = plugin.getKitManager().getKit(kitName);
        if (kit == null) return;

        if (slot == 22) { // Back
            KitEditorGUI.open(player, plugin);
            return;
        }

        // Toggle rules
        if (slot == 10) {
            kit.getRules().setNaturalRegen(!kit.getRules().isNaturalRegen());
        } else if (slot == 12) {
            kit.getRules().setAllowBlockBreak(!kit.getRules().isAllowBlockBreak());
        } else if (slot == 14) {
            kit.getRules().setAllowBlockPlace(!kit.getRules().isAllowBlockPlace());
        } else if (slot == 16) {
             // Damage multiplier
             // Not implemented toggle, requires input or cyclic change
             // Let's increment by 0.1
             double current = kit.getRules().getDamageMultiplier();
             current += 0.5;
             if (current > 3.0) current = 0.5;
             kit.getRules().setDamageMultiplier(current);
        } else if (slot == 18) {
            kit.getRules().setInstantTNT(!kit.getRules().isInstantTNT());
        }

        plugin.getKitManager().saveKits();
        KitEditorGUI.openKitSettings(player, kit); // Refresh
    }
}
