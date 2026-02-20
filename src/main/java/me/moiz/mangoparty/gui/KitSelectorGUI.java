package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KitSelectorGUI {
    public static final String TITLE_PREFIX = "Select Kit";

    public static void open(Player player, MangoParty plugin, String context) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + " (" + context + ")");

        for (Kit kit : plugin.getKitManager().getKits()) {
            List<String> lore = new ArrayList<>();
            // Show queue stats
            if (context.contains("Queue")) {
                lore.add("&7In Queue (1v1): &e" + plugin.getQueueManager().getQueueSize(kit, 1));
                lore.add("&7In Queue (2v2): &e" + plugin.getQueueManager().getQueueSize(kit, 2));
                lore.add("&7In Queue (3v3): &e" + plugin.getQueueManager().getQueueSize(kit, 3));
            } else {
                lore.add("&7Click to select.");
            }

            inv.addItem(createItem(kit.getIcon(), "&e" + kit.getName(), lore));
        }

        player.openInventory(inv);
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
