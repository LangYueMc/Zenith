package dev.shadowsoffire.apotheosis.mixin;

import dev.shadowsoffire.apotheosis.api.HealEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LivingEntity.class, priority = 900)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "heal", at = @At(value = "HEAD"), argsOnly = true)
    private float healEvent(float value) {
        float amount = HealEvent.EVENT.invoker().onLivingHeal(this, value);
        return amount >= 0 ? amount : 0;
    }

}
