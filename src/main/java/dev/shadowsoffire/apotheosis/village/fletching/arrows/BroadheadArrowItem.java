package dev.shadowsoffire.apotheosis.village.fletching.arrows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BroadheadArrowItem extends ArrowItem implements IApothArrowItem {

    public BroadheadArrowItem(Properties props) {
        super(props);
    }

    @Override
    public AbstractArrow createArrow(Level world, ItemStack stack, LivingEntity shooter) {
        return new BroadheadArrowEntity(shooter, world).bleed();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("info.zenith.broadhead_arrow").withStyle(ChatFormatting.RED));
    }

    @Override
    public AbstractArrow fromDispenser(Level world, double x, double y, double z) {
        AbstractArrow e = new BroadheadArrowEntity(world, x, y, z).bleed();
        e.pickup = AbstractArrow.Pickup.ALLOWED;
        return e;
    }

}
