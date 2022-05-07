package safro.apotheosis.garden;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import safro.apotheosis.Apotheosis;
import safro.apotheosis.api.config.Configuration;

import java.io.File;

public class GardenModule {
    public static int maxCactusHeight = 5;
    public static int maxReedHeight = 255;
    public static int maxBambooHeight = 32;

    public static Item ENDER_LEAD = register("ender_lead", new EnderLeadItem());

    public static void init() {
        reload(false);

        Apotheosis.HELPER.addShapeless(ENDER_LEAD, Items.ENDER_PEARL, Items.LEAD, Items.GOLD_INGOT);

        ComposterBlock.COMPOSTABLES.put(Blocks.CACTUS.asItem(), 0.5F);
        ComposterBlock.COMPOSTABLES.put(Blocks.SUGAR_CANE.asItem(), 0.5F);
    }

    public static void reload(boolean e) {
        Configuration c = new Configuration(new File(Apotheosis.configDir, "garden.cfg"));
        c.setTitle("Apotheosis Garden Module Configuration");
        maxCactusHeight = c.getInt("Cactus Height", "general", maxCactusHeight, 1, 512, "The max height a stack of cacti may grow to.  Vanilla is 3.  Values greater than 32 are uncapped growth.");
        maxReedHeight = c.getInt("Reed Height", "general", maxReedHeight, 1, 512, "The max height a stack of reeds may grow to.  Vanilla is 3.  Values greater than 32 are uncapped growth.");
        maxBambooHeight = c.getInt("Bamboo Height", "general", maxBambooHeight, 1, 64, "The max height a stack of bamboo may grow to.  Vanilla is 16.");
        if (!e && c.hasChanged()) c.save();
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registry.ITEM, new ResourceLocation(Apotheosis.MODID, name), item);
    }
}
