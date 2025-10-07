package carpaccio.minigameCore.listeners;

import carpaccio.minigameCore.MinigameCore;
import carpaccio.minigameCore.core.loot.LootTable;
import carpaccio.minigameCore.core.mobs.CustomMob;
import carpaccio.minigameCore.core.mobs.MobManager;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomMobDeathListener implements Listener {
    private final MinigameCore plugin;
    private final MobManager mobManager;

    public CustomMobDeathListener(MinigameCore plugin, MobManager mobManager) {
        this.plugin = plugin;
        this.mobManager = mobManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        plugin.getLogger().info("EntityDeathEvent disparado para: " + entity.getType());
        String mobId = mobManager.getSpawnedMobId(entity);
        plugin.getLogger().info("Mob ID: " + mobId);

        if (mobId != null) {
            CustomMob customMob = mobManager.getCustomMob(mobId);
            plugin.getLogger().info("CustomMob: " + (customMob != null ? customMob.toString() : "null"));

            if (customMob != null && customMob.getLootTableId() != null) {
                plugin.getLogger().info("LootTable ID: " + customMob.getLootTableId());
                LootTable lootTable = mobManager.getLootTable(customMob.getLootTableId());
                plugin.getLogger().info("LootTable: " + (lootTable != null ? lootTable.toString() : "null"));

                if (lootTable != null) {
                    // Remove drops padrão
                    event.getDrops().clear();
                    event.setDroppedExp(0);
                    plugin.getLogger().info("Drops padrão e EXP removidos");

                    // Gera e adiciona drops customizados
                    List<ItemStack> customDrops = lootTable.generateDrops(entity.getLocation());
                    plugin.getLogger().info("Drops gerados: " + customDrops);
                    for (ItemStack drop : customDrops) {
                        plugin.getLogger().info("ItemStack gerado: " + drop.getType() + ", Quantidade: " + drop.getAmount());
                    }
                    event.getDrops().addAll(customDrops);
                } else {
                    plugin.getLogger().warning("LootTable não encontrada para ID: " + customMob.getLootTableId());
                }
            } else {
                plugin.getLogger().warning("CustomMob ou LootTableId é null para mobId: " + mobId);
            }

            // Desrastreia o mob após processar os drops
            mobManager.untrackMob(entity);
            plugin.getLogger().info("Mob desrastreamento: " + entity.getType());
        } else {
            plugin.getLogger().info("Mob não é customizado: " + entity.getType());
        }
    }
}
