package dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DamageReductionAffix.DamageType;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Objects;

/**
 * Increases damage reduction to a certain damage type.
 */
public class DamageReductionBonus extends GemBonus {

    protected final DamageType type;
    protected final Map<LootRarity, StepFunction> values;

    public static Codec<DamageReductionBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            DamageType.CODEC.fieldOf("damage_type").forGetter(a -> a.type),
            VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, DamageReductionBonus::new));

    public DamageReductionBonus(GemClass gemClass, DamageType type, Map<LootRarity, StepFunction> values) {
        super(Apotheosis.loc("damage_reduction"), gemClass);
        this.type = type;
        this.values = values;
    }

    @Override
    public float onHurt(ItemStack gem, LootRarity rarity, DamageSource src, LivingEntity user, float amount) {
        if (src.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || src.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) return amount;
        if (Apotheosis.enableDebug) AdventureModule.LOGGER.info("Damage reduction percentage from gem: %{},", Affix.fmt(100 * this.values.get(rarity).get(0)));
        if (this.type.test(src)) return amount * (1 - this.values.get(rarity).get(0));
        return super.onHurt(gem, rarity, src, user, amount);
    }


    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        float level = this.values.get(rarity).get(0);
        return Component.translatable("affix.zenith:damage_reduction.desc", Component.translatable("misc.zenith." + this.type.getId()), Affix.fmt(100 * level)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public GemBonus validate() {
        Preconditions.checkNotNull(this.type, "Invalid DamageReductionBonus with null type");
        Preconditions.checkNotNull(this.values, "Invalid DamageReductionBonus with null values");
        Preconditions.checkArgument(this.values.entrySet().stream().mapMulti((entry, consumer) -> {
            consumer.accept(entry.getKey());
            consumer.accept(entry.getValue());
        }).allMatch(Objects::nonNull), "Invalid DamageReductionBonus with invalid values");
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return 0;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

}