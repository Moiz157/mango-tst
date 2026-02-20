package me.moiz.mangoparty.gui;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.KitRules;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KitEditorGUI {
    public static final String TITLE_LIST = "Kit Editor";
    public static final String TITLE_SETTINGS_PREFIX = "Kit Settings: ";

    public static void open(Player player, MangoParty plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_LIST);

        for (Kit kit : plugin.getKitManager().getKits()) {
            List<String> lore = new ArrayList<>();
            lore.add("&7Click to Edit Rules");
            inv.addItem(createItem(kit.getIcon(), "&e" + kit.getName(), lore));
        }

        player.openInventory(inv);
    }

    public static void openKitSettings(Player player, Kit kit) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_SETTINGS_PREFIX + kit.getName());
        KitRules rules = kit.getRules();

        inv.setItem(10, createRuleItem("Natural Regeneration", rules.isNaturalRegen()));
        inv.setItem(12, createRuleItem("Block Breaking", rules.isAllowBlockBreak()));
        inv.setItem(14, createRuleItem("Block Placing", rules.isAllowBlockPlace()));
        inv.setItem(16, createItem(Material.DIAMOND_SWORD, "&eDamage Multiplier: &f" + rules.getDamageMultiplier(), new ArrayList<>()));
        inv.setItem(18, createRuleItem("Instant TNT", rules.isInstantTNT()));

        inv.setItem(22, createItem(Material.BARRIER, "&cBack", new ArrayList<>()));

        player.openInventory(inv);
    }

    private static ItemStack createRuleItem(String name, boolean value) {
        Material mat = value ? Material.LIME_WOOL : Material.RED_WOOL;
        String color = value ? "&a" : "&c";
        String status = value ? "Enabled" : "Disabled";
        List<String> lore = new ArrayList<>();
        lore.add(color + status);
        lore.add("&7Click to toggle");
        return createItem(mat, "&e" + name, lore);
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
