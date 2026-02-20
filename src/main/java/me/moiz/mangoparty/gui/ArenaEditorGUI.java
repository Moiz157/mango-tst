package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ArenaEditorGUI {
    public static final String TITLE = "Arena Editor";

    public static void open(Player player, MangoParty plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        for (Arena arena : plugin.getArenaManager().getArenas()) {
            List<String> lore = new ArrayList<>();
            lore.add("&7Center: &f" + formatLoc(arena.getCenter()));
            lore.add("&7Schematic: &f" + (arena.getSchematicName() == null ? "None" : arena.getSchematicName()));
            lore.add("");
            lore.add("&eLeft-Click to Teleport");
            lore.add("&eRight-Click to Edit (TODO)");
            lore.add("&cShift-Right-Click to Delete");

            inv.addItem(createItem(Material.PAPER, "&a" + arena.getName(), lore));
        }

        // Add "Create New" button?
        ItemStack create = createItem(Material.EMERALD, "&aCreate New Arena", new ArrayList<>());
        inv.setItem(53, create);

        player.openInventory(inv);
    }

    private static String formatLoc(org.bukkit.Location loc) {
        if (loc == null) return "Not Set";
        return loc.getWorld().getName() + ", " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(HexUtils.colorize(name));
            List<String> coloredLore = new ArrayList<>();
            for (String l : lore) coloredLore.add(HexUtils.colorize(l));
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
