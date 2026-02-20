package me.moiz.mangoparty.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    /**
     * Deep copies an array of ItemStacks.
     * @param items The items to copy.
     * @return A deep copy of the items.
     */
    public static ItemStack[] deepCopy(ItemStack[] items) {
        if (items == null) return null;
        ItemStack[] copy = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                copy[i] = items[i].clone();
            }
        }
        return copy;
    }

    /**
     * Saves a player's inventory (contents and armor).
     * @param player The player.
     * @return An array containing contents.
     */
    public static ItemStack[] saveInventory(Player player) {
        return deepCopy(player.getInventory().getContents());
    }

    /**
     * Restores a player's inventory from a saved array.
     * @param player The player.
     * @param items The saved items.
     */
    public static void restoreInventory(Player player, ItemStack[] items) {
        if (items == null) return;
        player.getInventory().setContents(items);
    }
}
