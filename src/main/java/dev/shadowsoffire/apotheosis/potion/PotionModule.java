package dev.shadowsoffire.apotheosis.potion;

import dev.emi.trinkets.api.TrinketsApi;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.objects.GlowyBlockItem;
import dev.shadowsoffire.placebo.config.Configuration;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingEntityEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.living.LivingEntityLootEvents;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class PotionModule {

    public static final Logger LOGGER = LogManager.getLogger("Zenith : Potion");
    public static final ResourceLocation POTION_TEX = Apotheosis.loc("textures/potions.png");
    public static final PotionCharmItem POTION_CHARM = new PotionCharmItem();
    public static final Item LUCKY_FOOT = new GlowyBlockItem.GlowyItem(new Item.Properties());
    public static final RegistryObject<Item> SKULL_FRAGMENT = new RegistryObject(new ResourceLocation("wstweaks", "fragment"), Registries.ITEM);

    public static int knowledgeMult = 4;
    static boolean charmsInTrinketsOnly = false;

    public static void init() {
        potions();
        items();
        serializers();
        drops();

        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            LivingEntityEvents.TICK.register(entity -> {
                TrinketsApi.getTrinketComponent(entity).ifPresent(c -> c.forEach((slotReference, stack) -> {
                    if (stack.getItem() instanceof PotionCharmItem charm) {
                        charm.charmLogic(stack, entity.level(), entity, slotReference.index(), false);
                    }
                }));
            });
        }

        reload(false);
    }

    public static void items() {
        Apoth.registerItem(LUCKY_FOOT, "lucky_foot");
        Apoth.registerItem(POTION_CHARM, "potion_charm");
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(PotionCharmItem::fillItemCategory);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> entries.accept(LUCKY_FOOT));
    }

    public static void serializers() {
        Apoth.registerSerializer("potion_charm", PotionCharmRecipe.Serializer.INSTANCE);
        Apoth.registerSerializer("potion_charm_enchanting", PotionEnchantingRecipe.SERIALIZER);
    }

    public static void drops() {
        LivingEntityLootEvents.DROPS.register((target, source, drops, lootingLevel, recentlyHit) -> {
            if (target instanceof Rabbit rabbit) {
                if (rabbit.level().random.nextFloat() < 0.045F + 0.045F * lootingLevel) {
                    drops.clear();
                    drops.add(new ItemEntity(rabbit.level(), rabbit.getX(), rabbit.getY(), rabbit.getZ(), new ItemStack(PotionModule.LUCKY_FOOT)));
                }
            }
            return false;
        });
    }

    public static void reload(boolean e) {
        Configuration config = new Configuration(new File(Apotheosis.configDir, "potion.cfg"));
        config.setTitle("Zenith Potion Module Configuration");
        knowledgeMult = config.getInt("Knowledge XP Multiplier", "general", knowledgeMult, 1, Integer.MAX_VALUE,
                "The strength of Ancient Knowledge.  This multiplier determines how much additional xp is granted.\nServer-authoritative.");
        charmsInTrinketsOnly = config.getBoolean("Restrict Charms to Trinkets", "general", charmsInTrinketsOnly, "If Potion Charms will only work when in a trinkets slot, instead of in the inventory.");

        String[] defExt = {BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.NIGHT_VISION).toString(), BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.HEALTH_BOOST).toString()};
        String[] names = config.getStringList("Extended Potion Charms", "general", defExt,
                "A list of effects that, when as charms, will be applied and reapplied at a longer threshold to avoid issues at low durations, like night vision.\nServer-authoritative.");
        PotionCharmItem.EXTENDED_POTIONS.clear();
        for (String s : names) {
            try {
                PotionCharmItem.EXTENDED_POTIONS.add(new ResourceLocation(s));
            } catch (ResourceLocationException ex) {
                LOGGER.error("Invalid extended potion charm entry {} will be ignored.", s);
            }
        }
        String[] defDis = {"zenith_attributes:flying"};
        String[] disabled = config.getStringList("Disabled Potion Charms", "general", defDis,
                "A list of effects that will be unable to be crafted into charms.\nServer-authoritative.");
        PotionCharmItem.DISABLED_POTIONS.clear();
        for (String s : disabled) {
            try {
                PotionCharmItem.DISABLED_POTIONS.add(new ResourceLocation(s));
            } catch (ResourceLocationException ex) {
                LOGGER.error("Invalid disabled potion charm entry {} will be ignored.", s);
            }
        }

        if (!e && config.hasChanged()) config.save();
    }

    public static void potions() {
        PotionBrewing.addMix(Potions.AWKWARD, Items.SHULKER_SHELL, Potion.RESISTANCE);
        PotionBrewing.addMix(Potion.RESISTANCE, Items.REDSTONE, Potion.LONG_RESISTANCE);
        PotionBrewing.addMix(Potion.RESISTANCE, Items.GLOWSTONE_DUST, Potion.STRONG_RESISTANCE);

        PotionBrewing.addMix(Potions.AWKWARD, Items.GOLDEN_APPLE, Potion.ABSORPTION);
        PotionBrewing.addMix(Potion.ABSORPTION, Items.REDSTONE, Potion.LONG_ABSORPTION);
        PotionBrewing.addMix(Potion.ABSORPTION, Items.GLOWSTONE_DUST, Potion.STRONG_ABSORPTION);

        PotionBrewing.addMix(Potions.AWKWARD, Items.MUSHROOM_STEW, Potion.HASTE);
        PotionBrewing.addMix(Potion.HASTE, Items.REDSTONE, Potion.LONG_HASTE);
        PotionBrewing.addMix(Potion.HASTE, Items.GLOWSTONE_DUST, Potion.STRONG_HASTE);

        PotionBrewing.addMix(Potion.HASTE, Items.FERMENTED_SPIDER_EYE, Potion.FATIGUE);
        PotionBrewing.addMix(Potion.LONG_HASTE, Items.FERMENTED_SPIDER_EYE, Potion.LONG_FATIGUE);
        PotionBrewing.addMix(Potion.STRONG_HASTE, Items.FERMENTED_SPIDER_EYE, Potion.STRONG_FATIGUE);
        PotionBrewing.addMix(Potion.FATIGUE, Items.REDSTONE, Potion.LONG_FATIGUE);
        PotionBrewing.addMix(Potion.FATIGUE, Items.GLOWSTONE_DUST, Potion.STRONG_FATIGUE);

        if (SKULL_FRAGMENT.isPresent()) PotionBrewing.addMix(Potions.AWKWARD, SKULL_FRAGMENT.get(), Potion.WITHER);
        else PotionBrewing.addMix(Potions.AWKWARD, Items.WITHER_SKELETON_SKULL, Potion.WITHER);
        PotionBrewing.addMix(Potion.WITHER, Items.REDSTONE, Potion.LONG_WITHER);
        PotionBrewing.addMix(Potion.WITHER, Items.GLOWSTONE_DUST, Potion.STRONG_WITHER);

        PotionBrewing.addMix(Potions.AWKWARD, LUCKY_FOOT, Potions.LUCK);

        PotionBrewing.addMix(Potions.SLOW_FALLING, Items.FERMENTED_SPIDER_EYE, Potion.LEVITATION);
    }

    public class Potion {

        public static final net.minecraft.world.item.alchemy.Potion RESISTANCE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600)), "resistance");
        public static final net.minecraft.world.item.alchemy.Potion LONG_RESISTANCE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 9600)), "long_resistance");
        public static final net.minecraft.world.item.alchemy.Potion STRONG_RESISTANCE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("resistance", new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1800, 1)), "strong_resistance");
        public static final net.minecraft.world.item.alchemy.Potion ABSORPTION = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("absorption", new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1)), "absorption");
        public static final net.minecraft.world.item.alchemy.Potion LONG_ABSORPTION = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("absorption", new MobEffectInstance(MobEffects.ABSORPTION, 3600, 1)), "long_absorption");
        public static final net.minecraft.world.item.alchemy.Potion STRONG_ABSORPTION = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("absorption", new MobEffectInstance(MobEffects.ABSORPTION, 600, 3)), "strong_absorption");
        public static final net.minecraft.world.item.alchemy.Potion HASTE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 3600)), "haste");
        public static final net.minecraft.world.item.alchemy.Potion LONG_HASTE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 9600)), "long_haste");
        public static final net.minecraft.world.item.alchemy.Potion STRONG_HASTE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("haste", new MobEffectInstance(MobEffects.DIG_SPEED, 1800, 1)), "strong_haste");
        public static final net.minecraft.world.item.alchemy.Potion FATIGUE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 3600)), "fatigue");
        public static final net.minecraft.world.item.alchemy.Potion LONG_FATIGUE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 9600)), "long_fatigue");
        public static final net.minecraft.world.item.alchemy.Potion STRONG_FATIGUE = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("fatigue", new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1800, 1)), "strong_fatigue");
        public static final net.minecraft.world.item.alchemy.Potion WITHER = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("wither", new MobEffectInstance(MobEffects.WITHER, 3600)), "wither");
        public static final net.minecraft.world.item.alchemy.Potion LONG_WITHER = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("wither", new MobEffectInstance(MobEffects.WITHER, 9600)), "long_wither");
        public static final net.minecraft.world.item.alchemy.Potion STRONG_WITHER = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("wither", new MobEffectInstance(MobEffects.WITHER, 1800, 1)), "strong_wither");
        public static final net.minecraft.world.item.alchemy.Potion LEVITATION = Apoth.registerPot(new net.minecraft.world.item.alchemy.Potion("levitation", new MobEffectInstance(MobEffects.LEVITATION, 2400)), "levitation");
    }
}
