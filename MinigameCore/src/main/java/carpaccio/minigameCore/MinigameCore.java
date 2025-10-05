package carpaccio.minigameCore;

import carpaccio.minigameCore.commands.MiniCoreCommand;
import carpaccio.minigameCore.core.MobSpawnManager;
import carpaccio.minigameCore.manager.ConfigManager;
import carpaccio.minigameCore.utils.RegionPreview;
import carpaccio.minigameCore.utils.SelectionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class MinigameCore extends JavaPlugin {

    private MobSpawnManager spawnManager;

    @Override
    public void onEnable() {

        spawnManager = new MobSpawnManager(this);
        SelectionManager selectionManager = new SelectionManager();
        RegionPreview regionPreview = new RegionPreview(this);

        // Config Manager
        //ConfigManager.setupConfig(this);

        // Listeners
        Bukkit.getPluginManager().registerEvents(selectionManager, this);

        // Commands
        getCommand("minicore").setExecutor(new MiniCoreCommand(spawnManager, selectionManager, regionPreview));

        // Loggers
        getLogger().info("Minigame Core has started!");
        getLogger().info("Loaded Areas: " + spawnManager.getAreaNames().size());
    }

    @Override
    public void onDisable() {
        if (spawnManager != null) {
            spawnManager.shutdownAll();
        }

        getLogger().info("Minigame Core Deactivated!");
    }
}