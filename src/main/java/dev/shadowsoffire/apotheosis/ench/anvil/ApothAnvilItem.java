package dev.shadowsoffire.apotheosis.ench.anvil;

import dev.shadowsoffire.apotheosis.Apotheosis;
import io.github.fabricators_of_create.porting_lib.enchant.CustomEnchantingBehaviorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;

public class ApothAnvilItem extends BlockItem implements CustomEnchantingBehaviorItem {

    public ApothAnvilItem(Block block) {
        super(block, new Properties());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return stack.getCount() == 1 && (enchantment == Enchantments.UNBREAKING) || CustomEnchantingBehaviorItem.super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    //@Override
    public int getEnchantmentValue(ItemStack stack) {
        return 50;
    }

}
