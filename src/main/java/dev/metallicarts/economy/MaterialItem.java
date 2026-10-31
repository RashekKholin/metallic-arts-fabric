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
        var form = EconomyContent.form(stack.getItem());
        if (form != null) text.accept(Component.literal(form.material() + " • " + (stack.has(ModRegistries.METAL_DATA) ? form.kind() : form.units() + " measured units each")));
        var batch = stack.get(EconomyContent.COMPOSITION);
        if (batch != null) { text.accept(Component.literal(batch.quality() + " alloy • " + batch.parts())); }
        var data = stack.get(ModRegistries.METAL_DATA);
        if (data != null) {
            text.accept(Component.literal("Mass: " + data.massMicrograms() + " µg • purity " + Math.round(data.purity() * 100) + "%"));
            if(!stack.has(ModRegistries.METALMIND))text.accept(Component.literal("Stored charge: " + data.charge() + (data.identity().isPresent() ? " • Identity keyed" : " • no Identity key")));
        }
        var mind=stack.get(ModRegistries.METALMIND);
        if(mind!=null){
            text.accept(Component.literal("Feruchemical "+mind.attribute().id()+" • "+mind.charge()+"/"+mind.capacity()+" charge units"));
            text.accept(Component.literal(mind.keyed()?"Identity keyed • ability and matching Identity required":mind.unkeyed()?"Unkeyed • relevant Feruchemical ability still required":"Unfilled • keys to the first filler"));
        }
        if(form!=null&&java.util.List.of("ring","bracelet","bracer","earring").contains(form.kind()))text.accept(Component.literal("Contact equipment • M then F"));
        var spike=stack.get(ModRegistries.SPIKE);
        if(spike!=null){
            text.accept(Component.literal("Hemalurgic "+spike.power().label()+" • "+String.format(java.util.Locale.ROOT,"%.3f",spike.potency())+" potency preview"));
            text.accept(Component.literal(spike.drained()?"Spent • grants no power":spike.blood()?"Blood preserved • slower outside decay":"Outside-body decay • preview refreshes on use"));
            text.accept(Component.literal("M then H • later power handlers may be unavailable"));
        }else if(form!=null&&form.kind().equals("spike"))text.accept(Component.literal("Uncharged spike • M then H for intentional extraction"));
    }
}
