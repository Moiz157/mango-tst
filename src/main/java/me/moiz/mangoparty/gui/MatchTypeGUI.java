package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class MatchTypeGUI {
    public static final String TITLE = "Select Match Type";

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        inv.setItem(11, createItem(Material.DIAMOND_SWORD, "&b&lSplit Match", "&7Two teams from your party fight."));
        inv.setItem(13, createItem(Material.GOLDEN_SWORD, "&e&lParty FFA", "&7Free for all within your party."));
        inv.setItem(15, createItem(Material.IRON_SWORD, "&f&lParty Duel", "&7Challenge another party leader."));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(HexUtils.colorize(name));
        meta.setLore(Arrays.stream(lore).map(HexUtils::colorize).toList());
        item.setItemMeta(meta);
        return item;
    }
}
