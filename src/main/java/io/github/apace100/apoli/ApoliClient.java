package io.github.apace100.apoli;

import io.github.apace100.apoli.integration.PowerIntegrationClient;
import io.github.apace100.apoli.networking.ModPacketsS2C;
import io.github.apace100.apoli.networking.packet.c2s.UseActivePowerTypesC2SPacket;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.type.Active;
import io.github.apace100.apoli.power.type.PowerType;
import io.github.apace100.apoli.registry.ApoliClassDataClient;
import io.github.apace100.apoli.screen.GameHudRender;
import io.github.apace100.apoli.screen.HudManager;
import io.github.apace100.apoli.screen.NeoPowerHudRenderer;
import io.github.apace100.apoli.screen.PowerHudRenderer;
import io.github.apace100.apoli.util.ApoliConfigClient;
import io.github.apace100.apoli.util.keybinding.KeyBindingUtil;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ApoliClient implements ClientModInitializer {

	public static KeyBinding showPowersOnUsabilityHint;

	public static final Map<String, Boolean> lastKeyBindingStates = new HashMap<>();
	public static boolean shouldReloadWorldRenderer = false;

	@Override
	public void onInitializeClient() {

		showPowersOnUsabilityHint = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.apoli.usability_hint.show_powers", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category." + Apoli.MODID));
		ModPacketsS2C.register();

		ApoliClassDataClient.registerAll();
		PowerIntegrationClient.register();

//		GameHudRender.HUD_RENDERS.add(new PowerHudRenderer());
		HudManager.registerHudRenderer(new PowerHudRenderer());
		HudManager.init();

		AutoConfig.register(ApoliConfigClient.class, JanksonConfigSerializer::new);
		Apoli.config = AutoConfig.getConfigHolder(ApoliConfigClient.class).getConfig();

	}

	public static <P extends PowerType & Active> void performActivePowerTypes(List<P> activePowerTypes) {

		List<Identifier> powerTypeIds = activePowerTypes
			.stream()
			.peek(pt -> {if (pt.isActive()) {pt.onUse();}})
			.map(PowerType::getPower)
			.map(Power::getId)
			.toList();

		if (!powerTypeIds.isEmpty()) {
			ClientPlayNetworking.send(new UseActivePowerTypesC2SPacket(powerTypeIds));
		}

	}

	/**
	 * 	Add aliases to {@link KeyBindingUtil#ALIASES} instead.
	 */
	@Deprecated(forRemoval = true)
	public static void registerPowerKeybinding(String keyId, KeyBinding keyBinding) {
		KeyBindingUtil.ALIASES.addAlias(keyId, keyBinding.getTranslationKey());
	}

	/**
	 * 	Use {@link KeyBindingUtil#getKeyBinding(String)} instead.
	 */
	@Deprecated(forRemoval = true)
	@Nullable
	public static KeyBinding getKeyBinding(String keyBindingId) {
		return KeyBindingUtil.getKeyBinding(keyBindingId).orElse(null);
	}

}
