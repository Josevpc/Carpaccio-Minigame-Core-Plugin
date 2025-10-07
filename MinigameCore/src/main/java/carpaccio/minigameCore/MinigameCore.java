package carpaccio.minigameCore;

import carpaccio.minigameCore.commands.MiniCoreCommand;
import carpaccio.minigameCore.core.MobSpawnManager;
import carpaccio.minigameCore.core.mobs.MobManager;
import carpaccio.minigameCore.listeners.CustomMobDeathListener;
import carpaccio.minigameCore.manager.ConfigManager;
import carpaccio.minigameCore.utils.RegionPreview;
import carpaccio.minigameCore.utils.SelectionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class MinigameCore extends JavaPlugin {

    private MobSpawnManager spawnManager;

    @Override
    public void onEnable() {

        // Starting Managers
        MobManager mobManager = new MobManager();
        spawnManager = new MobSpawnManager(this, mobManager);
        SelectionManager selectionManager = new SelectionManager();
        RegionPreview regionPreview = new RegionPreview(this);
        CustomMobDeathListener deathListener = new CustomMobDeathListener(this, mobManager);

        // Config Manager
        //ConfigManager.setupConfig(this);

        // Load Configs
        ConfigManager.loadLootTables(this, getConfig(), mobManager);
        ConfigManager.loadCustomMobs(getConfig() , mobManager);

        // Listeners
        Bukkit.getPluginManager().registerEvents(selectionManager, this);
        Bukkit.getPluginManager().registerEvents(deathListener, this);

        // Commands
        getCommand("minicore").setExecutor(new MiniCoreCommand(spawnManager, selectionManager, regionPreview));

        // Loggers
        getLogger().info("Minigame Core has started!");
        getLogger().info("Loaded Areas: " + spawnManager.getAreaNames().size());
        getLogger().info("Mobs customizados carregados: " + mobManager.getCustomMobCount());
        getLogger().info("LootTables carregadas:" + mobManager.getLootTableCount());
    }

    @Override
    public void onDisable() {
        if (spawnManager != null) {
            spawnManager.shutdownAll();
        }

        getLogger().info("Minigame Core Deactivated!");
    }
}