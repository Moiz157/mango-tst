package me.moiz.mangoparty.managers;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.models.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaManager {
    private final MangoParty plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<String, List<Arena>> instances = new ConcurrentHashMap<>();
    private final File schematicDir;

    public ArenaManager(MangoParty plugin) {
        this.plugin = plugin;
        this.schematicDir = new File(plugin.getDataFolder(), "schematics");
        if (!schematicDir.exists()) {
            schematicDir.mkdirs();
        }
        loadArenas();
    }

    public void loadArenas() {
        FileConfiguration config = plugin.getConfigManager().getArenaConfig();
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection arenaSection = section.getConfigurationSection(key);
            if (arenaSection == null) continue;

            Arena arena = new Arena(key);
            arena.setCorner1(deserializeLocation(arenaSection.getString("corner1")));
            arena.setCorner2(deserializeLocation(arenaSection.getString("corner2")));
            arena.setCenter(deserializeLocation(arenaSection.getString("center")));
            arena.setSpawn1(deserializeLocation(arenaSection.getString("spawn1")));
            arena.setSpawn2(deserializeLocation(arenaSection.getString("spawn2")));
            arena.setSchematicName(arenaSection.getString("schematic"));

            arenas.put(key, arena);
            instances.put(key, new ArrayList<>());
        }
    }

    public void saveArenas() {
        FileConfiguration config = plugin.getConfigManager().getArenaConfig();
        config.set("arenas", null); // Clear existing

        for (Arena arena : arenas.values()) {
            String path = "arenas." + arena.getName();
            config.set(path + ".corner1", serializeLocation(arena.getCorner1()));
            config.set(path + ".corner2", serializeLocation(arena.getCorner2()));
            config.set(path + ".center", serializeLocation(arena.getCenter()));
            config.set(path + ".spawn1", serializeLocation(arena.getSpawn1()));
            config.set(path + ".spawn2", serializeLocation(arena.getSpawn2()));
            config.set(path + ".schematic", arena.getSchematicName());
        }
        plugin.getConfigManager().saveArenaConfig();
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public void createArena(String name) {
        arenas.put(name, new Arena(name));
        instances.put(name, new ArrayList<>());
        saveArenas();
    }

    public void deleteArena(String name) {
        arenas.remove(name);
        instances.remove(name);
        saveArenas();
    }

    public synchronized Arena getAvailableArena(String name) {
        Arena original = arenas.get(name);
        if (original == null) return null;

        if (!original.isActive()) {
            return original;
        }

        List<Arena> arenaInstances = instances.get(name);
        for (Arena instance : arenaInstances) {
            if (!instance.isActive()) {
                return instance;
            }
        }

        // Create new instance
        return createInstance(original, arenaInstances.size() + 1);
    }

    private Arena createInstance(Arena original, int index) {
        Arena instance = new Arena(original.getName() + "_inst_" + index);
        instance.setParent(original);
        instance.setInstance(true);
        instance.setSchematicName(original.getSchematicName());

        int xOffset = plugin.getConfig().getInt("arena.instance-x-offset", 200) * index;
        int zOffset = plugin.getConfig().getInt("arena.instance-z-offset", 0) * index;

        instance.setCorner1(offsetLocation(original.getCorner1(), xOffset, zOffset));
        instance.setCorner2(offsetLocation(original.getCorner2(), xOffset, zOffset));
        instance.setCenter(offsetLocation(original.getCenter(), xOffset, zOffset));
        instance.setSpawn1(offsetLocation(original.getSpawn1(), xOffset, zOffset));
        instance.setSpawn2(offsetLocation(original.getSpawn2(), xOffset, zOffset));

        instances.get(original.getName()).add(instance);

        // Paste schematic
        pasteSchematic(instance);

        return instance;
    }

    private Location offsetLocation(Location loc, int x, int z) {
        if (loc == null) return null;
        return new Location(loc.getWorld(), loc.getX() + x, loc.getY(), loc.getZ() + z, loc.getYaw(), loc.getPitch());
    }

    public void pasteSchematic(Arena arena) {
        String schemName = arena.getSchematicName();
        if (schemName == null) return;

        File schemFile = new File(schematicDir, schemName + ".schem"); // Try .schem
        if (!schemFile.exists()) {
            schemFile = new File(schematicDir, schemName + ".schematic"); // Try .schematic
        }

        if (!schemFile.exists()) {
            plugin.getLogger().warning("Schematic file not found: " + schemName);
            return;
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
        if (format == null) {
             plugin.getLogger().warning("Invalid schematic format: " + schemName);
             return;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(schemFile))) {
            Clipboard clipboard = reader.read();

            Location center = arena.getCenter();
            if (center == null) {
                plugin.getLogger().warning("Arena center not set for: " + arena.getName());
                return;
            }

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(center.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(center.getX(), center.getY(), center.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper serialization
    private String serializeLocation(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location deserializeLocation(String s) {
        if (s == null) return null;
        String[] parts = s.split(",");
        if (parts.length != 6) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }
}
