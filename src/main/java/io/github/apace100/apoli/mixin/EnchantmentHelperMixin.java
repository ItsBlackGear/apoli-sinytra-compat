package io.github.apace100.apoli.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.power.type.ModifyEnchantmentLevelPowerType;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true)
    private static void apoli$modifyEnchantmentsOnLevelQuery(RegistryEntry<Enchantment> enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        ItemEnchantmentsComponent components = ModifyEnchantmentLevelPowerType.getAndUpdateModifiedEnchantments(stack, EnchantmentHelper.getEnchantments(stack));
        cir.setReturnValue(components.getLevel(enchantment));
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;)V", at = @At("STORE"))
    private static ItemEnchantmentsComponent apoli$modifyEnchantmentsOnForEach(ItemEnchantmentsComponent original, ItemStack stack) {
        return ModifyEnchantmentLevelPowerType.getAndUpdateModifiedEnchantments(stack, original);
    }

    @ModifyExpressionValue(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z"))
    private static boolean apoli$allowWorkableEmptiesInForEach(boolean original, ItemStack stack) {
        return original && !ModifyEnchantmentLevelPowerType.isWorkableEmptyStack(stack);
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/enchantment/EnchantmentHelper$ContextAwareConsumer;)V", at = @At("STORE"))
    private static ItemEnchantmentsComponent apoli$modifyEnchantmentsOnForEachWithContext(ItemEnchantmentsComponent original, ItemStack stack) {
        return ModifyEnchantmentLevelPowerType.getAndUpdateModifiedEnchantments(stack, original);
    }

    @ModifyVariable(method = "hasAnyEnchantmentsIn", at = @At("STORE"))
    private static ItemEnchantmentsComponent apoli$modifyEnchantmentsOnInTagQuery(ItemEnchantmentsComponent original, ItemStack stack) {
        return ModifyEnchantmentLevelPowerType.getAndUpdateModifiedEnchantments(stack, original);
    }

}
