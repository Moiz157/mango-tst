package me.moiz.mangoparty.config;

import me.moiz.mangoparty.MangoParty;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final MangoParty plugin;

    private File arenaFile;
    private FileConfiguration arenaConfig;

    private File kitFile;
    private FileConfiguration kitConfig;

    private File scoreboardFile;
    private FileConfiguration scoreboardConfig;

    public ConfigManager(MangoParty plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        // Arenas
        arenaFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenaFile.exists()) {
            try {
                arenaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);

        // Kits
        kitFile = new File(plugin.getDataFolder(), "kits.yml");
        if (!kitFile.exists()) {
            try {
                kitFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        kitConfig = YamlConfiguration.loadConfiguration(kitFile);

        // Scoreboard
        scoreboardFile = new File(plugin.getDataFolder(), "scoreboard.yml");
        if (!scoreboardFile.exists()) {
            plugin.saveResource("scoreboard.yml", false);
        }
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }

    public FileConfiguration getArenaConfig() {
        return arenaConfig;
    }

    public void saveArenaConfig() {
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getKitConfig() {
        return kitConfig;
    }

    public void saveKitConfig() {
        try {
            kitConfig.save(kitFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getScoreboardConfig() {
        return scoreboardConfig;
    }

    public void reloadScoreboardConfig() {
        scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardFile);
    }
}
