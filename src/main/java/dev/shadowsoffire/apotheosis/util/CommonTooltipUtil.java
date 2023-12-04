package dev.shadowsoffire.apotheosis.util;

import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class CommonTooltipUtil {

    public static void appendBlockStats(Level world, BlockState state, Consumer<Component> tooltip) {
        float maxEterna = EnchantingStatRegistry.getMaxEterna(state, world, BlockPos.ZERO);
        float eterna = EnchantingStatRegistry.getEterna(state, world, BlockPos.ZERO);
        float quanta = EnchantingStatRegistry.getQuanta(state, world, BlockPos.ZERO);
        float arcana = EnchantingStatRegistry.getArcana(state, world, BlockPos.ZERO);
        float rectification = EnchantingStatRegistry.getQuantaRectification(state, world, BlockPos.ZERO);
        int clues = EnchantingStatRegistry.getBonusClues(state, world, BlockPos.ZERO);
        if (eterna != 0 || quanta != 0 || arcana != 0 || rectification != 0 || clues != 0) {
            tooltip.accept(Component.translatable("info.zenith.ench_stats").withStyle(ChatFormatting.GOLD));
        }
        if (eterna != 0) {
            if (eterna > 0) {
                tooltip.accept(Component.translatable("info.zenith.eterna.p", String.format("%.2f", eterna), String.format("%.2f", maxEterna)).withStyle(ChatFormatting.GREEN));
            } else
                tooltip.accept(Component.translatable("info.zenith.eterna", String.format("%.2f", eterna)).withStyle(ChatFormatting.GREEN));
        }
        if (quanta != 0) {
            tooltip.accept(Component.translatable("info.zenith.quanta" + (quanta > 0 ? ".p" : ""), String.format("%.2f", quanta)).withStyle(ChatFormatting.RED));
        }
        if (arcana != 0) {
            tooltip.accept(Component.translatable("info.zenith.arcana" + (arcana > 0 ? ".p" : ""), String.format("%.2f", arcana)).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (rectification != 0) {
            tooltip.accept(Component.translatable("info.zenith.rectification" + (rectification > 0 ? ".p" : ""), String.format("%.2f", rectification)).withStyle(ChatFormatting.YELLOW));
        }
        if (clues != 0) {
            tooltip.accept(Component.translatable("info.zenith.clues" + (clues > 0 ? ".p" : ""), String.format("%d", clues)).withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    public static void appendTableStats(Level world, BlockPos pos, Consumer<Component> tooltip) {
        ApothEnchantmentMenu.TableStats stats = ApothEnchantmentMenu.gatherStats(world, pos);
        tooltip.accept(Component.translatable("info.zenith.eterna.t", String.format("%.2f", stats.eterna()), String.format("%.2f", EnchantingStatRegistry.getAbsoluteMaxEterna())).withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.translatable("info.zenith.quanta.t", String.format("%.2f", Math.min(100, stats.quanta()))).withStyle(ChatFormatting.RED));
        tooltip.accept(Component.translatable("info.zenith.arcana.t", String.format("%.2f", Math.min(100, stats.arcana()))).withStyle(ChatFormatting.DARK_PURPLE));
        tooltip.accept(Component.translatable("info.zenith.rectification.t", String.format("%.2f", Mth.clamp(stats.rectification(), -100, 100))).withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.translatable("info.zenith.clues.t", String.format("%d", stats.clues())).withStyle(ChatFormatting.DARK_AQUA));
    }

}
