package dev.shadowsoffire.apotheosis.ench.enchantments.corrupted;

import dev.shadowsoffire.apotheosis.Apoth;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingEntityDamageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class BerserkersFuryEnchant extends Enchantment {

    public BerserkersFuryEnchant() {
        super(Rarity.VERY_RARE, EnchantmentCategory.ARMOR_CHEST, new EquipmentSlot[]{EquipmentSlot.CHEST});
    }

    @Override
    public int getMinCost(int level) {
        return 50 + level * 40;
    }

    @Override
    public int getMaxCost(int level) {
        return 200;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isCurse() {
        return true;
    }

    @Override
    public Component getFullname(int level) {
        return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_RED);
    }

    /**
     * Handles the application of Berserker's Fury.
     */
    @SuppressWarnings("deprecation")
    public void livingHurt() {
        LivingEntityDamageEvents.HURT.register(e -> {
            LivingEntity user = e.damaged;
            if (e.damageSource.getEntity() instanceof Entity && user.getEffect(MobEffects.DAMAGE_RESISTANCE) == null) {
                int level = EnchantmentHelper.getEnchantmentLevel(this, user);
                if (level > 0) {
                    if (isOnCooldown(BuiltInRegistries.ENCHANTMENT.getKey(this), 900, user)) return;
                    user.invulnerableTime = 0;
                    user.hurt(user.damageSources().source(Apoth.DamageTypes.CORRUPTED), (float) Math.pow(2.5, level));
                    user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 500, level - 1));
                    user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, level - 1));
                    user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, level - 1));
                    startCooldown(BuiltInRegistries.ENCHANTMENT.getKey(this), user);
                }
            }
        });

    }

    /**
     * Checks if the affix is still on cooldown, if a cooldown was set via {link #startCooldown(Affix, int, LivingEntity)}
     */
    private static boolean isOnCooldown(ResourceLocation id, int cooldown, LivingEntity entity) {
        long lastApplied = entity.getCustomData().getLong("apoth.affix_cooldown." + id.toString());
        return lastApplied != 0 && lastApplied + cooldown >= entity.level().getGameTime();
    }

    /**
     * Records the current time as a cooldown tracker. Used in conjunction with {link #isOnCooldown(Affix, int, LivingEntity)}
     */
    private static void startCooldown(ResourceLocation id, LivingEntity entity) {
        entity.getCustomData().putLong("apoth.affix_cooldown." + id.toString(), entity.level().getGameTime());
    }

}
