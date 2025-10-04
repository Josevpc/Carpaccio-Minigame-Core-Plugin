package carpaccio.minigameCore.core;

import carpaccio.minigameCore.MinigameCore;
import carpaccio.minigameCore.utils.Cuboid;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;

/**
 * Sistema de spawn de mobs em área delimitada (usando Cuboid)
 * Pode ser instanciado e gerenciado de forma independente
 */
public class MobSpawnSystem {

    private final MinigameCore plugin;

    // Agora usamos Cuboid ao invés de pos1/pos2
    private Cuboid region;
    private Location pos1; // mantidos para compatibilidade com setters antigos
    private Location pos2;

    private final Set<UUID> spawnedMobs;
    private int spawnTaskId;
    private int checkTaskId;

    // Configurações
    private int spawnInterval;
    private int maxMobs;
    private int checkInterval;
    private EntityType[] allowedMobs;
    private boolean isActive;

    // ==========================================
    // CONSTRUTOR
    // ==========================================

    public MobSpawnSystem(MinigameCore plugin) {
        this.plugin = plugin;
        this.spawnedMobs = new HashSet<>();
        this.spawnTaskId = -1;
        this.checkTaskId = -1;
        this.isActive = false;

        // Configurações padrão
        this.spawnInterval = 100; // 5s (20 ticks = 1s)
        this.maxMobs = 20;
        this.checkInterval = 20; // 1s
        this.allowedMobs = new EntityType[]{
                EntityType.COW, EntityType.SHEEP, EntityType.PIG,
                EntityType.CHICKEN, EntityType.RABBIT, EntityType.HORSE
        };
    }

    // ==========================================
    // GETTERS / SETTERS
    // ==========================================

    /** Retorna o Cuboid atual da região */
    public Cuboid getRegion() {
        return region;
    }

    /** Define o Cuboid diretamente */
    public void setRegion(Cuboid region) {
        this.region = region;
    }

    /** Atalho: define a área com duas posições e cria o Cuboid */
    public void setArea(Location a, Location b) {
        if (a == null || b == null) {
            this.region = null;
            this.pos1 = a;
            this.pos2 = b;
            return;
        }
        try {
            this.region = new Cuboid(a, b); // valida mesmo mundo e normaliza limites
            this.pos1 = a.clone();
            this.pos2 = b.clone();
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("As posições da área precisam estar no mesmo mundo: " + ex.getMessage());
            this.region = null;
        }
    }

    /** Compat: primeira posição (armazenada e usada para construir o Cuboid quando possível) */
    public Location getPosition1() { return pos1; }
    public void setPosition1(Location location) {
        this.pos1 = location != null ? location.clone() : null;
        if (this.pos1 != null && this.pos2 != null) setArea(this.pos1, this.pos2);
    }

    /** Compat: segunda posição */
    public Location getPosition2() { return pos2; }
    public void setPosition2(Location location) {
        this.pos2 = location != null ? location.clone() : null;
        if (this.pos1 != null && this.pos2 != null) setArea(this.pos1, this.pos2);
    }

    public int getSpawnInterval() { return spawnInterval; }
    public void setSpawnInterval(int ticks) {
        this.spawnInterval = ticks;
        if (isActive) restart();
    }

    public int getMaxMobs() { return maxMobs; }
    public void setMaxMobs(int max) { this.maxMobs = max; }

    public int getCheckInterval() { return checkInterval; }
    public void setCheckInterval(int ticks) {
        this.checkInterval = ticks;
        if (isActive) restart();
    }

    public EntityType[] getAllowedMobs() { return allowedMobs; }
    public void setAllowedMobs(EntityType... types) { this.allowedMobs = types; }

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
        if (region == null) {
            return false;
        }
        if (isActive) stop();

        spawnTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (spawnedMobs.size() < maxMobs) {
                spawnRandomMob();
            }
        }, 0L, spawnInterval);

        checkTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                this::checkMobsLocation, 0L, checkInterval);

        isActive = true;
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
        if (region == null || allowedMobs == null || allowedMobs.length == 0) return;

        Location spawnLoc = getRandomLocationInsideRegion();
        if (spawnLoc == null) return;

        EntityType type = allowedMobs[new Random().nextInt(allowedMobs.length)];
        try {
            Entity entity = spawnLoc.getWorld().spawnEntity(spawnLoc, type);
            if (entity instanceof LivingEntity) {
                spawnedMobs.add(entity.getUniqueId());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao spawnar mob: " + e.getMessage());
        }
    }

    /** Verifica se os mobs ainda estão na área e remove os que saíram */
    private void checkMobsLocation() {
        if (region == null) return;

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
                entity.remove();
                iterator.remove();
            }
        }
    }

    /** Pega uma localização aleatória dentro do Cuboid (X/Z) e ajusta Y pelo highestBlockYAt */
    private Location getRandomLocationInsideRegion() {
        if (region == null) return null;

        Location lower = region.getLowerNE(); // min XYZ
        Location upper = region.getUpperSW(); // max XYZ
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
        if (region == null || loc == null) return false;

        // Garante mesmo mundo
        Location lower = region.getLowerNE();
        if (loc.getWorld() == null || !loc.getWorld().equals(lower.getWorld())) return false;

        Location upper = region.getUpperSW();

        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        int minX = Math.min(lower.getBlockX(), upper.getBlockX());
        int maxX = Math.max(lower.getBlockX(), upper.getBlockX());
        int minZ = Math.min(lower.getBlockZ(), upper.getBlockZ());
        int maxZ = Math.max(lower.getBlockZ(), upper.getBlockZ());

        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
