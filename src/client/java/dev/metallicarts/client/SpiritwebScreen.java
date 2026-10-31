package dev.metallicarts.client;

import dev.metallicarts.domain.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Read-only foundation. Opening the screen cannot grant, activate or edit powers. */
public final class SpiritwebScreen extends Screen {
    public SpiritwebScreen() { super(Component.translatable("screen.metallicarts.title")); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 16, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("screen.metallicarts.phase"), width / 2, 32, 0xFFFFCC77);
        Spiritweb state = MetallicArtsClient.snapshot;
        if (state == null) {
            graphics.centeredText(font, Component.translatable("screen.metallicarts.waiting"), width / 2, 64, 0xFFFFFFFF);
            return;
        }
        int left = Math.max(8, width / 2 - 150);
        for (int n = 0; n < Metal.values().length; n++) {
            Metal metal = Metal.values()[n];
            int x = left + (n / 8) * 155;
            int y = 58 + (n % 8) * 18;
            String access = (state.strength(Art.ALLOMANCY, metal) > 0 ? "A" : "-")
                + "/" + (state.strength(Art.FERUCHEMY, metal) > 0 ? "F" : "-");
            graphics.text(font, Component.translatable("metal.metallicarts." + metal.id()).append("  " + access), x, y, 0xFFFFFFFF);
        }
        graphics.centeredText(font, Component.translatable("screen.metallicarts.legend"), width / 2, 210, 0xFFAAAAAA);
    }
}
