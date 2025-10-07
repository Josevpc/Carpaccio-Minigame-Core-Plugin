package carpaccio.minigameCore.core.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomLoot {
    private final Material material;
    private final int minAmount;
    private final int maxAmount;
    private final double dropChance;
    private final String displayName;
    private final List<String> lore;
    private final List<EnchantmentData> enchantments;

    public CustomLoot(Material material, int minAmount, int maxAmount, double dropChance) {
        this.material = material;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.dropChance = dropChance;
        this.displayName = null;
        this.lore = new ArrayList<>();
        this.enchantments = new ArrayList<>();
    }

    public CustomLoot(Material material, int minAmount, int maxAmount, double dropChance,
                      String displayName, List<String> lore, List<EnchantmentData> enchantments) {
        this.material = material;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.dropChance = dropChance;
        this.displayName = displayName;
        this.lore = lore != null ? lore : new ArrayList<>();
        this.enchantments = enchantments != null ? enchantments : new ArrayList<>();
    }

    public ItemStack generateDrop(Random random) {
        if (random.nextDouble() > dropChance) {
            return null;
        }

        int amount = minAmount + random.nextInt(maxAmount - minAmount + 1);
        ItemStack item = new ItemStack(material, amount);

        if (displayName != null || !lore.isEmpty() || !enchantments.isEmpty()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                if (displayName != null) {
                    meta.setDisplayName(displayName);
                }
                if (!lore.isEmpty()) {
                    meta.setLore(lore);
                }
                item.setItemMeta(meta);
            }

            if (!enchantments.isEmpty()){
                for (EnchantmentData enchData : enchantments) {
                    Enchantment enchantment = Enchantment.getByName(enchData.enchantment);
                    item.addUnsafeEnchantment(enchantment, enchData.level);
                }
            }
        }

        return item;
    }

    public static class EnchantmentData {
        public final String enchantment;
        public final int level;

        public EnchantmentData(String enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }
    }
}