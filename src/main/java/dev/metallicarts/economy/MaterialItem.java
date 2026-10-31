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
            text.accept(Component.literal("Stored charge: " + data.charge() + (data.identity().isPresent() ? " • Identity keyed" : " • no Identity key")));
        }
        if (form != null && java.util.List.of("ring","bracelet","bracer","spike").contains(form.kind())) text.accept(Component.literal("Equipment • powers arrive in later phases"));
    }
}
