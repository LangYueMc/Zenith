package dev.shadowsoffire.apotheosis.advancements;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds.Ints;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;
import java.util.function.Predicate;

public class ExtendedInvTrigger extends InventoryChangeTrigger {

    @Override
    public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate andPred, DeserializationContext conditionsParser) {
        JsonObject slots = GsonHelper.getAsJsonObject(json, "slots", new JsonObject());
        Ints occupied = Ints.fromJson(slots.get("occupied"));
        Ints full = Ints.fromJson(slots.get("full"));
        Ints empty = Ints.fromJson(slots.get("empty"));
        ItemPredicate[] predicate = ItemPredicate.fromJsonArray(json.get("items"));
        if (json.has("zenith")) predicate = this.deserializeZenith(json.getAsJsonObject("zenith"));
        return new TriggerInstance(andPred, occupied, full, empty, predicate);
    }

    ItemPredicate[] deserializeZenith(JsonObject json) {
        String type = json.get("type").getAsString();
        if ("spawn_egg".equals(type))
            return new ItemPredicate[]{new TrueItemPredicate(s -> s.getItem() instanceof SpawnEggItem)};
        if ("enchanted".equals(type)) {
            Enchantment ench = json.has("enchantment") ? BuiltInRegistries.ENCHANTMENT.get(new ResourceLocation(json.get("enchantment").getAsString())) : null;
            Ints bound = Ints.fromJson(json.get("level"));
            return new ItemPredicate[]{new TrueItemPredicate(s -> {
                Map<Enchantment, Integer> enchMap = EnchantmentHelper.getEnchantments(s);
                if (ench != null) return bound.matches(enchMap.getOrDefault(ench, 0));
                return enchMap.values().stream().anyMatch(bound::matches);
            })};
        }
        if ("nbt".equals(type)) {
            CompoundTag tag;
            try {
                tag = TagParser.parseTag(GsonHelper.convertToString(json.get("nbt"), "nbt"));
            } catch (CommandSyntaxException e) {
                throw new RuntimeException(e);
            }
            return new ItemPredicate[]{new TrueItemPredicate(s -> {
                if (!s.hasTag()) return false;
                for (String key : tag.getAllKeys()) {
                    if (!tag.get(key).equals(s.getTag().get(key))) return false;
                }
                return true;
            })};

        }
        return new ItemPredicate[0];
    }

    private static class TrueItemPredicate extends ItemPredicate {

        Predicate<ItemStack> predicate;

        TrueItemPredicate(Predicate<ItemStack> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean matches(ItemStack item) {
            return this.predicate.test(item);
        }
    }

}
