package carpaccio.minigameCore.core.mobs;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class CustomMob {
    private final String id;
    private final EntityType entityType;
    private final String displayName;
    private final double health;
    private final double damage;
    private final double speed;
    private final boolean baby;
    private final ItemStack helmet;
    private final ItemStack chestplate;
    private final ItemStack leggings;
    private final ItemStack boots;
    private final ItemStack mainHand;
    private final ItemStack offHand;
    private final List<PotionEffect> potionEffects;
    private final String lootTableId;

    private CustomMob(Builder builder) {
        this.id = builder.id;
        this.entityType = builder.entityType;
        this.displayName = builder.displayName;
        this.health = builder.health;
        this.damage = builder.damage;
        this.speed = builder.speed;
        this.baby = builder.baby;
        this.helmet = builder.helmet;
        this.chestplate = builder.chestplate;
        this.leggings = builder.leggings;
        this.boots = builder.boots;
        this.mainHand = builder.mainHand;
        this.offHand = builder.offHand;
        this.potionEffects = builder.potionEffects;
        this.lootTableId = builder.lootTableId;
    }

    public Entity spawn(Location location) {
        Entity entity = location.getWorld().spawnEntity(location, entityType);

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;

            if (displayName != null) {
                living.setCustomName(displayName);
                living.setCustomNameVisible(true);
            }

            if (health > 0) {
                living.getAttribute(Attribute.MAX_HEALTH).setBaseValue(health);
                living.setHealth(health);
            }

            if (damage > 0 && living.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
                living.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
            }

            if (speed > 0) {
                living.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speed);
            }

            EntityEquipment equipment = living.getEquipment();
            if (equipment != null) {
                if (helmet != null) equipment.setHelmet(helmet);
                if (chestplate != null) equipment.setChestplate(chestplate);
                if (leggings != null) equipment.setLeggings(leggings);
                if (boots != null) equipment.setBoots(boots);
                if (mainHand != null) equipment.setItemInMainHand(mainHand);
                if (offHand != null) equipment.setItemInOffHand(offHand);

                equipment.setHelmetDropChance(0.0f);
                equipment.setChestplateDropChance(0.0f);
                equipment.setLeggingsDropChance(0.0f);
                equipment.setBootsDropChance(0.0f);
                equipment.setItemInMainHandDropChance(0.0f);
                equipment.setItemInOffHandDropChance(0.0f);
            }

            for (PotionEffect effect : potionEffects) {
                living.addPotionEffect(effect);
            }
        }

        return entity;
    }

    public String getId() {
        return id;
    }

    public String getLootTableId() {
        return lootTableId;
    }

    public static class Builder {
        private final String id;
        private final EntityType entityType;
        private String displayName;
        private double health = -1;
        private double damage = -1;
        private double speed = -1;
        private boolean baby = false;
        private ItemStack helmet;
        private ItemStack chestplate;
        private ItemStack leggings;
        private ItemStack boots;
        private ItemStack mainHand;
        private ItemStack offHand;
        private List<PotionEffect> potionEffects = new ArrayList<>();
        private String lootTableId;

        public Builder(String id, EntityType entityType) {
            this.id = id;
            this.entityType = entityType;
        }

        public Builder displayName(String name) {
            this.displayName = name;
            return this;
        }

        public Builder health(double health) {
            this.health = health;
            return this;
        }

        public Builder damage(double damage) {
            this.damage = damage;
            return this;
        }

        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }

        public Builder baby(boolean baby) {
            this.baby = baby;
            return this;
        }

        public Builder helmet(ItemStack helmet) {
            this.helmet = helmet;
            return this;
        }

        public Builder chestplate(ItemStack chestplate) {
            this.chestplate = chestplate;
            return this;
        }

        public Builder leggings(ItemStack leggings) {
            this.leggings = leggings;
            return this;
        }

        public Builder boots(ItemStack boots) {
            this.boots = boots;
            return this;
        }

        public Builder mainHand(ItemStack item) {
            this.mainHand = item;
            return this;
        }

        public Builder offHand(ItemStack item) {
            this.offHand = item;
            return this;
        }

        public Builder addPotionEffect(PotionEffect effect) {
            this.potionEffects.add(effect);
            return this;
        }

        public Builder lootTable(String lootTableId) {
            this.lootTableId = lootTableId;
            return this;
        }

        public CustomMob build() {
            return new CustomMob(this);
        }
    }
}