package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyFovPowerType;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {

    private AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @ModifyReturnValue(method = "getFovMultiplier", at = @At(value = "RETURN", ordinal = 0))
    private float apoli$modifySpyglassFov(float original) {
        return PowerHolderComponent.modify(this, ModifyFovPowerType.class, original);
    }

    @Inject(method = "getFovMultiplier", at = @At("RETURN"), cancellable = true)
    private void apoli$modifyFov(CallbackInfoReturnable<Float> cir, @Share("fovValue") LocalRef<Float> fovValue) {
        float originalValue = cir.getReturnValue();
        float modifiedValue = PowerHolderComponent.modify(this, ModifyFovPowerType.class, originalValue);

        if (modifiedValue != originalValue) {
            cir.setReturnValue(modifiedValue);
        }
    }
}
