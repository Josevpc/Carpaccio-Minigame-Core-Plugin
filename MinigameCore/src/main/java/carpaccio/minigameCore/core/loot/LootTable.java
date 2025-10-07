package carpaccio.minigameCore.core.loot;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootTable {
    private final String id;
    private final List<CustomLoot> loots;
    private final Random random;

    public LootTable(String id) {
        this.id = id;
        this.loots = new ArrayList<>();
        this.random = new Random();
    }

    public void addLoot(CustomLoot loot) {
        loots.add(loot);
    }

    public List<CustomLoot> getLoots() {
        return loots;
    }

    public List<ItemStack> generateDrops(Location location) {
        List<ItemStack> drops = new ArrayList<>();

        for (CustomLoot loot : loots) {
            ItemStack drop = loot.generateDrop(random);
            if (drop != null) {
                drops.add(drop);
            }
        }
        return drops;
    }

    public String getId() {
        return id;
    }
}