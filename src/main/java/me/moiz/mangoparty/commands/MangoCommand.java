package me.moiz.mangoparty.commands;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.regions.Region;
import me.moiz.mangoparty.MangoParty;
import me.moiz.mangoparty.gui.ArenaEditorGUI;
import me.moiz.mangoparty.gui.KitEditorGUI;
import me.moiz.mangoparty.models.Arena;
import me.moiz.mangoparty.utils.HexUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MangoCommand implements CommandExecutor {
    private final MangoParty plugin;

    public MangoCommand(MangoParty plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mangoparty.admin")) {
            sender.sendMessage(HexUtils.colorize("&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(HexUtils.colorize("&cUsage: /mango <arena|kit|setspawn> ..."));
            return true;
        }

        if (args[0].equalsIgnoreCase("setspawn")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(HexUtils.colorize("&cOnly players can use this command."));
                return true;
            }
            Player player = (Player) sender;
            plugin.getConfig().set("spawn", player.getLocation());
            plugin.saveConfig();
            player.sendMessage(HexUtils.colorize("&aGlobal spawn set!"));
            return true;
        }

        if (args[0].equalsIgnoreCase("kit")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("editor")) {
                if (!(sender instanceof Player)) return true;
                KitEditorGUI.open((Player) sender, plugin);
                return true;
            }
            // /mango create kit <name> logic handled below or here?
            // "create kit" is separate subcommand?
        }

        if (args[0].equalsIgnoreCase("create") && args.length > 2 && args[1].equalsIgnoreCase("kit")) {
            // /mango create kit <name>
            if (!(sender instanceof Player)) return true;
            String name = args[2];
            plugin.getKitManager().createKit(name, (Player) sender);
            sender.sendMessage(HexUtils.colorize("&aKit " + name + " created from your inventory."));
            return true;
        }

        if (args[0].equalsIgnoreCase("arena")) {
            if (args.length < 2) {
                sender.sendMessage(HexUtils.colorize("&cUsage: /mango arena <subcommand>"));
                return true;
            }

            if (args[1].equalsIgnoreCase("editor")) {
                if (!(sender instanceof Player)) return true;
                ArenaEditorGUI.open((Player) sender, plugin);
                return true;
            }

            if (args[1].equalsIgnoreCase("list")) {
                sender.sendMessage(HexUtils.colorize("&eArenas: " + String.join(", ", plugin.getArenaManager().getArenas().stream().map(Arena::getName).toList())));
                return true;
            }

            if (args.length < 3) {
                sender.sendMessage(HexUtils.colorize("&cUsage: /mango arena <create|corner1|corner2|center|spawn1|spawn2|save|delete> <name>"));
                return true;
            }

            String action = args[1];
            String name = args[2];
            Arena arena = plugin.getArenaManager().getArena(name);

            if (action.equalsIgnoreCase("create")) {
                if (arena != null) {
                    sender.sendMessage(HexUtils.colorize("&cArena " + name + " already exists!"));
                    return true;
                }
                plugin.getArenaManager().createArena(name);
                sender.sendMessage(HexUtils.colorize("&aArena " + name + " created."));
                return true;
            }

            if (arena == null) {
                sender.sendMessage(HexUtils.colorize("&cArena " + name + " does not exist!"));
                return true;
            }

            if (action.equalsIgnoreCase("delete")) {
                plugin.getArenaManager().deleteArena(name);
                sender.sendMessage(HexUtils.colorize("&aArena " + name + " deleted."));
                return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(HexUtils.colorize("&cOnly players can use this command."));
                return true;
            }
            Player player = (Player) sender;

            if (action.equalsIgnoreCase("corner1")) {
                arena.setCorner1(player.getLocation());
                sender.sendMessage(HexUtils.colorize("&aCorner 1 set for " + name));
            } else if (action.equalsIgnoreCase("corner2")) {
                arena.setCorner2(player.getLocation());
                sender.sendMessage(HexUtils.colorize("&aCorner 2 set for " + name));
            } else if (action.equalsIgnoreCase("center")) {
                arena.setCenter(player.getLocation());
                sender.sendMessage(HexUtils.colorize("&aCenter set for " + name));
            } else if (action.equalsIgnoreCase("spawn1")) {
                arena.setSpawn1(player.getLocation());
                sender.sendMessage(HexUtils.colorize("&aSpawn 1 set for " + name));
            } else if (action.equalsIgnoreCase("spawn2")) {
                arena.setSpawn2(player.getLocation());
                sender.sendMessage(HexUtils.colorize("&aSpawn 2 set for " + name));
            } else if (action.equalsIgnoreCase("save")) {
                try {
                    com.sk89q.worldedit.entity.Player actor = BukkitAdapter.adapt(player);
                    Region region;

                    // Use selection if available, otherwise use arena corners
                    try {
                        region = WorldEdit.getInstance().getSessionManager().get(actor).getSelection(actor.getWorld());
                    } catch (IncompleteRegionException e) {
                        region = null;
                    }

                    if (region == null) {
                        if (arena.getCorner1() != null && arena.getCorner2() != null) {
                            region = new com.sk89q.worldedit.regions.CuboidRegion(
                                BukkitAdapter.adapt(arena.getCorner1().getWorld()),
                                BukkitAdapter.asBlockVector(arena.getCorner1()),
                                BukkitAdapter.asBlockVector(arena.getCorner2())
                            );
                        }
                    }

                    if (region == null) {
                        sender.sendMessage(HexUtils.colorize("&cPlease make a WorldEdit selection or set arena corners first!"));
                        return true;
                    }

                    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

                    try (com.sk89q.worldedit.EditSession editSession = WorldEdit.getInstance().newEditSession(actor.getWorld())) {
                        com.sk89q.worldedit.function.operation.ForwardExtentCopy copy = new com.sk89q.worldedit.function.operation.ForwardExtentCopy(
                            editSession, region, clipboard, region.getMinimumPoint()
                        );
                        // Configure copy?
                        copy.setCopyingBiomes(true);
                        com.sk89q.worldedit.function.operation.Operations.complete(copy);
                    }

                    File schematicDir = new File(plugin.getDataFolder(), "schematics");
                    if (!schematicDir.exists()) schematicDir.mkdirs();

                    File schemFile = new File(schematicDir, name + ".schem");

                    try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schemFile))) {
                        writer.write(clipboard);
                    }

                    arena.setSchematicName(name);
                    plugin.getArenaManager().saveArenas();
                    sender.sendMessage(HexUtils.colorize("&aSchematic saved as " + name + ".schem"));

                } catch (Exception e) {
                    sender.sendMessage(HexUtils.colorize("&cError saving schematic: " + e.getMessage()));
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage(HexUtils.colorize("&cUnknown action: " + action));
            }
            plugin.getArenaManager().saveArenas();
            return true;
        }

        return true;
    }
}
