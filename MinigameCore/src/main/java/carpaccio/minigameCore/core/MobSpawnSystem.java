package carpaccio.minigameCore.core;

import carpaccio.minigameCore.MinigameCore;

import carpaccio.minigameCore.core.mobs.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.Random;

/**
 * Sistema de spawn de mobs em área delimitada (usando Cuboid)
 * Pode ser instanciado e gerenciado de forma independente
 */
public class MobSpawnSystem {

    private final MinigameCore plugin;

    private final Set<UUID> spawnedMobs;
    private SpawnArea area;
    private final MobManager mobManager;

    private int spawnTaskId;
    private int checkTaskId;
    private boolean isActive;

    // ==========================================
    // CONSTRUTOR
    // ==========================================

    public MobSpawnSystem(MinigameCore plugin, SpawnArea area, MobManager mobManager) {
        this.plugin = plugin;

        this.spawnedMobs = new HashSet<>();
        this.area = area;
        this.mobManager = mobManager;

        this.spawnTaskId = -1;
        this.checkTaskId = -1;
        this.isActive = false;
    }

    // ==========================================
    // GETTERS / SETTERS
    // ==========================================

    /** Retorna o Spawn Area */
    public SpawnArea getArea() { return area; }

    /** Define a Spawn Area diretamente */
    public void setArea(SpawnArea area) { this.area = area; }

    public boolean isActive() { return isActive; }
    public int getMobCount() { return spawnedMobs.size(); }
    public Set<UUID> getSpawnedMobs() { return new HashSet<>(spawnedMobs); }

    // ==========================================
    // PRINCIPAIS
    // ==========================================

    /**
     * Inicia o sistema de spawn
     * @return true se iniciado com sucesso, false se a área não foi definida
     */
    public boolean start() {
        if (area.getRegion() == null) {
            plugin.getLogger().info("Error, null Region!");
            return false;
        }
        if (isActive) stop();

        spawnTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (spawnedMobs.size() < area.getMaxMobs()) {
                spawnRandomMob();
            }
        }, 0L, area.getSpawnInterval());

        checkTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                this::checkMobsLocation, 0L, area.getCheckInterval());

        isActive = true;
        plugin.getLogger().info("Spawning at: "+ area.getRegionName());
        return true;
    }

    /** Para o sistema de spawn */
    public void stop() {
        if (spawnTaskId != -1) {
            Bukkit.getScheduler().cancelTask(spawnTaskId);
            spawnTaskId = -1;
        }
        if (checkTaskId != -1) {
            Bukkit.getScheduler().cancelTask(checkTaskId);
            checkTaskId = -1;
        }
        isActive = false;
    }

    /** Reinicia (útil ao mudar configs) */
    public void restart() {
        if (isActive) {
            stop();
            start();
        }
    }

    /** Remove todos os mobs spawnados pelo sistema */
    public int clearAllMobs() {
        int count = 0;
        for (UUID uuid : new HashSet<>(spawnedMobs)) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
                mobManager.untrackMob(entity);
                count++;
            }
        }
        spawnedMobs.clear();
        return count;
    }

    /** Limpa recursos ao desativar */
    public void shutdown() {
        stop();
        clearAllMobs();
    }

    // ==========================================
    // PRIVADAS
    // ==========================================

    /** Spawna um mob aleatório dentro do Cuboid */
    private void spawnRandomMob() {
        if (area.getRegion() == null || area.getMobList() == null || area.getMobList().length == 0) return;

        Location spawnLoc = getRandomLocationInsideRegion();
        if (spawnLoc == null) return;

        String mobId = area.getMobList()[new Random().nextInt(area.getMobList().length)];
        try {
            Entity entity = mobManager.getCustomMob(mobId).spawn(spawnLoc);
            if (entity instanceof LivingEntity) {
                spawnedMobs.add(entity.getUniqueId());
                mobManager.trackSpawnedMob(entity, mobId);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao spawnar mob: " + e.getMessage());
        }
    }

    /** Verifica se os mobs ainda estão na área e remove os que saíram */
    private void checkMobsLocation() {
        if (area.getRegion() == null) return;

        Iterator<UUID> iterator = spawnedMobs.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = Bukkit.getEntity(uuid);

            if (entity == null || !entity.isValid()) {
                iterator.remove();
                continue;
            }

            Location loc = entity.getLocation();
            if (!isInsideRegion(loc)) {
                mobManager.untrackMob(entity);
                entity.remove();
                iterator.remove();
            }
        }
    }

    /** Pega uma localização aleatória dentro do Cuboid (X/Z) e ajusta Y pelo highestBlockYAt */
    private Location getRandomLocationInsideRegion() {
        if (area.getRegion() == null) return null;

        Location lower = area.getRegion().getLowerNE(); // min XYZ
        Location upper = area.getRegion().getUpperSW(); // max XYZ
        World world = lower.getWorld();

        if (world == null) return null;

        Random rand = new Random();

        int minX = Math.min(lower.getBlockX(), upper.getBlockX());
        int maxX = Math.max(lower.getBlockX(), upper.getBlockX());
        int minZ = Math.min(lower.getBlockZ(), upper.getBlockZ());
        int maxZ = Math.max(lower.getBlockZ(), upper.getBlockZ());

        int x = minX + rand.nextInt(maxX - minX + 1);
        int z = minZ + rand.nextInt(maxZ - minZ + 1);

        // pega Y do topo do terreno +1 para spawnar no ar
        int y = world.getHighestBlockYAt(x, z) + 1;

        return new Location(world, x + 0.5, y, z + 0.5);
    }

    /** Testa se um ponto está dentro do Cuboid */
    private boolean isInsideRegion(Location loc) {
        if (area.getRegion() == null || loc == null) return false;

        // Garante mesmo mundo
        Location lower = area.getRegion().getLowerNE();
        if (loc.getWorld() == null || !loc.getWorld().equals(lower.getWorld())) return false;

        Location upper = area.getRegion().getUpperSW();

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        int minX = Math.min(lower.getBlockX(), upper.getBlockX());
        int maxX = Math.max(lower.getBlockX(), upper.getBlockX());
        int minZ = Math.min(lower.getBlockZ(), upper.getBlockZ());
        int maxZ = Math.max(lower.getBlockZ(), upper.getBlockZ());

        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
