package com.cfog1.advancedwatermechanics.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow
    public abstract boolean isSwimming();

    @Shadow
    public abstract float getSpeed();

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Redirect(method = "getDestroySpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean redirected(Player player, TagKey<Fluid> tagKey) {
        return false;
    }

    @Redirect(method = "getDestroySpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean onGroundCheck(Player player) {
        return player.level().getFluidState(player.blockPosition()).is(FluidTags.WATER) || player.onGround();
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void travel(Vec3 travelVec, CallbackInfo ci) {
        if (getSpeed() > 0.1 && isInWater() && !isSwimming() && !isPassenger()) {
            double yAngle = getLookAngle().y;
            double d = yAngle < -0.2 ? 0.085 : 0.06;

            if (yAngle <= 0
                    || jumping
                    || !level().getBlockState(BlockPos.containing(getX(), getY() + 1 - 0.1, getZ())).getFluidState().isEmpty()) {
                Vec3 deltaMovement = getDeltaMovement();
                setDeltaMovement(deltaMovement.add(0, (yAngle - deltaMovement.y) * d, 0));
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        setNoGravity(isInWater() && !isSwimming() && !isPassenger());
    }
}