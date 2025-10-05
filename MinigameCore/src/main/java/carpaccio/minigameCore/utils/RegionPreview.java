package carpaccio.minigameCore.utils;

import carpaccio.minigameCore.MinigameCore;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegionPreview {

    private final MinigameCore plugin;
    private final double particleSpacing;
    private final Map<UUID, BukkitTask> activePreviews;

    public RegionPreview(MinigameCore plugin) {
        this(plugin, 0.5);
    }

    public RegionPreview(MinigameCore plugin, double particleSpacing) {
        this.plugin = plugin;
        this.particleSpacing = particleSpacing;
        this.activePreviews = new HashMap<>();
    }

    /**
     * Para o preview ativo de um jogador
     * @param p Jogador
     * @return true se havia um preview ativo, false caso contrário
     */
    public boolean stopPreview(Player p) {
        if (p == null) return false;

        BukkitTask task = activePreviews.remove(p.getUniqueId());
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }

    /**
     * Para todos os previews ativos
     */
    public void stopAllPreviews() {
        for (BukkitTask task : activePreviews.values()) {
            task.cancel();
        }
        activePreviews.clear();
    }

    /**
     * Verifica se um jogador tem preview ativo
     * @param p Jogador
     * @return true se tem preview ativo
     */
    public boolean hasActivePreview(Player p) {
        return p != null && activePreviews.containsKey(p.getUniqueId());
    }

    /**
     * Mostra um preview visual das bordas de uma região usando partículas
     * @param p Jogador que verá o preview
     * @param pos1 Primeira posição da região
     * @param pos2 Segunda posição da região
     */
    public void showPreview(Player p, Location pos1, Location pos2) {
        showPreview(p, pos1, pos2, 200, Particle.BLOCK, Material.REDSTONE_BLOCK);
    }

    /**
     * Mostra um preview visual das bordas de uma região usando partículas
     * @param p Jogador que verá o preview
     * @param cuboid Cuboid representando a região
     */
    public void showPreview(Player p, Cuboid cuboid) {
        showPreview(p, cuboid.getLowerNE(), cuboid.getUpperSW());
    }

    /**
     * Mostra um preview visual personalizado das bordas de uma região
     * @param p Jogador que verá o preview
     * @param pos1 Primeira posição
     * @param pos2 Segunda posição
     * @param durationTicks Duração em ticks (20 ticks = 1 segundo, -1 para infinito)
     * @param particle Tipo de partícula
     * @param material Material para a partícula BLOCK
     */
    public void showPreview(Player p, Location pos1, Location pos2, int durationTicks, Particle particle, Material material) {
        if (pos1 == null || pos2 == null || !pos1.getWorld().equals(pos2.getWorld())) {
            return;
        }

        // Para preview anterior se existir
        stopPreview(p);

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            final World world = pos1.getWorld();
            // Adiciona 0.5 para centralizar no bloco, e ajusta limites para cobrir toda área
            final double minX = Math.floor(Math.min(pos1.getX(), pos2.getX()));
            final double maxX = Math.floor(Math.max(pos1.getX(), pos2.getX())) + 1.0;
            final double minY = Math.floor(Math.min(pos1.getY(), pos2.getY()));
            final double maxY = Math.floor(Math.max(pos1.getY(), pos2.getY())) + 1.0;
            final double minZ = Math.floor(Math.min(pos1.getZ(), pos2.getZ()));
            final double maxZ = Math.floor(Math.max(pos1.getZ(), pos2.getZ())) + 1.0;
            final BlockData blockData = material.createBlockData();

            @Override
            public void run() {
                if ((durationTicks > 0 && ticks >= durationTicks) || p == null || !p.isOnline()) {
                    activePreviews.remove(p.getUniqueId());
                    cancel();
                    return;
                }

                drawEdges();
                ticks++;
            }

            private void drawEdges() {
                // Bordas no eixo X (paralelas ao eixo X)
                drawLine(minX, minY, minZ, maxX, minY, minZ);
                drawLine(minX, minY, maxZ, maxX, minY, maxZ);
                drawLine(minX, maxY, minZ, maxX, maxY, minZ);
                drawLine(minX, maxY, maxZ, maxX, maxY, maxZ);

                // Bordas no eixo Y (paralelas ao eixo Y)
                drawLine(minX, minY, minZ, minX, maxY, minZ);
                drawLine(maxX, minY, minZ, maxX, maxY, minZ);
                drawLine(minX, minY, maxZ, minX, maxY, maxZ);
                drawLine(maxX, minY, maxZ, maxX, maxY, maxZ);

                // Bordas no eixo Z (paralelas ao eixo Z)
                drawLine(minX, minY, minZ, minX, minY, maxZ);
                drawLine(maxX, minY, minZ, maxX, minY, maxZ);
                drawLine(minX, maxY, minZ, minX, maxY, maxZ);
                drawLine(maxX, maxY, minZ, maxX, maxY, maxZ);
            }

            private void drawLine(double x1, double y1, double z1, double x2, double y2, double z2) {
                double distance = Math.sqrt(
                        Math.pow(x2 - x1, 2) +
                                Math.pow(y2 - y1, 2) +
                                Math.pow(z2 - z1, 2)
                );

                if (distance < 0.1) {
                    spawnParticle(x1, y1, z1);
                    return;
                }

                double dx = (x2 - x1) / distance;
                double dy = (y2 - y1) / distance;
                double dz = (z2 - z1) / distance;

                for (double i = 0; i <= distance; i += particleSpacing) {
                    double x = x1 + (dx * i);
                    double y = y1 + (dy * i);
                    double z = z1 + (dz * i);
                    spawnParticle(x, y, z);
                }
            }

            private void spawnParticle(double x, double y, double z) {
                if (particle == Particle.BLOCK) {
                    world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0, blockData);
                } else {
                    world.spawnParticle(particle, x, y, z, 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);

        activePreviews.put(p.getUniqueId(), task);
    }

    /**
     * Mostra preview permanente (até ser cancelado manualmente)
     */
    public void showPermanentPreview(Player p, Location pos1, Location pos2) {
        showPreview(p, pos1, pos2, -1, Particle.BLOCK, Material.REDSTONE_BLOCK);
    }

    /**
     * Mostra preview com partículas coloridas (REDSTONE)
     */
    public void showColoredPreview(Player p, Location pos1, Location pos2, Color color) {
        showColoredPreview(p, pos1, pos2, 200, color);
    }

    /**
     * Mostra preview com partículas coloridas personalizadas
     */
    public void showColoredPreview(Player p, Location pos1, Location pos2, int durationTicks, Color color) {
        if (pos1 == null || pos2 == null || !pos1.getWorld().equals(pos2.getWorld())) {
            return;
        }

        // Para preview anterior se existir
        stopPreview(p);

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            final World world = pos1.getWorld();
            // Adiciona 0.5 para centralizar no bloco, e ajusta limites para cobrir toda área
            final double minX = Math.floor(Math.min(pos1.getX(), pos2.getX()));
            final double maxX = Math.floor(Math.max(pos1.getX(), pos2.getX())) + 1.0;
            final double minY = Math.floor(Math.min(pos1.getY(), pos2.getY()));
            final double maxY = Math.floor(Math.max(pos1.getY(), pos2.getY())) + 1.0;
            final double minZ = Math.floor(Math.min(pos1.getZ(), pos2.getZ()));
            final double maxZ = Math.floor(Math.max(pos1.getZ(), pos2.getZ())) + 1.0;
            final Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);

            @Override
            public void run() {
                if ((durationTicks > 0 && ticks >= durationTicks) || p == null || !p.isOnline()) {
                    activePreviews.remove(p.getUniqueId());
                    cancel();
                    return;
                }

                drawEdges();
                ticks++;
            }

            private void drawEdges() {
                drawLine(minX, minY, minZ, maxX, minY, minZ);
                drawLine(minX, minY, maxZ, maxX, minY, maxZ);
                drawLine(minX, maxY, minZ, maxX, maxY, minZ);
                drawLine(minX, maxY, maxZ, maxX, maxY, maxZ);

                drawLine(minX, minY, minZ, minX, maxY, minZ);
                drawLine(maxX, minY, minZ, maxX, maxY, minZ);
                drawLine(minX, minY, maxZ, minX, maxY, maxZ);
                drawLine(maxX, minY, maxZ, maxX, maxY, maxZ);

                drawLine(minX, minY, minZ, minX, minY, maxZ);
                drawLine(maxX, minY, minZ, maxX, minY, maxZ);
                drawLine(minX, maxY, minZ, minX, maxY, maxZ);
                drawLine(maxX, maxY, minZ, maxX, maxY, maxZ);
            }

            private void drawLine(double x1, double y1, double z1, double x2, double y2, double z2) {
                double distance = Math.sqrt(
                        Math.pow(x2 - x1, 2) +
                                Math.pow(y2 - y1, 2) +
                                Math.pow(z2 - z1, 2)
                );

                if (distance < 0.1) {
                    world.spawnParticle(Particle.DUST, x1, y1, z1, 1, 0, 0, 0, 0, dustOptions);
                    return;
                }

                double dx = (x2 - x1) / distance;
                double dy = (y2 - y1) / distance;
                double dz = (z2 - z1) / distance;

                for (double i = 0; i <= distance; i += particleSpacing) {
                    double x = x1 + (dx * i);
                    double y = y1 + (dy * i);
                    double z = z1 + (dz * i);
                    world.spawnParticle(Particle.DUST, x, y, z, 1, 0, 0, 0, 0, dustOptions);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);

        activePreviews.put(p.getUniqueId(), task);
    }

    /**
     * Mostra preview colorido permanente
     */
    public void showPermanentColoredPreview(Player p, Location pos1, Location pos2, Color color) {
        showColoredPreview(p, pos1, pos2, -1, color);
    }
}