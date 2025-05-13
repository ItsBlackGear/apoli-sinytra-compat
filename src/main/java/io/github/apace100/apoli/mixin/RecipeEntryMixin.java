package io.github.apace100.apoli.mixin;

import com.mojang.serialization.DataResult;
import io.github.apace100.apoli.util.RecipeUtil;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("unchecked")
@Mixin(RecipeEntry.class)
public class RecipeEntryMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static <T extends Recipe<?>> T a(T value) {
        DataResult<Recipe<?>> result = RecipeUtil.validateRecipe(value);
        return (T) result.result().orElse(value);
    }
}
