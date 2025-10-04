package carpaccio.minigameCore.core;

import carpaccio.minigameCore.MinigameCore;
import carpaccio.minigameCore.core.MobSpawnSystem;
import carpaccio.minigameCore.core.SpawnArea;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gerenciador centralizado de sistemas de spawn de mobs
 * Gerencia múltiplas áreas de spawn com diferentes configurações
 */
public class MobSpawnManager {

    private final MinigameCore plugin;
    private final Map<String, MobSpawnSystem> spawnSystems;
    private final Map<String, SpawnArea> spawnAreas;
    private final File configFile;
    private FileConfiguration config;

    // ==========================================
    // CONSTRUTOR
    // ==========================================

    /**
     * Construtor do gerenciador
     *
     * @param plugin Instância do plugin principal
     */
    public MobSpawnManager(MinigameCore plugin) {
        this.plugin = plugin;
        this.spawnSystems = new HashMap<>();
        this.spawnAreas = new HashMap<>();
        this.configFile = new File(plugin.getDataFolder(), "spawn_areas.yml");

        loadConfig();
    }

    // ==========================================
    // GETTERS
    // ==========================================

    /**
     * Obtém um sistema de spawn pelo nome
     */
    public MobSpawnSystem getSystem(String name) {
        return spawnSystems.get(name);
    }

    /**
     * Obtém uma área de spawn pelo nome
     */
    public SpawnArea getArea(String name) {
        return spawnAreas.get(name);
    }

    /**
     * Obtém todos os nomes de áreas registradas
     */
    public Set<String> getAreaNames() {
        return new HashSet<>(spawnAreas.keySet());
    }

    /**
     * Obtém todas as áreas de spawn
     */
    public Collection<SpawnArea> getAllAreas() {
        return new ArrayList<>(spawnAreas.values());
    }

    /**
     * Obtém o total de mobs em todas as áreas
     */
    public int getTotalMobs() {
        return spawnSystems.values().stream()
                .mapToInt(MobSpawnSystem::getMobCount)
                .sum();
    }

    /**
     * Obtém o total de áreas ativas
     */
    public long getActiveAreasCount() {
        return spawnSystems.values().stream()
                .filter(MobSpawnSystem::isActive)
                .count();
    }

    /**
     * Verifica se existe uma área com o nome especificado
     */
    public boolean hasArea(String name) {
        return spawnAreas.containsKey(name);
    }

    // ==========================================
    // FUNÇÕES DE GERENCIAMENTO DE ÁREAS
    // ==========================================

    /**
     * Cria uma nova área de spawn
     *
     * @param name          Nome da área
     * @param pos1          Primeira posição
     * @param pos2          Segunda posição
     * @param mobs          Tipos de mobs permitidos
     * @param maxMobs       Máximo de mobs
     * @param spawnInterval Intervalo de spawn em ticks
     * @return true se criado com sucesso
     */
    public boolean createArea(String name, Location pos1, Location pos2,
                              EntityType[] mobs, int maxMobs, int spawnInterval) {

        if (spawnAreas.containsKey(name)) {
            return false;
        }

        // Cria a área
        SpawnArea area = new SpawnArea(name, pos1, pos2, mobs, maxMobs, spawnInterval);
        spawnAreas.put(name, area);

        // Cria o sistema
        MobSpawnSystem system = new MobSpawnSystem(plugin);
        system.setPosition1(pos1);
        system.setPosition2(pos2);
        system.setAllowedMobs(mobs);
        system.setMaxMobs(maxMobs);
        system.setSpawnInterval(spawnInterval);

        spawnSystems.put(name, system);

        saveConfig();
        return true;
    }

    /**
     * Remove uma área de spawn
     *
     * @param name Nome da área
     * @return true se removido com sucesso
     */
    public boolean removeArea(String name) {
        MobSpawnSystem system = spawnSystems.remove(name);
        if (system != null) {
            system.shutdown();
        }

        SpawnArea area = spawnAreas.remove(name);
        if (area != null) {
            saveConfig();
            return true;
        }

        return false;
    }

    /**
     * Inicia uma área de spawn
     */
    public boolean startArea(String name) {
        MobSpawnSystem system = spawnSystems.get(name);
        if (system != null) {
            return system.start();
        }
        return false;
    }

    /**
     * Para uma área de spawn
     */
    public boolean stopArea(String name) {
        MobSpawnSystem system = spawnSystems.get(name);
        if (system != null) {
            system.stop();
            return true;
        }
        return false;
    }

    /**
     * Limpa todos os mobs de uma área
     */
    public int clearArea(String name) {
        MobSpawnSystem system = spawnSystems.get(name);
        if (system != null) {
            return system.clearAllMobs();
        }
        return 0;
    }

    /**
     * Ativa/desativa uma área
     */
    public boolean toggleArea(String name) {
        MobSpawnSystem system = spawnSystems.get(name);
        if (system != null) {
            if (system.isActive()) {
                system.stop();
            } else {
                system.start();
            }
            return true;
        }
        return false;
    }

    /**
     * Atualiza as configurações de uma área
     */
    public boolean updateArea(String name, int maxMobs, int spawnInterval) {
        MobSpawnSystem system = spawnSystems.get(name);
        SpawnArea area = spawnAreas.get(name);

        if (system != null && area != null) {
            system.setMaxMobs(maxMobs);
            system.setSpawnInterval(spawnInterval);

            area.setMaxMobs(maxMobs);
            area.setSpawnInterval(spawnInterval);

            saveConfig();
            return true;
        }
        return false;
    }

    /**
     * Atualiza os mobs permitidos de uma área
     */
    public boolean updateAreaMobs(String name, EntityType... mobs) {
        MobSpawnSystem system = spawnSystems.get(name);
        SpawnArea area = spawnAreas.get(name);

        if (system != null && area != null) {
            system.setAllowedMobs(mobs);
            area.setAllowedMobs(mobs);

            saveConfig();
            return true;
        }
        return false;
    }

    // ==========================================
    // FUNÇÕES DE GERENCIAMENTO GLOBAL
    // ==========================================

    /**
     * Inicia todas as áreas
     */
    public void startAll() {
        spawnSystems.values().forEach(MobSpawnSystem::start);
        plugin.getLogger().info("Todas as áreas de spawn foram iniciadas!");
    }

    /**
     * Para todas as áreas
     */
    public void stopAll() {
        spawnSystems.values().forEach(MobSpawnSystem::stop);
        plugin.getLogger().info("Todas as áreas de spawn foram paradas!");
    }

    /**
     * Limpa todos os mobs de todas as áreas
     */
    public int clearAll() {
        int total = 0;
        for (MobSpawnSystem system : spawnSystems.values()) {
            total += system.clearAllMobs();
        }
        return total;
    }

    /**
     * Desliga e limpa todas as áreas
     */
    public void shutdownAll() {
        spawnSystems.values().forEach(MobSpawnSystem::shutdown);
        spawnSystems.clear();
        plugin.getLogger().info("Todos os sistemas de spawn foram desligados!");
    }

    /**
     * Recarrega todas as áreas da configuração
     */
    public void reloadAreas() {
        shutdownAll();
        spawnAreas.clear();
        loadConfig();
        plugin.getLogger().info("Áreas de spawn recarregadas!");
    }

    // ==========================================
    // FUNÇÕES DE CONSULTA
    // ==========================================

    /**
     * Obtém informações detalhadas de uma área
     */
    public String getAreaInfo(String name) {
        MobSpawnSystem system = spawnSystems.get(name);
        SpawnArea area = spawnAreas.get(name);

        if (system == null || area == null) {
            return "Área não encontrada!";
        }

        StringBuilder info = new StringBuilder();
        info.append("§e=== Área: ").append(name).append(" ===\n");
        info.append("§6Status: ").append(system.isActive() ? "§aAtiva" : "§cInativa").append("\n");
        info.append("§6Mobs: §f").append(system.getMobCount()).append("/").append(system.getMaxMobs()).append("\n");
        info.append("§6Intervalo: §f").append(system.getSpawnInterval()).append(" ticks\n");
        info.append("§6Tipos: §f").append(area.getMobTypesString());

        return info.toString();
    }

    /**
     * Lista todas as áreas
     */
    public List<String> listAreas() {
        List<String> list = new ArrayList<>();

        for (String name : spawnAreas.keySet()) {
            MobSpawnSystem system = spawnSystems.get(name);
            String status = system != null && system.isActive() ? "§aAtiva" : "§cInativa";
            int count = system != null ? system.getMobCount() : 0;
            int max = system != null ? system.getMaxMobs() : 0;

            list.add(String.format("§6%s §f- %s §f(%d/%d mobs)", name, status, count, max));
        }

        return list;
    }

    /**
     * Obtém áreas ativas
     */
    public List<String> getActiveAreas() {
        return spawnSystems.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Obtém áreas inativas
     */
    public List<String> getInactiveAreas() {
        return spawnSystems.entrySet().stream()
                .filter(entry -> !entry.getValue().isActive())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // ==========================================
    // FUNÇÕES DE CONFIGURAÇÃO
    // ==========================================

    /**
     * Carrega as áreas do arquivo de configuração
     */
    private void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("spawn_areas.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        ConfigurationSection areasSection = config.getConfigurationSection("areas");
        if (areasSection == null) {
            return;
        }

        for (String areaName : areasSection.getKeys(false)) {
            try {
                ConfigurationSection areaConfig = areasSection.getConfigurationSection(areaName);

                // Carrega as posições
                Location pos1 = loadLocation(areaConfig.getConfigurationSection("pos1"));
                Location pos2 = loadLocation(areaConfig.getConfigurationSection("pos2"));

                // Carrega os mobs
                List<String> mobsList = areaConfig.getStringList("mobs");
                EntityType[] mobs = mobsList.stream()
                        .map(EntityType::valueOf)
                        .toArray(EntityType[]::new);

                // Carrega configurações
                int maxMobs = areaConfig.getInt("max-mobs", 20);
                int spawnInterval = areaConfig.getInt("spawn-interval", 100);
                boolean autoStart = areaConfig.getBoolean("auto-start", false);
                int checkInterval = areaConfig.getInt("check-interval", 20);

                SpawnArea area = new SpawnArea(areaName, pos1, pos2, mobs, maxMobs, spawnInterval);
                spawnAreas.put(areaName, area);

                // Cria e configura o sistema
                MobSpawnSystem system = new MobSpawnSystem(plugin);
                system.setPosition1(area.getPos1());
                system.setPosition2(area.getPos2());
                system.setAllowedMobs(area.getAllowedMobs());
                system.setMaxMobs(area.getMaxMobs());
                system.setSpawnInterval(area.getSpawnInterval());
                system.setCheckInterval(area.getCheckInterval());

                spawnSystems.put(areaName, system);

                // Auto-inicia se configurado
                if (autoStart) {
                    startArea(areaName);
                }

            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar área '" + areaName + "': " + e.getMessage());
            }
        }
    }

    /**
     * Salva as áreas no arquivo de configuração
     */
    private void saveConfig() {
        config = new YamlConfiguration();

        for (Map.Entry<String, SpawnArea> entry : spawnAreas.entrySet()) {
            String name = entry.getKey();
            SpawnArea area = entry.getValue();
            MobSpawnSystem system = spawnSystems.get(name);

            String path = "areas." + name;

            // Salva posições
            saveLocation(config, path + ".pos1", area.getPos1());
            saveLocation(config, path + ".pos2", area.getPos2());

            // Salva mobs
            List<String> mobsList = Arrays.stream(area.getAllowedMobs())
                    .map(Enum::name)
                    .collect(Collectors.toList());
            config.set(path + ".mobs", mobsList);

            // Salva configurações
            config.set(path + ".max-mobs", area.getMaxMobs());
            config.set(path + ".spawn-interval", area.getSpawnInterval());
            config.set(path + ".check-interval", area.getCheckInterval());
            config.set(path + ".auto-start", system != null && system.isActive());
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar configuração: " + e.getMessage());
        }
    }

    /**
     * Carrega uma localização da configuração
     */
    private Location loadLocation(ConfigurationSection section) {
        String worldName = section.getString("world");
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");

        return new Location(plugin.getServer().getWorld(worldName), x, y, z);
    }

    /**
     * Salva uma localização na configuração
     */
    private void saveLocation(FileConfiguration config, String path, Location loc) {
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
    }
}