package dev.metallicarts.client;

import dev.metallicarts.domain.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Ownership is read-only; supported burn buttons send server-validated intent. */
public final class SpiritwebScreen extends Screen {
    private boolean wheel;
    private int page,emotion,refresh;
    public SpiritwebScreen() { super(Component.translatable("screen.metallicarts.title")); }
    @Override protected void init(){
        AdvancedArtsClient.action(3,0);
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.translatable("metallicarts.guide.title"),b->minecraft.setScreen(new GuideScreen())).bounds(8,8,70,18).build());
        int burnWidth=Math.min(72,(width-88)/4),burnLeft=(width-4*(burnWidth+4)+4)/2;
        for(int i=0;i<4;i++){
            final int index=page*4+i;
            double angle=-Math.PI/2+i*Math.PI/2;
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
                UiText.of(dev.metallicarts.allomancy.AllomancyConfig.SUPPORTED.get(index).id()),
                button->AllomancyClient.cycle(index)).bounds(
                    wheel?width/2+(int)Math.round(Math.cos(angle)*70)-36:burnLeft+i*(burnWidth+4),
                    wheel?120+(int)Math.round(Math.sin(angle)*60)-10:206,wheel?72:burnWidth,20).build());
        }
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.c4dd3c8cdd8d"),b->{page=Math.floorMod(page-1,4);rebuildWidgets();}).bounds(12,207,24,18).build());
        addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.091385be99b4"),b->{page=(page+1)%4;rebuildWidgets();}).bounds(width-36,207,24,18).build());
        if(page==1)addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.53b1d9dd0bf4", (dev.metallicarts.arts.EmotionProfile.Emotion.values()[emotion].name().toLowerCase())),b->{emotion=(emotion+1)%4;AdvancedArtsClient.action(0,emotion);rebuildWidgets();}).bounds(width/2-70,44,140,12).build());
        if(page==3)addRenderableWidget(net.minecraft.client.gui.components.Button.builder(net.minecraft.network.chat.Component.translatable("ui.metallicarts.2734f687a7bf"),b->AdvancedArtsClient.openJournal()).bounds(width/2-70,44,140,12).build());
    }
    @Override public void tick(){if(++refresh%20==0)AdvancedArtsClient.action(3,0);}
    @Override public boolean isPauseScreen() { return false; }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent event){
        if(ContextKeys.LAYOUT.matches(event)){wheel=!wheel;rebuildWidgets();return true;}
        if(net.minecraft.client.KeyMapping.get("key.metallicarts.systems").matches(event)){SystemsClient.open();return true;}
        if(ContextKeys.F.matches(event)){FeruchemyClient.open();return true;}
        if(ContextKeys.H.matches(event)){HemalurgyClient.open();return true;}
        if(ContextKeys.PREVIOUS.matches(event)||ContextKeys.NEXT.matches(event)){page=Math.floorMod(page+(ContextKeys.PREVIOUS.matches(event)?-1:1),4);rebuildWidgets();return true;}
        return super.keyPressed(event);
    }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if(wheel)for(int n=0;n<90;n++){
            double a=n*Math.PI/45;int x=width/2+(int)Math.round(Math.cos(a)*70),y=120+(int)Math.round(Math.sin(a)*60);
            graphics.fill(x-1,y-1,x+1,y+1,0xFF6699CC);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 16, 0xFFFFFFFF);
        graphics.centeredText(font, net.minecraft.network.chat.Component.translatable("ui.metallicarts.3addf7707fdc", (new String[]{"Physical","Mental","Enhancement","Temporal"}[page]), ((page+1))), width / 2, 32, 0xFFFFCC77);
        if(wheel){
            Spiritweb state=MetallicArtsClient.snapshot;
            int burnWidth=Math.min(72,(width-88)/4),burnLeft=(width-4*(burnWidth+4)+4)/2;
        for(int i=0;i<4;i++){
                var metal=dev.metallicarts.allomancy.AllomancyConfig.SUPPORTED.get(page*4+i);
                double a=-Math.PI/2+i*Math.PI/2;
                int x=width/2+(int)Math.round(Math.cos(a)*70),y=120+(int)Math.round(Math.sin(a)*60)+12;
                String mode=dev.metallicarts.allomancy.BurnMode.from(AllomancyClient.status.mode(page*4+i)).name().toLowerCase(java.util.Locale.ROOT);
                long fuel=state!=null&&dev.metallicarts.allomancy.ReserveLedger.valid(state)?dev.metallicarts.allomancy.ReserveLedger.ordinary(state,metal):0;
                graphics.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.2b2bafa3c6dc", (mode), (fuel)),x,y,0xFFCCDDFF);
            }
            graphics.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.493c212ae538"),width/2,208,0xFFCCCCCC);
            graphics.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.5093d7afc3b4"),width/2,224,0xFFAAAAAA);
            return;
        }
        var modes=new StringBuilder();
        for(int i=0;i<4;i++)modes.append(dev.metallicarts.allomancy.AllomancyConfig.SUPPORTED.get(page*4+i).id()).append(": ")
            .append(dev.metallicarts.allomancy.BurnMode.from(AllomancyClient.status.mode(page*4+i)).name().toLowerCase(java.util.Locale.ROOT)).append("  ");
        if((AllomancyClient.status.flags()&4)!=0)modes.append("targets: ").append(AllomancyClient.status.flags()>>8);
        if(page==0||page==2)graphics.centeredText(font,UiText.of(modes.toString()),width/2,44,0xFF77BBFF);
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
            String access = (AdvancedArtsClient.access(state,Art.ALLOMANCY,metal) > 0 ? "A" : "-")
                + "/" + (AdvancedArtsClient.access(state,Art.FERUCHEMY,metal) > 0 ? "F" : "-");
            long ordinary=dev.metallicarts.allomancy.ReserveLedger.valid(state)?dev.metallicarts.allomancy.ReserveLedger.ordinary(state,metal):0;
            String reserve = ordinary<1000?" | "+ordinary+"µg":String.format(java.util.Locale.ROOT, " | %.4fg", ordinary / 1_000_000.0);
            graphics.text(font, Component.translatable("metal.metallicarts." + metal.id()).append("  " + access + reserve), x, y, 0xFFFFFFFF);
        }
        graphics.centeredText(font, Component.translatable("screen.metallicarts.legend"), width / 2, 184, 0xFFAAAAAA);
        graphics.centeredText(font,net.minecraft.network.chat.Component.translatable("ui.metallicarts.1da2ee60dfa3"),width/2,195,0xFFAAAAAA);
        graphics.centeredText(font,(page==0?net.minecraft.network.chat.Component.translatable("ui.metallicarts.9417475f3edd"):net.minecraft.network.chat.Component.translatable("ui.metallicarts.46fd6c953019")),width/2,231,0xFFAAAAAA);
    }
}
