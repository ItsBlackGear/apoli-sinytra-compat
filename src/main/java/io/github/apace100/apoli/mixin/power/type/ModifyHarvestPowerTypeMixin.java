package io.github.apace100.apoli.mixin.power.type;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.type.ModifyHarvestPowerType;
import io.github.apace100.apoli.util.SavedBlockPosition;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public abstract class ModifyHarvestPowerTypeMixin {

	@Mixin(AbstractBlock.class)
	public abstract static class BlockBreakingDeltaProxy implements ToggleableFeature {

		@ModifyVariable(method = "calcBlockBreakingDelta", at = @At(value = "STORE"), ordinal = 0)
		private int apoli$modifyHarvest(int original, BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
			return PowerHolderComponent.getPowerTypes(player, ModifyHarvestPowerType.class)
				.stream()
				.filter(powerType -> powerType.doesApply(world, pos))
				.max(ModifyHarvestPowerType::compareTo)
				.map(power -> power.isAllowed() ? 30 : 100)
				.orElse(original);
		}

	}

	@Mixin(ServerPlayerInteractionManager.class)
	public abstract static class HarvestabilityProxy {

		@Shadow
		protected ServerWorld world;

		@Shadow
		@Final
		protected ServerPlayerEntity player;

		// Inject at the beginning to cache the block position being broken
		@Inject(method = "tryBreakBlock", at = @At("HEAD"))
		private void apoli$cacheBreakingBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Share(value = "breakingBlock", namespace = Apoli.MODID) LocalRef<SavedBlockPosition> breakingBlockRef) {
			breakingBlockRef.set(new SavedBlockPosition(this.world, pos));
		}

		// Modify the boolean variable holding the result of player.canHarvest()
		@ModifyVariable(
			method = "tryBreakBlock",
			// Target the STORE operation for the second boolean variable in the relevant scope.
			// 'bl' is the first (ordinal 0), the result of canHarvest() is the second (ordinal 1).
			at = @At(value = "STORE"),
			ordinal = 1 // Target the second boolean variable stored in this part of the method
		)
		private boolean apoli$modifyHarvestResult(boolean originalHarvestResult, @Share(value = "breakingBlock", namespace = Apoli.MODID) LocalRef<SavedBlockPosition> breakingBlockRef) {
			// Apply the power logic, using the original harvest result as the fallback
			return PowerHolderComponent.getPowerTypes(this.player, ModifyHarvestPowerType.class)
				.stream()
				.filter(powerType -> powerType.doesApply(breakingBlockRef.get())) // Use cached position
				.max(ModifyHarvestPowerType::compareTo)
				.map(ModifyHarvestPowerType::isAllowed)
				.orElse(originalHarvestResult); // Fallback to original result if no power applies
		}
	}
}
