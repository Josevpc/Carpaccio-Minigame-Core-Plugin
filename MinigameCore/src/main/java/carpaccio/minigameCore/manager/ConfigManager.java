package carpaccio.minigameCore.manager;

import carpaccio.minigameCore.MinigameCore;
import carpaccio.minigameCore.core.loot.CustomLoot;
import carpaccio.minigameCore.core.loot.LootTable;
import carpaccio.minigameCore.core.mobs.CustomMob;
import carpaccio.minigameCore.core.mobs.MobManager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigManager {

    public static void loadLootTables(MinigameCore plugin, FileConfiguration config, MobManager mobManager) {
        plugin.getLogger().info("Iniciando carregamento de LootTables...");
        ConfigurationSection lootSection = config.getConfigurationSection("loot_tables");
        if (lootSection == null) {
            plugin.getLogger().warning("Seção 'loot_tables' não encontrada no arquivo de configuração.");
            return;
        }

        for (String lootId : lootSection.getKeys(false)) {
            plugin.getLogger().info("Carregando LootTable: " + lootId);
            ConfigurationSection tableSection = lootSection.getConfigurationSection(lootId);
            if (tableSection == null) {
                plugin.getLogger().warning("Seção inválida para LootTable: " + lootId);
                continue;
            }

            LootTable lootTable = new LootTable(lootId);

            List<?> rawItems = tableSection.getList("items");
            if (rawItems == null || rawItems.isEmpty()) {
                plugin.getLogger().warning("Nenhum item encontrado para LootTable: " + lootId);
                continue;
            }

            List<Map<String, Object>> items = new ArrayList<>();
            for (Object obj : rawItems) {
                if (obj instanceof Map) {
                    //noinspection unchecked
                    items.add((Map<String, Object>) obj);
                } else {
                    plugin.getLogger().warning("Item inválido na LootTable " + lootId + ": " + obj);
                }
            }

            if (items.isEmpty()) {
                plugin.getLogger().warning("Nenhum item válido encontrado para LootTable: " + lootId);
                continue;
            }

            for (Map<String, Object> itemMap : items) {
                plugin.getLogger().info("Processando item: " + itemMap);
                CustomLoot loot = loadCustomLoot(plugin, itemMap);
                if (loot != null) {
                    lootTable.addLoot(loot);
                    plugin.getLogger().info("Item adicionado à LootTable " + lootId + ": " + loot.toString());
                } else {
                    plugin.getLogger().warning("Falha ao carregar CustomLoot para item: " + itemMap);
                }
            }

            plugin.getLogger().info("LootTable " + lootId + " carregada com " + lootTable.getLoots().size() + " itens.");
            mobManager.registerLootTable(lootTable);
        }

        plugin.getLogger().info("Carregamento de LootTables concluído.");
    }

    private static CustomLoot loadCustomLoot(MinigameCore plugin, Map<String, Object> itemMap) {
        try {
            String materialName = (String) itemMap.get("material");
            Material material = Material.getMaterial(materialName);
            if (material == null) {
                plugin.getLogger().warning("Material inválido: " + materialName);
                return null;
            }

            int minAmount = (int) itemMap.getOrDefault("min_amount", 1);
            int maxAmount = (int) itemMap.getOrDefault("max_amount", minAmount);
            double dropChance = (double) itemMap.getOrDefault("drop_chance", 1.0);
            String displayName = (String) itemMap.get("display_name");
            List<String> lore = (List<String>) itemMap.get("lore");
            List<String> enchantments = (List<String>) itemMap.get("enchantments");
            List<CustomLoot.EnchantmentData> enchantmentData = new ArrayList<>();

            if (enchantments != null) {
                for (String enchant : enchantments) {
                    String[] parts = enchant.split(":");
                    if (parts.length == 2) {
                        enchantmentData.add(new CustomLoot.EnchantmentData(parts[0], Integer.parseInt(parts[1])));
                    }
                }
            }

            CustomLoot loot;

            if (displayName != null || lore != null || enchantments != null ){
                loot = new CustomLoot(material, minAmount, maxAmount, dropChance, displayName, lore, enchantmentData);
            } else {
                loot = new CustomLoot(material, minAmount, maxAmount, dropChance);
            }
            return loot;
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao carregar CustomLoot: " + e.getMessage());
            return null;
        }
    }

    public static void loadCustomMobs(FileConfiguration config, MobManager mobManager) {
        ConfigurationSection mobsSection = config.getConfigurationSection("custom_mobs");
        if (mobsSection == null) return;

        for (String mobId : mobsSection.getKeys(false)) {
            ConfigurationSection mobSection = mobsSection.getConfigurationSection(mobId);
            if (mobSection == null) continue;

            try {
                EntityType entityType = EntityType.valueOf(mobSection.getString("entity_type"));

                CustomMob.Builder builder = new CustomMob.Builder(mobId, entityType);

                String displayName = mobSection.getString("display_name");
                if (displayName != null) {
                    builder.displayName(ChatColor.translateAlternateColorCodes('&', displayName));
                }

                if (mobSection.contains("health")) {
                    builder.health(mobSection.getDouble("health"));
                }

                if (mobSection.contains("damage")) {
                    builder.damage(mobSection.getDouble("damage"));
                }

                if (mobSection.contains("speed")) {
                    builder.speed(mobSection.getDouble("speed"));
                }

                if (mobSection.contains("loot_table")) {
                    builder.lootTable(mobSection.getString("loot_table"));
                }

                // Equipamentos
                ConfigurationSection equipSection = mobSection.getConfigurationSection("equipment");
                if (equipSection != null) {
                    if (equipSection.contains("helmet")) {
                        builder.helmet(loadItemStack(equipSection.getConfigurationSection("helmet")));
                    }
                    if (equipSection.contains("chestplate")) {
                        builder.chestplate(loadItemStack(equipSection.getConfigurationSection("chestplate")));
                    }
                    if (equipSection.contains("leggings")) {
                        builder.leggings(loadItemStack(equipSection.getConfigurationSection("leggings")));
                    }
                    if (equipSection.contains("boots")) {
                        builder.boots(loadItemStack(equipSection.getConfigurationSection("boots")));
                    }
                    if (equipSection.contains("main_hand")) {
                        builder.mainHand(loadItemStack(equipSection.getConfigurationSection("main_hand")));
                    }
                    if (equipSection.contains("off_hand")) {
                        builder.offHand(loadItemStack(equipSection.getConfigurationSection("off_hand")));
                    }
                }

                // Efeitos de poção
                List<?> effects = mobSection.getList("potion_effects");
                if (effects != null) {
                    for (Object effectObj : effects) {
                        if (effectObj instanceof ConfigurationSection) {
                            ConfigurationSection effectSection = (ConfigurationSection) effectObj;
                            PotionEffectType type = PotionEffectType.getByName(effectSection.getString("type"));
                            if (type != null) {
                                int duration = effectSection.getInt("duration", 600);
                                int amplifier = effectSection.getInt("amplifier", 0);
                                builder.addPotionEffect(new PotionEffect(type, duration, amplifier));
                            }
                        }
                    }
                }

                CustomMob customMob = builder.build();
                mobManager.registerCustomMob(customMob);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static ItemStack loadItemStack(ConfigurationSection section) {
        if (section == null) return null;

        try {
            Material material = Material.valueOf(section.getString("material"));
            ItemStack item = new ItemStack(material);

            List<String> enchantments = section.getStringList("enchantments");
            for (String enchantStr : enchantments) {
                String[] parts = enchantStr.split(":");
                if (parts.length == 2) {
                    Enchantment enchant = Enchantment.getByName(parts[0]);
                    int level = Integer.parseInt(parts[1]);
                    if (enchant != null) {
                        item.addUnsafeEnchantment(enchant, level);
                    }
                }
            }

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}