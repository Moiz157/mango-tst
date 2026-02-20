package me.moiz.mangoparty.models;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Kit {
    private final String name;
    private Material icon = Material.IRON_SWORD;
    private ItemStack[] inventory;
    private KitRules rules;

    public Kit(String name) {
        this.name = name;
        this.rules = new KitRules();
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public ItemStack[] getInventory() {
        return inventory;
    }

    public void setInventory(ItemStack[] inventory) {
        this.inventory = inventory;
    }

    public KitRules getRules() {
        return rules;
    }

    public void setRules(KitRules rules) {
        this.rules = rules;
    }
}
