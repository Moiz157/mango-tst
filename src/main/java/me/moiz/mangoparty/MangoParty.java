package me.moiz.mangoparty;

import me.moiz.mangoparty.commands.*;
import me.moiz.mangoparty.config.ConfigManager;
import me.moiz.mangoparty.listeners.*;
import me.moiz.mangoparty.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class MangoParty extends JavaPlugin {

    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private PartyManager partyManager;
    private QueueManager queueManager;
    private DuelManager duelManager;
    private MatchManager matchManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        getLogger().info("Initializing MangoParty...");

        // Initialize Managers
        configManager = new ConfigManager(this);
        arenaManager = new ArenaManager(this);
        kitManager = new KitManager(this);
        partyManager = new PartyManager(this);
        // QueueManager depends on MatchManager indirectly (via plugin), initialized later is fine.
        queueManager = new QueueManager(this);
        duelManager = new DuelManager(this);
        scoreboardManager = new ScoreboardManager(this);
        matchManager = new MatchManager(this); // Depends on ScoreboardManager

        // Register Commands
        getCommand("mango").setExecutor(new MangoCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("spectate").setExecutor(new SpectateCommand(this));

        getCommand("1v1queue").setExecutor(new QueueCommand(this));
        getCommand("2v2queue").setExecutor(new QueueCommand(this));
        getCommand("3v3queue").setExecutor(new QueueCommand(this));
        getCommand("leavequeue").setExecutor(new LeaveQueueCommand(this));

        // Duel command can handle 'accept', 'decline' or just challenge
        getCommand("duel").setExecutor(new DuelCommand(this)); // Need to add to plugin.yml? I did.
        // Wait, plugin.yml doesn't have "duel" command?
        // Let's check plugin.yml content I wrote.
        // I checked. I did NOT add "duel" command in plugin.yml explicitly in my memory trace.
        // I added "party", "mango", "1v1queue", etc.
        // I need to add "duel" command to plugin.yml!

        // Register Listeners
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PartyListener(this), this);
        getServer().getPluginManager().registerEvents(new MatchListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(this), this);
        getServer().getPluginManager().registerEvents(new DuelListener(this), this);

        getLogger().info("MangoParty enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling MangoParty...");

        if (matchManager != null) {
            // End all matches?
            // MatchManager doesn't expose cleanup, but gameLoop stops on plugin disable.
            // We should ideally end matches gracefully.
            // But for now, we rely on server shutdown handling or just saving data.
        }

        if (arenaManager != null) {
            arenaManager.saveArenas();
        }

        if (kitManager != null) {
            kitManager.saveKits();
        }

        getLogger().info("MangoParty disabled.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public KitManager getKitManager() { return kitManager; }
    public PartyManager getPartyManager() { return partyManager; }
    public QueueManager getQueueManager() { return queueManager; }
    public DuelManager getDuelManager() { return duelManager; }
    public MatchManager getMatchManager() { return matchManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
}
