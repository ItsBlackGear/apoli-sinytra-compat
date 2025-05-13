package io.github.apace100.apoli.screen;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class HudManager {
    private static final List<GameHudRender> HUD_RENDERERS = new ArrayList<>();

    public static void registerHudRenderer(GameHudRender renderer) {
        HUD_RENDERERS.add(renderer);
    }

    public static void init() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            for (GameHudRender renderer : HUD_RENDERERS) {
                renderer.render(drawContext, tickDelta);
            }
        });
    }
}