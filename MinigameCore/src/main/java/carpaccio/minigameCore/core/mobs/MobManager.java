package carpaccio.minigameCore.core.mobs;

import carpaccio.minigameCore.core.loot.LootTable;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobManager {
    private final Map<String, CustomMob> customMobs;
    private final Map<String, LootTable> lootTables;

    // Rastreamento: Entity -> ID do CustomMob
    private final Map<Entity, String> spawnedMobs;

    public MobManager() {
        this.customMobs = new HashMap<>();
        this.lootTables = new HashMap<>();
        this.spawnedMobs = new HashMap<>();
    }

    // ==================== Registro de Mobs e Loot Tables ====================

    public void registerCustomMob(CustomMob mob) {
        customMobs.put(mob.getId(), mob);
    }

    public void registerLootTable(LootTable lootTable) {
        lootTables.put(lootTable.getId(), lootTable);
    }

    // ==================== Getters ====================

    public CustomMob getCustomMob(String id) {
        return customMobs.get(id);
    }

    public LootTable getLootTable(String id) {
        return lootTables.get(id);
    }

    // ==================== Rastreamento de Entidades ====================

    /**
     * Registra uma entidade spawnada e qual CustomMob ela representa
     *
     * @param entity A entidade que foi spawnada
     * @param mobId O ID do CustomMob (usado para buscar loot table depois)
     */
    public void trackSpawnedMob(Entity entity, String mobId) {
        spawnedMobs.put(entity, mobId);
    }

    /**
     * Busca qual CustomMob ID está associado a uma entidade
     * Usado principalmente quando o mob morre para saber qual loot dropar
     *
     * @param entity A entidade a ser consultada
     * @return O ID do CustomMob, ou null se não estiver rastreada
     */
    public String getSpawnedMobId(Entity entity) {
        return spawnedMobs.get(entity);
    }

    /**
     * Remove uma entidade do rastreamento
     * Deve ser chamado quando o mob morre ou é removido
     *
     * @param entity A entidade a ser removida do rastreamento
     */
    public void untrackMob(Entity entity) {
        spawnedMobs.remove(entity);
    }

    /**
     * Verifica se uma entidade está sendo rastreada
     *
     * @param entity A entidade a verificar
     * @return true se está rastreada, false caso contrário
     */
    public boolean isTracked(Entity entity) {
        return spawnedMobs.containsKey(entity);
    }

    /**
     * Retorna quantas entidades estão sendo rastreadas atualmente
     *
     * @return Número total de entidades rastreadas
     */
    public int getTrackedMobCount() {
        return spawnedMobs.size();
    }

    /**
     * Limpa todas as entidades mortas/inválidas do rastreamento
     * Deve ser chamado periodicamente para evitar memory leaks
     *
     * @return Número de entidades limpas
     */
    public int cleanupDeadMobs() {
        int removed = 0;
        List<Entity> toRemove = new ArrayList<>();

        for (Entity entity : spawnedMobs.keySet()) {
            if (entity.isDead() || !entity.isValid()) {
                toRemove.add(entity);
            }
        }

        for (Entity entity : toRemove) {
            spawnedMobs.remove(entity);
            removed++;
        }

        return removed;
    }

    // ==================== Contadores ====================

    public int getCustomMobCount() {
        return customMobs.size();
    }

    public int getLootTableCount() {
        return lootTables.size();
    }

    public List<String> getAllCustomMobIds() {
        return new ArrayList<>(customMobs.keySet());
    }

    public List<String> getAllLootTableIds() {
        return new ArrayList<>(lootTables.keySet());
    }
}