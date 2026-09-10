package com.carloshdzz22.zombieinfection.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class TooltipItem extends Item {
    private final String[] tooltipKeys;

    public TooltipItem(Properties properties, String... tooltipKeys) {
        super(properties);
        this.tooltipKeys = tooltipKeys.clone();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltip, tooltipFlag);
        for (String key : tooltipKeys) {
            tooltip.accept(Component.translatable(key).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
}
