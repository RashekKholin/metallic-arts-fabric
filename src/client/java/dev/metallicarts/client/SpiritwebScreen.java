package dev.metallicarts.client;

import dev.metallicarts.domain.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Ownership is read-only; supported burn buttons send server-validated intent. */
public final class SpiritwebScreen extends Screen {
    public SpiritwebScreen() { super(Component.translatable("screen.metallicarts.title")); }
    @Override protected void init(){
        for(int i=0;i<4;i++){
            final int index=i;
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                Component.literal(dev.metallicarts.allomancy.AllomancyConfig.SUPPORTED.get(i).id()),
                button->AllomancyClient.cycle(index)).bounds(width/2-150+i*76,206,72,20).build());
        }
    }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 16, 0xFFFFFFFF);
        graphics.centeredText(font, Component.literal("Phase 3 development • Steel / Iron / Pewter / Tin"), width / 2, 32, 0xFFFFCC77);
        var modes=new StringBuilder();
        for(int i=0;i<4;i++)modes.append(dev.metallicarts.allomancy.AllomancyConfig.SUPPORTED.get(i).id()).append(": ")
            .append(dev.metallicarts.allomancy.BurnMode.from(AllomancyClient.status.mode(i)).name().toLowerCase(java.util.Locale.ROOT)).append("  ");
        if((AllomancyClient.status.flags()&4)!=0)modes.append("targets: ").append(AllomancyClient.status.flags()>>8);
        graphics.centeredText(font,Component.literal(modes.toString()),width/2,44,0xFF77BBFF);
        Spiritweb state = MetallicArtsClient.snapshot;
        if (state == null) {
            graphics.centeredText(font, Component.translatable("screen.metallicarts.waiting"), width / 2, 64, 0xFFFFFFFF);
            return;
        }
        int left = Math.max(8, width / 2 - 150);
        for (int n = 0; n < Metal.values().length; n++) {
            Metal metal = Metal.values()[n];
            int x = left + (n / 8) * 155;
            int y = 58 + (n % 8) * 16;
            String access = (state.strength(Art.ALLOMANCY, metal) > 0 ? "A" : "-")
                + "/" + (state.strength(Art.FERUCHEMY, metal) > 0 ? "F" : "-");
            long ordinary=dev.metallicarts.allomancy.ReserveLedger.valid(state)?dev.metallicarts.allomancy.ReserveLedger.ordinary(state,metal):0;
            String reserve = ordinary<1000?" | "+ordinary+"µg":String.format(java.util.Locale.ROOT, " | %.4fg", ordinary / 1_000_000.0);
            graphics.text(font, Component.translatable("metal.metallicarts." + metal.id()).append("  " + access + reserve), x, y, 0xFFFFFFFF);
        }
        graphics.centeredText(font, Component.translatable("screen.metallicarts.legend"), width / 2, 184, 0xFFAAAAAA);
        graphics.centeredText(font,Component.literal("Ordinary fuel (g / µg); invested metal reserved"),width/2,195,0xFFAAAAAA);
        graphics.centeredText(font,Component.literal("G select / H clear • R push / V pull / C coin"),width/2,231,0xFFAAAAAA);
    }
}
