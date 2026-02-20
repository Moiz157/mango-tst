package me.moiz.mangoparty.managers;

import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Kit;
import me.moiz.mangoparty.models.KitRules;
import me.moiz.mangoparty.utils.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class KitManager {
    private final MangoParty plugin;
    private final Map<String, Kit> kits = new HashMap<>();

    public KitManager(MangoParty plugin) {
        this.plugin = plugin;
        loadKits();
    }

    public void loadKits() {
        FileConfiguration config = plugin.getConfigManager().getKitConfig();
        ConfigurationSection section = config.getConfigurationSection("kits");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection kitSection = section.getConfigurationSection(key);
            if (kitSection == null) continue;

            Kit kit = new Kit(key);
            String iconName = kitSection.getString("icon", "IRON_SWORD");
            try {
                kit.setIcon(Material.valueOf(iconName));
            } catch (IllegalArgumentException e) {
                kit.setIcon(Material.IRON_SWORD);
            }

            // Load Inventory
            List<?> invList = kitSection.getList("inventory");
            if (invList != null) {
                List<ItemStack> items = (List<ItemStack>) invList;
                kit.setInventory(items.toArray(new ItemStack[0]));
            }

            // Load Rules
            KitRules rules = kit.getRules();
            ConfigurationSection rulesSection = kitSection.getConfigurationSection("rules");
            if (rulesSection != null) {
                rules.setNaturalRegen(rulesSection.getBoolean("natural-regen", true));
                rules.setAllowBlockBreak(rulesSection.getBoolean("allow-block-break", false));
                rules.setAllowBlockPlace(rulesSection.getBoolean("allow-block-place", false));
                rules.setDamageMultiplier(rulesSection.getDouble("damage-multiplier", 1.0));
                rules.setInstantTNT(rulesSection.getBoolean("instant-tnt", false));
            }

            kits.put(key, kit);
        }
    }

    public void saveKits() {
        FileConfiguration config = plugin.getConfigManager().getKitConfig();
        config.set("kits", null);

        for (Kit kit : kits.values()) {
            String path = "kits." + kit.getName();
            config.set(path + ".icon", kit.getIcon().name());

            if (kit.getInventory() != null) {
                config.set(path + ".inventory", Arrays.asList(kit.getInventory()));
            }

            KitRules rules = kit.getRules();
            String rulesPath = path + ".rules";
            config.set(rulesPath + ".natural-regen", rules.isNaturalRegen());
            config.set(rulesPath + ".allow-block-break", rules.isAllowBlockBreak());
            config.set(rulesPath + ".allow-block-place", rules.isAllowBlockPlace());
            config.set(rulesPath + ".damage-multiplier", rules.getDamageMultiplier());
            config.set(rulesPath + ".instant-tnt", rules.isInstantTNT());
        }
        plugin.getConfigManager().saveKitConfig();
    }

    public void createKit(String name, Player source) {
        Kit kit = new Kit(name);
        kit.setInventory(InventoryUtils.saveInventory(source));
        kits.put(name, kit);
        saveKits();
    }

    public void deleteKit(String name) {
        kits.remove(name);
        saveKits();
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public Collection<Kit> getKits() {
        return kits.values();
    }

    public void applyKit(Player player, Kit kit) {
        if (kit == null) return;
        InventoryUtils.restoreInventory(player, InventoryUtils.deepCopy(kit.getInventory()));
        // Rules are handled in listeners, not applied here.
    }
}
