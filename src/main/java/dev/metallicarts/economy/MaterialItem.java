package dev.metallicarts.economy;

import dev.metallicarts.registry.ModRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import java.util.function.Consumer;

public class MaterialItem extends Item {
    public MaterialItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> text, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, text, flag);
        var cores=stack.get(dev.metallicarts.equipment.Medallions.CORES);if(cores!=null){text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.986b44e6ebbb", (cores.size())));for(int i=0;i<cores.size();i++){var core=cores.get(i).get(ModRegistries.METALMIND);if(core!=null)text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.1ea776d04863", ((i+1)), (core.contentLabel()), (core.charge()), (core.capacity())));}text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.039c6a4fc972"));}
        if(stack.getItem() instanceof GodmetalItem)text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.6ae2c0aac044"));
        if(stack.is(EconomyContent.ITEMS.get("harmonium_sample")))text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.fdd0fd90f7d9"));
        if(stack.is(EconomyContent.ITEMS.get("trellium_sample")))text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.f13c6a7720d9"));
        var form = EconomyContent.form(stack.getItem());
        if (form != null) text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.089331a24aff", (form.material()), ((stack.has(ModRegistries.METAL_DATA) ? form.kind() : form.units() + " measured units each"))));
        var batch = stack.get(EconomyContent.COMPOSITION);
        if (batch != null) { text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.4a48bab59dc2", (batch.quality()), (batch.parts()))); }
        var data = stack.get(ModRegistries.METAL_DATA);
        if (data != null) {
            text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.778e6c766294", (data.massMicrograms()), (Math.round(data.purity() * 100))));
            if(!stack.has(ModRegistries.METALMIND))text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.971992ade841", (data.charge()), ((data.identity().isPresent() ? " • Identity keyed" : " • no Identity key"))));
        }
        var mind=stack.get(ModRegistries.METALMIND);
        if(mind!=null){
            text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.ced334eef81f", (mind.attribute().id()), (mind.charge()), (mind.capacity())));
            text.accept((mind.unsealed()?net.minecraft.network.chat.Component.translatable("ui.metallicarts.dd8fad48bef9"):(mind.keyed()?net.minecraft.network.chat.Component.translatable("ui.metallicarts.24f65c28b0cc"):(mind.unkeyed()?net.minecraft.network.chat.Component.translatable("ui.metallicarts.5d2275cf5cf4"):net.minecraft.network.chat.Component.translatable("ui.metallicarts.46537226c1c2")))));
            if(!mind.contentLabel().isEmpty())text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.973933c8ebb9", (mind.contentLabel())));
            if(mind.attribute()==dev.metallicarts.feruchemy.FeruchemicalAttribute.MEMORY)text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.cc59773cd0b1", (mind.memories())));
        }
        if(form!=null&&java.util.List.of("ring","bracelet","bracer","earring").contains(form.kind()))text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.c8a4292fcae4"));
        var spike=stack.get(ModRegistries.SPIKE);
        if(spike!=null){
            text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.cc0d522ab6a3", (spike.power().label()), (String.format(java.util.Locale.ROOT,"%.3f",spike.potency()))));
            text.accept((spike.drained()?net.minecraft.network.chat.Component.translatable("ui.metallicarts.fb9d85a4da52"):(spike.blood()?net.minecraft.network.chat.Component.translatable("ui.metallicarts.d88abeef1b11"):net.minecraft.network.chat.Component.translatable("ui.metallicarts.49e605fafd51"))));
            text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.50c8c8bceff4"));
        }else if(form!=null&&form.kind().equals("spike"))text.accept(net.minecraft.network.chat.Component.translatable("ui.metallicarts.0837b2f309e9"));
    }
}
