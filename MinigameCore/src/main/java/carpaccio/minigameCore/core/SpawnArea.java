package carpaccio.minigameCore.core;

import carpaccio.minigameCore.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Representa uma área de spawn com suas configurações
 * Armazena dados de configuração, não executa o spawn
 */
public class SpawnArea {

    // ==========================================
    // ATRIBUTOS
    // ==========================================

    private final String name;
    private Cuboid region;
    private Location pos1;
    private Location pos2;
    private EntityType[] allowedMobs;
    private int maxMobs;
    private int spawnInterval;
    private int checkInterval;
    private boolean autoStart;

    // ==========================================
    // CONSTRUTORES
    // ==========================================

    /**
     * Construtor completo
     */
    public SpawnArea(String name, Location pos1, Location pos2,
                     EntityType[] allowedMobs, int maxMobs, int spawnInterval, int checkInterval) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.allowedMobs = allowedMobs;
        this.maxMobs = maxMobs;
        this.spawnInterval = spawnInterval;
        this.checkInterval = 20; // Padrão: 1 segundo
        this.autoStart = false;

        setArea(this.pos1, this.pos2);
    }

    /**
     * Construtor com valores padrão
     */
    public SpawnArea(String name, Location pos1, Location pos2) {
        this(name, pos1, pos2,
                new EntityType[]{EntityType.COW, EntityType.SHEEP, EntityType.PIG},
                20, 100, 20);
    }

    /**
     * Construtor com auto-start
     */
    public SpawnArea(String name, Location pos1, Location pos2,
                     EntityType[] allowedMobs, int maxMobs,
                     int spawnInterval, int checkInterval, boolean autoStart) {
        this(name, pos1, pos2, allowedMobs, maxMobs, spawnInterval, checkInterval);
        this.autoStart = autoStart;
    }

    // ==========================================
    // GETTERS
    // ==========================================

    /**
     * Obtém o nome da área
     */
    public String getName() { return name; }

    /** Retorna o Cuboid atual da região */
    public Cuboid getRegion() { return region; }

    /**
     * Obtém a primeira posição
     */
    public Location getPos1() { return pos1; }

    /**
     * Obtém a segunda posição
     */
    public Location getPos2() { return pos2; }

    /**
     * Obtém os tipos de mobs permitidos
     */
    public EntityType[] getAllowedMobs() { return allowedMobs; }

    /**
     * Obtém o número máximo de mobs
     */
    public int getMaxMobs() { return maxMobs; }

    /**
     * Obtém o intervalo de spawn em ticks
     */
    public int getSpawnInterval() { return spawnInterval; }

    /**
     * Obtém o intervalo de verificação em ticks
     */
    public int getCheckInterval() { return checkInterval; }

    /**
     * Verifica se deve iniciar automaticamente
     */
    public boolean isAutoStart() { return autoStart; }

    /**
     * Obtém o nome do mundo da área
     */
    public String getWorldName() { return pos1 != null ? pos1.getWorld().getName() : null; }

    // ==========================================
    // SETTERS
    // ==========================================


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
            //plugin.getLogger().warning("As posições da área precisam estar no mesmo mundo: " + ex.getMessage());
            this.region = null;
        }
    }

    /**
     * Define a primeira posição
     */

    public void setPos1(Location location) {
        this.pos1 = location != null ? location.clone() : null;
        if (this.pos1 != null && this.pos2 != null) setArea(this.pos1, this.pos2);
    }

    /**
     * Define a segunda posição
     */
    public void setPos2(Location location) {
        this.pos2 = location != null ? location.clone() : null;
        if (this.pos1 != null && this.pos2 != null) setArea(this.pos1, this.pos2);
    }

    /**
     * Define os tipos de mobs permitidos
     */
    public void setAllowedMobs(EntityType... types) {
        this.allowedMobs = types;
    }

    /**
     * Define o número máximo de mobs
     */
    public void setMaxMobs(int max) {
        this.maxMobs = max;
    }

    /**
     * Define o intervalo de spawn
     */
    public void setSpawnInterval(int ticks) {
        this.spawnInterval = ticks;
    }

    /**
     * Define o intervalo de verificação
     */
    public void setCheckInterval(int ticks) {
        this.checkInterval = ticks;
    }

    /**
     * Define se deve iniciar automaticamente
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    // ==========================================
    // MÉTODOS UTILITÁRIOS
    // ==========================================

    /**
     * Obtém os tipos de mobs como string formatada
     */
    public String getMobTypesString() {
        return Arrays.stream(allowedMobs)
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Obtém os tipos de mobs como string formatada (versão bonita)
     */
    public String getMobTypesFormatted() {
        return Arrays.stream(allowedMobs)
                .map(type -> formatMobName(type.name()))
                .collect(Collectors.joining(", "));
    }

    /**
     * Verifica se um tipo de mob é permitido
     */
    public boolean isMobAllowed(EntityType type) {
        return Arrays.asList(allowedMobs).contains(type);
    }

    /**
     * Adiciona um tipo de mob à lista de permitidos
     */
    public void addMobType(EntityType type) {
        if (!isMobAllowed(type)) {
            EntityType[] newArray = Arrays.copyOf(allowedMobs, allowedMobs.length + 1);
            newArray[allowedMobs.length] = type;
            allowedMobs = newArray;
        }
    }

    /**
     * Remove um tipo de mob da lista de permitidos
     */
    public void removeMobType(EntityType type) {
        allowedMobs = Arrays.stream(allowedMobs)
                .filter(t -> t != type)
                .toArray(EntityType[]::new);
    }

    /**
     * Valida se a área está configurada corretamente
     */
    public boolean isValid() {
        if (name == null || name.isEmpty()) return false;
        if (pos1 == null || pos2 == null) return false;
        if (!pos1.getWorld().equals(pos2.getWorld())) return false;
        if (allowedMobs == null || allowedMobs.length == 0) return false;
        if (maxMobs <= 0) return false;
        if (spawnInterval <= 0) return false;

        return true;
    }

    /**
     * Cria uma cópia da área
     */
    public SpawnArea clone() {
        SpawnArea copy = new SpawnArea(
                name,
                pos1.clone(),
                pos2.clone(),
                allowedMobs.clone(),
                maxMobs,
                spawnInterval,
                checkInterval,
                autoStart
        );
        copy.setCheckInterval(checkInterval);
        return copy;
    }

    // ==========================================
    // MÉTODOS DE FORMATAÇÃO
    // ==========================================

    /**
     * Formata o nome de um mob para exibição
     */
    private String formatMobName(String mobName) {
        return Arrays.stream(mobName.split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    /**
     * Retorna uma string com informações básicas da área
     */
    public String getInfo() {
        StringBuilder info = new StringBuilder();
        info.append("§e=== ").append(name).append(" ===\n");
        info.append("§6Mundo: §f").append(getWorldName()).append("\n");
        info.append("§6Posição 1: §f").append(formatLocation(pos1)).append("\n");
        info.append("§6Posição 2: §f").append(formatLocation(pos2)).append("\n");
        info.append("§6Volume: §f").append(region.getVolume()).append(" blocos\n");
        info.append("§6Mobs: §f").append(getMobTypesFormatted()).append("\n");
        info.append("§6Max Mobs: §f").append(maxMobs).append("\n");
        info.append("§6Intervalo: §f").append(spawnInterval).append(" ticks\n");
        info.append("§6Auto-Start: §f").append(autoStart ? "§aSim" : "§cNão");

        return info.toString();
    }

    /**
     * Retorna uma string resumida da área
     */
    public String getSummary() {
        return String.format("§6%s §f[%s] - %d mobs, %dt interval",
                name, getWorldName(), maxMobs, spawnInterval);
    }

    /**
     * Formata uma localização
     */
    private String formatLocation(Location loc) {
        return String.format("X:%d Y:%d Z:%d",
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    // ==========================================
    // SOBRESCRITA DE MÉTODOS
    // ==========================================

    @Override
    public String toString() {
        return "SpawnArea{name='" + name + "', mobs=" + allowedMobs.length +
                ", max=" + maxMobs + ", interval=" + spawnInterval + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SpawnArea other = (SpawnArea) obj;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}